package com.eddgrant.lan2rfgatewaystats.persistence.influxdb

import com.influxdb.annotations.Column
import com.influxdb.annotations.Measurement
import java.time.Instant

@Measurement(name = "Pressure")
data class Pressure(
    @Column(tag = true) val source: String,
    @Column(tag = true) val subject: String,
    @Column val value: Double,
    @Column(timestamp = true) val time: Instant,
)