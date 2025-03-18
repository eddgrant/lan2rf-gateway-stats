package com.eddgrant.lan2rfgatewaystats.persistence.influxdb

import com.influxdb.annotations.Column
import com.influxdb.annotations.Measurement
import java.time.Instant

@Measurement(name = "Status")
data class Status(
    @Column(tag = true) val source: String,
    @Column val value: String,
    @Column(timestamp = true) val time: Instant,
)