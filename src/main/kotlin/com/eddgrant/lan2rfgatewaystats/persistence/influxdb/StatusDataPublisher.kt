package com.eddgrant.lan2rfgatewaystats.persistence.influxdb

import com.eddgrant.lan2rfgatewaystats.intergas.LAN2RFConfiguration
import com.eddgrant.lan2rfgatewaystats.intergas.StatusData
import com.eddgrant.lan2rfgatewaystats.intergas.StatusData.Companion.IO_STATUS_NAME_BURNER_ACTIVE
import com.eddgrant.lan2rfgatewaystats.intergas.StatusData.Companion.IO_STATUS_NAME_LOCKED_OUT
import com.eddgrant.lan2rfgatewaystats.intergas.StatusData.Companion.IO_STATUS_NAME_PUMP_ACTIVE
import com.eddgrant.lan2rfgatewaystats.intergas.StatusData.Companion.IO_STATUS_NAME_TAP_FUNCTION_ACTIVE
import com.eddgrant.lan2rfgatewaystats.intergas.StatusData.Companion.STATUS_DISPLAY_CODE_NAME
import com.eddgrant.lan2rfgatewaystats.persistence.influxdb.Temperature.Type.*
import com.influxdb.client.domain.WritePrecision
import com.influxdb.client.kotlin.InfluxDBClientKotlin
import io.micronaut.context.annotation.Requires
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
@Requires(beans = [InfluxDBClientKotlin::class, LAN2RFConfiguration::class])
class StatusDataPublisher(
    private val influxDBClientKotlin: InfluxDBClientKotlin,
    private val lan2RFConfiguration: LAN2RFConfiguration
) {

    fun publishAsDiscreteMeasurements(statusData: Flux<StatusData>): Flux<Void> {
        return statusData
            .flatMap { statusData ->
                Mono.create<Void> { sink ->
                    val job = CoroutineScope(Dispatchers.IO).launch {
                        try {
                            val now = now()
                            val measurements = asMeasurements(statusData, now)
                            influxDBClientKotlin
                                .getWriteKotlinApi()
                                .writeMeasurements(
                                    measurements,
                                    WritePrecision.MS
                                )
                            LOGGER.info("Status Data measurements sent at: {}", now)
                            LOGGER.debug("Measurements: {}", measurements)
                            sink.success()
                        } catch (e: Exception) {
                            sink.error(e)
                        }
                    }
                    sink.onDispose { job.cancel() }
                }
                .onErrorResume { e ->
                    LOGGER.error("Failed to write measurements to InfluxDB. Will retry on next interval.", e)
                    Mono.empty()
                }
            }
    }

    private fun asMeasurements(
        statusData: StatusData,
        now: Instant
    ): Set<Any> {
        return buildSet {
            if (lan2RFConfiguration.measurements.boiler) {
                addAll(createBoilerMeasurements(statusData, now))
            }

            if (lan2RFConfiguration.measurements.room1) {
                addAll(createRoom1Measurements(statusData, now))
            }

            if (lan2RFConfiguration.measurements.room2) {
                addAll(createRoom2Measurements(statusData, now))
            }
        }
    }

    private fun createBoilerMeasurements(
        statusDataValue: StatusData,
        now: Instant
    ): Set<Any> {
        return setOf(
            Temperature(
                lan2RFConfiguration.source,
                "central_heating",
                statusDataValue.centralHeatingTemperature(),
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
            Pressure(
                lan2RFConfiguration.source,
                "central_heating",
                statusDataValue.centralHeatingPressure(),
                now
            ),
            TextStatus(lan2RFConfiguration.source, STATUS_DISPLAY_CODE_NAME, statusDataValue.getStatusDisplayCode(), now),
            OperationalStatus(lan2RFConfiguration.source, IO_STATUS_NAME_LOCKED_OUT, statusDataValue.isLockedOut(), now),
            OperationalStatus(lan2RFConfiguration.source, IO_STATUS_NAME_PUMP_ACTIVE, statusDataValue.isPumpActive(), now),
            OperationalStatus(lan2RFConfiguration.source, IO_STATUS_NAME_TAP_FUNCTION_ACTIVE, statusDataValue.isTapFunctionActive(), now),
            OperationalStatus(lan2RFConfiguration.source, IO_STATUS_NAME_BURNER_ACTIVE, statusDataValue.isBurnerActive(), now)
        )
    }

    private fun createRoom1Measurements(
        statusDataValue: StatusData,
        now: Instant
    ): Set<Any> {
        val room1 = lan2RFConfiguration.room1Name
        return setOf(
            Temperature(
                lan2RFConfiguration.source,
                room1,
                statusDataValue.room1Temperature(),
                RECORDED,
                now
            ),
            Temperature(
                lan2RFConfiguration.source,
                room1,
                statusDataValue.room1TemperatureSetpoint(),
                SETPOINT,
                now
            ),
            Temperature(
                lan2RFConfiguration.source,
                room1,
                statusDataValue.room1TemperatureSetpointOverride(),
                SETPOINT_OVERRIDE,
                now
            )
        )
    }

    private fun createRoom2Measurements(
        statusDataValue: StatusData,
        now: Instant
    ): Set<Any> {
        val room2 = lan2RFConfiguration.room2Name
        return setOf(
            Temperature(
                lan2RFConfiguration.source,
                room2,
                statusDataValue.room2Temperature(),
                RECORDED,
                now
            ),
            Temperature(
                lan2RFConfiguration.source,
                room2,
                statusDataValue.room2TemperatureSetpoint(),
                SETPOINT,
                now
            ),
            Temperature(
                lan2RFConfiguration.source,
                room2,
                statusDataValue.room2TemperatureSetpointOverride(),
                SETPOINT_OVERRIDE,
                now
            )
        )
    }

    companion object {
        private val LOGGER = LoggerFactory.getLogger(StatusDataPublisher::class.java)
    }
}