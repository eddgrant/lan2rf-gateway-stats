package com.eddgrant.lan2rfgatewaystats

import com.eddgrant.lan2rfgatewaystats.intergas.LAN2RFConfiguration
import com.eddgrant.lan2rfgatewaystats.persistence.influxdb.InfluxDBConfiguration
import com.influxdb.client.kotlin.InfluxDBClientKotlin
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.micronaut.test.extensions.kotest5.annotation.MicronautTest
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.toList
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * This test is an end-to-end test that verifies
 * that the application can collect data from a LAN2RF
 * and persist it in an InfluxDB instance.
 *
 * The test requires a real LAN2RF to be available on the network,
 * so this test is not suitable for automated CI/CD pipelines.
 */
@MicronautTest(
    environments = ["influxdb-integration-test"]
)
class LAN2RFGatewayStatsEndToEndTest(
    private val lan2RFGatewayStats: LAN2RFGatewayStats,
    private val laN2RFConfiguration: LAN2RFConfiguration,
    private val influxDBConfiguration: InfluxDBConfiguration,
    private val influxDBClient: InfluxDBClientKotlin
) : BehaviorSpec({

    beforeSpec {
        CoroutineScope(Dispatchers.IO).launch {
            lan2RFGatewayStats.run()
        }
    }

    afterSpec {
        lan2RFGatewayStats.onShutdown()
    }

    Given("the LAN2RFGatewayStats application is running") {
        When("enough time has passed for status data to have been published") {
            runBlocking {
                // Wait for a period longer than the check interval to ensure data has been published.
                delay(laN2RFConfiguration.checkInterval.toMillis() + 2000)
            }

            Then("the expected measurements should be present in InfluxDB") {
                val query = """
                    from(bucket: "${influxDBConfiguration.bucket}")
                        ||> range(start: -1m)
                        ||> filter(fn: (r) => (
                        |   r["source"] == "${laN2RFConfiguration.source}"))""".trimMargin()

                val actualRecords = runBlocking {
                    influxDBClient.getQueryKotlinApi().query(query).toList()
                }

                /**
                 * We can't assert any of the measurement values as they are
                 * all coming from the live system. All we can assert is that there is
                 * the expected record count of each measurement type.
                 */
                val statuses = actualRecords.filter { it.measurement == "Status" }
                statuses.size.shouldBe(1)

                val pressures = actualRecords.filter { it.measurement == "Pressure" }
                pressures.size.shouldBe(1)

                val temperatures = actualRecords.filter { it.measurement == "Temperature" }
                temperatures.size.shouldBe(8)
            }
        }
    }
}) {
    companion object {
        val LOGGER: Logger = LoggerFactory.getLogger(LAN2RFGatewayStatsEndToEndTest::class.java)
    }
}
