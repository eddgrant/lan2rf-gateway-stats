package com.eddgrant.lan2rfgatewaystats.persistence.influxdb

import com.influxdb.LogLevel
import io.micronaut.context.annotation.ConfigurationProperties
import io.micronaut.context.annotation.Requires
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import java.net.URL

@ConfigurationProperties("influxdb")
@Requires(property = "influxdb.bucket")
@Requires(property = "influxdb.org")
@Requires(property = "influxdb.token")
@Requires(property = "influxdb.url")
class InfluxDBConfiguration {

    @NotBlank
    lateinit var bucket : String

    @NotBlank
    lateinit var org : String

    @NotNull
    var logLevel : LogLevel = LogLevel.NONE

    @NotBlank
    lateinit var token : String

    @NotNull
    lateinit var url : URL
}
