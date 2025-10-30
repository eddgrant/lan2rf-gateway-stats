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
import org.slf4j.LoggerFactory
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.time.Instant
import java.time.Instant.now

@Singleton
class StatusDataPublisher(
    private val influxDBClientKotlin: InfluxDBClientKotlin,
    private val lan2RFConfiguration: LAN2RFConfiguration
) {

    fun emitStatusDataAsDiscreteMeasurements(statusData: Flux<StatusData>): Flux<Void> {
        return statusData
            .flatMap { statusDataValue ->
                Mono.create<Void> { sink ->
                    val job = CoroutineScope(Dispatchers.IO).launch {
                        try {
                            val now = now()
                            val measurements = asMeasurements(statusDataValue, now)
                            influxDBClientKotlin
                                .getWriteKotlinApi()
                                .writeMeasurements(
                                    measurements,
                                    WritePrecision.MS
                                )
                            LOGGER.debug("Measurements written for timestamp: {}", now)
                            sink.success()
                        } catch (e: Exception) {
                            sink.error(e)
                        }
                    }
                    sink.onDispose { job.cancel() }
                }
            }
    }

    private fun asMeasurements(
        statusDataValue: StatusData,
        now: Instant
    ): Set<Any> {
        val measurements = setOf(
            Temperature(
                lan2RFConfiguration.source,
                "central_heating",
                statusDataValue.centralHeatingTemperature(),
                RECORDED,
                now
            ),
            Temperature(
                lan2RFConfiguration.source,
                "room1",
                statusDataValue.room1Temperature(),
                RECORDED,
                now
            ),
            Temperature(
                lan2RFConfiguration.source,
                "room2",
                statusDataValue.room2Temperature(),
                RECORDED,
                now
            ),
            Temperature(
                lan2RFConfiguration.source,
                "tap",
                statusDataValue.tapTemperature(),
                RECORDED,
                now
            ),
            Temperature(
                lan2RFConfiguration.source,
                "room1",
                statusDataValue.room1TemperatureSetpoint(),
                SETPOINT,
                now
            ),
            Temperature(
                lan2RFConfiguration.source,
                "room1",
                statusDataValue.room1TemperatureSetpointOverride(),
                SETPOINT_OVERRIDE,
                now
            ),
            Temperature(
                lan2RFConfiguration.source,
                "room2",
                statusDataValue.room2TemperatureSetpoint(),
                SETPOINT,
                now
            ),
            Temperature(
                lan2RFConfiguration.source,
                "room2",
                statusDataValue.room2TemperatureSetpointOverride(),
                SETPOINT_OVERRIDE,
                now
            ),
            Pressure(
                lan2RFConfiguration.source,
                "central_heating",
                statusDataValue.centralHeatingPressure(),
                now
            ),
            Status(lan2RFConfiguration.source, statusDataValue.getStatusDisplayCode(), now)
        )
        return measurements
    }

    companion object {
        private val LOGGER = LoggerFactory.getLogger(StatusDataPublisher::class.java)
    }
}