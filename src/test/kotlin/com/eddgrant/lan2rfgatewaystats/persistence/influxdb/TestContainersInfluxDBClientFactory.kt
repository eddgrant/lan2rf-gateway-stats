package com.eddgrant.lan2rfgatewaystats.persistence.influxdb

import com.influxdb.client.InfluxDBClientOptions
import com.influxdb.client.kotlin.InfluxDBClientKotlin
import com.influxdb.client.kotlin.InfluxDBClientKotlinFactory
import io.micronaut.context.annotation.Factory
import io.micronaut.context.annotation.Replaces
import io.micronaut.context.annotation.Requirements
import io.micronaut.context.annotation.Requires
import jakarta.inject.Singleton

@Requirements(
    Requires(bean = InfluxDBConfiguration::class),
    Requires(env = ["influxdb-integration-test"])
)
@Replaces(bean = StandaloneInfluxDBClientFactory::class)
@Factory
class TestContainersInfluxDBClientFactory(
    private val influxDBConfiguration: InfluxDBConfiguration,
    private val influxDBService: InfluxDBService
) : InfluxDBClientFactory {

    /**
     * Used in influxdb-integration-tests in which the InfluxDB
     * is provided by TestContainers, thus its URL is not known
     * until it has been created.
     */
    @Singleton
    @Replaces(value = InfluxDBClientKotlin::class, factory = StandaloneInfluxDBClientFactory::class)
    override fun influxDBClient() : InfluxDBClientKotlin {
        val influxDBClientOptions = InfluxDBClientOptions
            .builder()
            .authenticateToken(influxDBConfiguration.token.toCharArray())
            .bucket(influxDBConfiguration.bucket)
            .logLevel(influxDBConfiguration.logLevel)
            .org(influxDBConfiguration.org)
            .url(influxDBService.influxdb.url)
            .build()
        return InfluxDBClientKotlinFactory.create(influxDBClientOptions)
    }
}