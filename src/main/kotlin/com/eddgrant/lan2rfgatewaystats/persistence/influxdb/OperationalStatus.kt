package com.eddgrant.lan2rfgatewaystats.persistence.influxdb

import com.influxdb.annotations.Column
import com.influxdb.annotations.Measurement
import java.time.Instant

@Measurement(name = "OperationalStatus")
data class OperationalStatus(
    @Column(tag = true) val source: String,
    @Column(tag = true) val subject: String,
    @Column val active: Boolean,
    @Column(timestamp = true) val time: Instant,
)