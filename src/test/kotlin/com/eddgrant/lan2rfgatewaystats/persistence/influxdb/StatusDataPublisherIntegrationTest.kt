@file:OptIn(ExperimentalCoroutinesApi::class)

package com.eddgrant.lan2rfgatewaystats.persistence.influxdb

import com.eddgrant.lan2rfgatewaystats.intergas.LAN2RFConfiguration.Companion.LAN2RF_DEFAULT_SOURCE_NAME
import com.eddgrant.lan2rfgatewaystats.intergas.StatusDataTestFixtures.Companion.BASIC
import com.eddgrant.lan2rfgatewaystats.persistence.influxdb.Temperature.Type.*
import com.influxdb.client.kotlin.InfluxDBClientKotlin
import com.influxdb.query.FluxRecord
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.micronaut.test.extensions.kotest5.annotation.MicronautTest
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import reactor.core.publisher.Flux

@MicronautTest(environments = ["influxdb-integration-test"])
class StatusDataPublisherIntegrationTest(
    private val influxDBConfiguration: InfluxDBConfiguration,
    private val influxDBService: InfluxDBService,
    private val influxDBClientKotlin: InfluxDBClientKotlin,
    private val statusDataPublisher: StatusDataPublisher
) : BehaviorSpec({

    fun findTemperatures(
        source: String,
        location: String,
        type: Temperature.Type,
    ): Channel<FluxRecord> {
        val fluxQuery =
            """from(bucket: "${influxDBConfiguration.bucket}") 
            ||> range(start: 0) 
            ||> filter(fn: (r) => (
            |   r["_measurement"] == "Temperature" 
            |   and r["source"] == "$source" 
            |   and r["type"] == "${type.name}" 
            |   and r["location"] == "$location"))""".trimMargin()
        return influxDBClientKotlin.getQueryKotlinApi().query(fluxQuery)
    }

    fun findPressures(
        source: String,
        subject: String,
    ): Channel<FluxRecord> {
        val fluxQuery =
            """from(bucket: "${influxDBConfiguration.bucket}") 
            ||> range(start: 0) 
            ||> filter(fn: (r) => (
            |   r["_measurement"] == "Pressure" 
            |   and r["source"] == "$source" 
            |   and r["subject"] == "$subject"))""".trimMargin()
        return influxDBClientKotlin.getQueryKotlinApi().query(fluxQuery)
    }

    fun findStatuses(
        source: String,
    ): Channel<FluxRecord> {
        val fluxQuery =
            """from(bucket: "${influxDBConfiguration.bucket}") 
            ||> range(start: 0) 
            ||> filter(fn: (r) => (
            |   r["_measurement"] == "Status" 
            |   and r["source"] == "$source"))""".trimMargin()
        return influxDBClientKotlin.getQueryKotlinApi().query(fluxQuery)
    }

    suspend fun assertTemperatureInInfluxDB(
        source: String,
        location: String,
        expectedTemperature: Double,
        type: Temperature.Type,
    ) {
        val results = findTemperatures(source, location, type)
        val result = results.receive()
        result.measurement.shouldBe("Temperature")
        result.value.shouldBe(expectedTemperature)
        result.values["source"].shouldBe(source)
        result.values["location"].shouldBe(location)
        result.values["type"].shouldBe(type.name)

        // Assert there was exactly 1 matched result.
        results.receiveCatching().getOrNull().shouldBeNull()
    }

    suspend fun assertPressureInInfluxDB(
        source: String,
        subject: String,
        expectedPressure: Double
    ) {
        val results = findPressures(source, subject)
        val result = results.receive()
        result.measurement.shouldBe("Pressure")
        result.value.shouldBe(expectedPressure)
        result.values["source"].shouldBe(source)
        result.values["subject"].shouldBe(subject)

        // Assert there was exactly 1 matched result.
        results.receiveCatching().getOrNull().shouldBeNull()
    }

    suspend fun assertStatusInInfluxDB(
        source: String,
        expectedStatus: String
    ) {
        val results = findStatuses(source)
        val result = results.receive()
        result.measurement.shouldBe("Status")
        result.value.shouldBe(expectedStatus)

        // Assert there was exactly 1 matched result.
        results.receiveCatching().getOrNull().shouldBeNull()
    }

    afterSpec {
        influxDBService.influxdb.stop()
    }

    context("it should send discrete measurements to influxdb") {

        given("a status data exists") {
            val statusData = Flux.just(BASIC)
            `when`("the status data is emitted") {
                statusDataPublisher.publishAsDiscreteMeasurements(statusData)
                    .blockLast()

                then("Room 1 temperature is sent to InfluxDB") {
                    assertTemperatureInInfluxDB(LAN2RF_DEFAULT_SOURCE_NAME, "room1", BASIC.room1Temperature(), RECORDED)
                }
                then("Room 2 temperature is sent to InfluxDB") {
                    assertTemperatureInInfluxDB(LAN2RF_DEFAULT_SOURCE_NAME, "room2", BASIC.room2Temperature(), RECORDED)
                }
                then("Central Heating temperature is sent to InfluxDB") {
                    assertTemperatureInInfluxDB(LAN2RF_DEFAULT_SOURCE_NAME, "central_heating", BASIC.centralHeatingTemperature(), RECORDED)
                }
                then("Tap temperature is sent to InfluxDB") {
                    assertTemperatureInInfluxDB(LAN2RF_DEFAULT_SOURCE_NAME, "tap", BASIC.tapTemperature(), RECORDED)
                }
                then("Room 1 temperature set point is sent to InfluxDB") {
                    assertTemperatureInInfluxDB(LAN2RF_DEFAULT_SOURCE_NAME, "room1", BASIC.room1TemperatureSetpoint(), SETPOINT)
                }
                then("Room 2 temperature set point is sent to InfluxDB") {
                    assertTemperatureInInfluxDB(LAN2RF_DEFAULT_SOURCE_NAME, "room2", BASIC.room2TemperatureSetpoint(), SETPOINT)
                }
                then("Room 1 temperature set point override is sent to InfluxDB") {
                    assertTemperatureInInfluxDB(LAN2RF_DEFAULT_SOURCE_NAME, "room1", BASIC.room1TemperatureSetpointOverride(), SETPOINT_OVERRIDE)
                }
                then("Room 2 temperature set point override is sent to InfluxDB") {
                    assertTemperatureInInfluxDB(LAN2RF_DEFAULT_SOURCE_NAME, "room2", BASIC.room2TemperatureSetpointOverride(), SETPOINT_OVERRIDE)
                }
                then("Central heating pressure is sent to InfluxDB") {
                    assertPressureInInfluxDB(LAN2RF_DEFAULT_SOURCE_NAME, "central_heating", BASIC.centralHeatingPressure())
                }
                then("Boiler status is sent to InfluxDB") {
                    assertStatusInInfluxDB(LAN2RF_DEFAULT_SOURCE_NAME, BASIC.getStatusDisplayCode())
                }
            }
        }
    }
})
