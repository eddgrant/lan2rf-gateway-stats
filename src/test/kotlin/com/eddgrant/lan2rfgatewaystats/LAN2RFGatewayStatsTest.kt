package com.eddgrant.lan2rfgatewaystats

import com.eddgrant.lan2rfgatewaystats.intergas.LAN2RFClient
import com.eddgrant.lan2rfgatewaystats.persistence.influxdb.StatusDataOrchestrator
import io.kotest.core.spec.style.StringSpec
import io.micronaut.test.annotation.MockBean
import io.micronaut.test.extensions.kotest5.annotation.MicronautTest
import io.mockk.mockk

@MicronautTest(environments = ["lan2rf-integration-test"])
class LAN2RFGatewayStatsTest(
    private val lan2RFClient : LAN2RFClient,
    private val statusDataOrchestrator: StatusDataOrchestrator,
    private val laN2RFGatewayStats: LAN2RFGatewayStats,
) : StringSpec({

    /*"it sends the data to InfluxDB" {
        // Given
        // TODO: Set up Mock responses
        val publishDuration = Duration.parse("50ms")
        val laN2RFConfigurationMock = getMock(laN2RFConfiguration)
        every { laN2RFConfigurationMock.checkInterval } returns publishDuration.toJavaDuration()

        val intergasServiceMock = getMock(intergasService)
        every { intergasServiceMock.getStatusData() } returns Mono.just(StatusDataTestFixtures.BASIC)


        // When
        // TODO: Run this in a background thread so it doesn't block.
        laN2RFGatewayStats.run()

        // Check that data is being send to InfluxDB.
        println("sdouihasdouahsdouahsd")
        println("sdouihasdouahsdouahsd")
        println(statusDataOrchestrator)
    }*/

}) {
    @MockBean(LAN2RFClient::class)
    fun lan2RFClient(): LAN2RFClient = mockk<LAN2RFClient>()
}
