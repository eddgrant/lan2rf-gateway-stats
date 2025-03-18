package com.eddgrant.lan2rfgatewaystats.persistence.influxdb

import io.micronaut.context.annotation.Requires
import jakarta.inject.Singleton
import org.testcontainers.containers.InfluxDBContainer
import org.testcontainers.utility.DockerImageName

@Singleton
@Requires(env =  ["influxdb-integration-test"])
class InfluxDBService(
    influxDBConfiguration: InfluxDBConfiguration
) {
    val influxdb = InfluxDBContainer(
        DockerImageName.parse("influxdb:2.0.7")
    )
        .withAdminToken(influxDBConfiguration.token)
        .withOrganization(influxDBConfiguration.org)
        .withBucket(influxDBConfiguration.bucket)

    init {
        /**
         * Start here so other the InfluxDB URL is computed
         * as it is required by other beans.
         */
        influxdb.start()
    }
}