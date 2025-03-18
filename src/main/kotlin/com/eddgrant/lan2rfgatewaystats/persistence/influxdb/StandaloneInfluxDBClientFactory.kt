package com.eddgrant.lan2rfgatewaystats.persistence.influxdb

import com.influxdb.client.InfluxDBClientOptions
import com.influxdb.client.kotlin.InfluxDBClientKotlin
import com.influxdb.client.kotlin.InfluxDBClientKotlinFactory
import io.micronaut.context.annotation.Factory
import io.micronaut.context.annotation.Requires
import jakarta.inject.Singleton

interface InfluxDBClientFactory {
    fun influxDBClient() : InfluxDBClientKotlin
}

@Requires(bean = InfluxDBConfiguration::class)
@Factory
class StandaloneInfluxDBClientFactory(
    private val influxDBConfiguration: InfluxDBConfiguration
) : InfluxDBClientFactory {

    @Singleton
    override fun influxDBClient() : InfluxDBClientKotlin {
        val influxDBClientOptions = InfluxDBClientOptions
            .builder()
            .authenticateToken(influxDBConfiguration.token.toCharArray())
            .bucket(influxDBConfiguration.bucket)
            .logLevel(influxDBConfiguration.logLevel)
            .org(influxDBConfiguration.org)
            .url(influxDBConfiguration.url.toString())
            .build()
        return InfluxDBClientKotlinFactory.create(influxDBClientOptions)
    }
}