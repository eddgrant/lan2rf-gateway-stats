package com.eddgrant.lan2rfgatewaystats.intergas

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.micronaut.test.annotation.MockBean
import io.micronaut.test.extensions.kotest5.MicronautKotest5Extension.getMock
import io.micronaut.test.extensions.kotest5.annotation.MicronautTest
import io.mockk.every
import io.mockk.mockk
import org.slf4j.LoggerFactory
import reactor.core.publisher.Mono
import kotlin.time.Duration
import kotlin.time.toJavaDuration


@MicronautTest(environments = ["lan2rf-integration-test"])
class LAN2RFPublisherTest(
    private val intergasService: IntergasService,
    private val lan2RfRepository: LAN2RFRepository,
) : StringSpec({

    "it publishes status data as per the configured timings" {
        // Given
        val publishDuration = Duration.parse("50ms")
        val intergasServiceMock = getMock(intergasService)
        every { intergasServiceMock.getStatusData() } returns Mono.just(StatusDataTestFixtures.BASIC)

        val numberOfEmissionsToWaitFor = 5

        // When
        val statusDataList = lan2RfRepository
            .getStatusData()
            .take(numberOfEmissionsToWaitFor.toLong())
            .doOnNext({ logger.info("StatusData received.") })
            .doOnComplete({ logger.info("Take until predicate time limit reached.") })
            .collectList()
            // Add 1 extra publish duration to the block period for good measure.
            .block((publishDuration * (numberOfEmissionsToWaitFor + 1)).toJavaDuration())!!

        // Then
        statusDataList.size.shouldBe(numberOfEmissionsToWaitFor)
        statusDataList.forEach({ it.shouldBe(StatusDataTestFixtures.BASIC) })
    }

}){
    @MockBean(LAN2RFConfiguration::class)
    fun lan2rfConfiguration(): LAN2RFConfiguration {
        val mock = mockk<LAN2RFConfiguration>()
        every { mock.checkInterval } returns Duration.parse("50ms").toJavaDuration()
        return mock
    }

    @MockBean(IntergasService::class)
    fun intergasService(): IntergasService = mockk<IntergasService>()

    companion object {
        val logger = LoggerFactory.getLogger(LAN2RFPublisherTest::class.java)
    }
}
