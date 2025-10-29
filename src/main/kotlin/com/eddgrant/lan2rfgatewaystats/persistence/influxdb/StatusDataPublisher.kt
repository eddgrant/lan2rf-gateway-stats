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
import org.slf4j.LoggerFactory
import reactor.core.Disposable
import reactor.core.publisher.Flux
import reactor.core.scheduler.Schedulers
import java.time.Instant

@Singleton
class StatusDataPublisher(
    private val influxDBClientKotlin: InfluxDBClientKotlin,
    private val lan2RFConfiguration: LAN2RFConfiguration
) {

    fun emitStatusDataAsDiscreteMeasurements(statusData: Flux<StatusData>): Disposable {
        return statusData
            .subscribeOn(Schedulers.boundedElastic())
            .subscribe {
                rawEmitStatusDataAsDiscreteMeasurements(it)
            }
    }

    fun rawEmitStatusDataAsDiscreteMeasurements(statusData: StatusData) {
        val now = Instant.now()

        val centralHeatingTemperature = Temperature(lan2RFConfiguration.source, "central_heating", statusData.centralHeatingTemperature(), RECORDED, now)
        val room1Temperature = Temperature(lan2RFConfiguration.source, "room1", statusData.room1Temperature(), RECORDED, now)
        val room2Temperature = Temperature(lan2RFConfiguration.source, "room2", statusData.room2Temperature(), RECORDED, now)
        val tapTemperature = Temperature(lan2RFConfiguration.source, "tap", statusData.tapTemperature(), RECORDED, now)
        val room1TemperatureSetPoint = Temperature(lan2RFConfiguration.source, "room1", statusData.room1TemperatureSetpoint(), SETPOINT, now)
        val room1TemperatureSetPointOverride = Temperature(lan2RFConfiguration.source, "room1", statusData.room1TemperatureSetpointOverride(), SETPOINT_OVERRIDE, now)
        val room2TemperatureSetPoint = Temperature(lan2RFConfiguration.source, "room2", statusData.room2TemperatureSetpoint(), SETPOINT, now)
        val room2TemperatureSetPointOverride = Temperature(lan2RFConfiguration.source, "room2", statusData.room2TemperatureSetpointOverride(), SETPOINT_OVERRIDE, now)
        val centralHeatingPressure = Pressure(lan2RFConfiguration.source, "central_heating", statusData.centralHeatingPressure(), now)
        val boilerStatus = Status(lan2RFConfiguration.source, statusData.getStatusDisplayCode(), now)

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

    private fun <M> CoroutineScope.writeMeasurement(measurement: M) {
        launch {
            influxDBClientKotlin
                .getWriteKotlinApi()
                .writeMeasurement(
                    measurement,
                    WritePrecision.MS
                )
            LOGGER.debug("Measurement written:  {}", measurement)
        }
    }

    companion object {
        private val LOGGER = LoggerFactory.getLogger(StatusDataPublisher::class.java)
    }
}