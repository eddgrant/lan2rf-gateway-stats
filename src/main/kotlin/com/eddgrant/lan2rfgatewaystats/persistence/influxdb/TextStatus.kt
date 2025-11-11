package com.eddgrant.lan2rfgatewaystats.persistence.influxdb

import com.influxdb.annotations.Column
import com.influxdb.annotations.Measurement
import java.time.Instant

@Measurement(name = "TextStatus")
data class TextStatus(
    @Column(tag = true) val source: String,
    @Column(tag = true) val subject: String,
    @Column val value: String,
    @Column(timestamp = true) val time: Instant,
)