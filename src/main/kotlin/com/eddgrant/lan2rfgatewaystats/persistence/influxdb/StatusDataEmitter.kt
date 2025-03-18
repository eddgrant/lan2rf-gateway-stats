package com.eddgrant.lan2rfgatewaystats.persistence.influxdb

import com.eddgrant.lan2rfgatewaystats.intergas.LAN2RFConfiguration
import com.eddgrant.lan2rfgatewaystats.intergas.StatusData
import com.eddgrant.lan2rfgatewaystats.persistence.influxdb.Temperature.Type.*
import com.influxdb.client.domain.WritePrecision
import com.influxdb.client.kotlin.InfluxDBClientKotlin
import jakarta.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import reactor.core.publisher.Mono
import java.time.Instant

@Singleton
class StatusDataEmitter(
    private val influxDBClientKotlin: InfluxDBClientKotlin,
    private val lan2RFConfiguration: LAN2RFConfiguration
) {
    suspend fun emitStatusDataAsDiscreteMeasurements(statusData: Mono<StatusData>) {
        val now = Instant.now()

        statusData.subscribe {
            val centralHeatingTemperature = Temperature(lan2RFConfiguration.source, "central_heating", it.centralHeatingTemperature(), RECORDED, now)
            val room1Temperature = Temperature(lan2RFConfiguration.source, "room1", it.room1Temperature(), RECORDED, now)
            val room2Temperature = Temperature(lan2RFConfiguration.source, "room2", it.room2Temperature(), RECORDED, now)
            val tapTemperature = Temperature(lan2RFConfiguration.source, "tap", it.tapTemperature(), RECORDED, now)
            val room1TemperatureSetPoint = Temperature(lan2RFConfiguration.source, "room1", it.room1TemperatureSetpoint(), SETPOINT, now)
            val room1TemperatureSetPointOverride = Temperature(lan2RFConfiguration.source, "room1", it.room1TemperatureSetpointOverride(), SETPOINT_OVERRIDE, now)
            val room2TemperatureSetPoint = Temperature(lan2RFConfiguration.source, "room2", it.room2TemperatureSetpoint(), SETPOINT, now)
            val room2TemperatureSetPointOverride = Temperature(lan2RFConfiguration.source, "room2", it.room2TemperatureSetpointOverride(), SETPOINT_OVERRIDE, now)
            val centralHeatingPressure = Pressure(lan2RFConfiguration.source, "central_heating", it.centralHeatingPressure(), now)
            val boilerStatus = Status(lan2RFConfiguration.source, it.getStatusDisplayCode(), now)

            // isLockedOut

            runBlocking(Dispatchers.IO) {
                writeMeasurement(centralHeatingTemperature)
                writeMeasurement(room1Temperature)
                writeMeasurement(room2Temperature)
                writeMeasurement(tapTemperature)
                writeMeasurement(room1TemperatureSetPoint)
                writeMeasurement(room1TemperatureSetPointOverride)
                writeMeasurement(room2TemperatureSetPoint)
                writeMeasurement(room2TemperatureSetPointOverride)
                writeMeasurement(centralHeatingPressure)
                writeMeasurement(boilerStatus)
            }
        }
    }

    private fun <M> CoroutineScope.writeMeasurement(measurement: M) {
        launch {
            influxDBClientKotlin
                .getWriteKotlinApi()
                .writeMeasurement(
                    measurement,
                    WritePrecision.MS
                )
            println("Measurement written:  $measurement")
        }
    }
}