package com.eddgrant.lan2rfgatewaystats.persistence.influxdb

import com.influxdb.annotations.Column
import com.influxdb.annotations.Measurement
import java.time.Instant

@Measurement(name = "Temperature")
data class Temperature(
    @Column(tag = true) val source: String,
    @Column(tag = true) val location: String,
    @Column val value: Double,
    @Column(tag = true) val type: Type,
    @Column(timestamp = true) val time: Instant,
) {
    enum class Type {
        RECORDED,
        SETPOINT,
        SETPOINT_OVERRIDE
    }
}