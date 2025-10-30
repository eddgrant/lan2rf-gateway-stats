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
import java.util.concurrent.CountDownLatch
import kotlin.time.Duration
import kotlin.time.toJavaDuration


@MicronautTest(environments = ["integration-test"])
class LAN2RFPublisherTest(
    private val intergasService: IntergasService,
    private val laN2RFConfiguration: LAN2RFConfiguration,
    private val lan2RfRepository: LAN2RFRepository,
) : StringSpec({

    "it publishes status data as per the configured timings" {
        // Given
        val publishDuration = Duration.parse("50ms")
        val laN2RFConfigurationMock = getMock(laN2RFConfiguration)
        every { laN2RFConfigurationMock.checkInterval } returns publishDuration.toJavaDuration()

        val intergasServiceMock = getMock(intergasService)
        every { intergasServiceMock.getStatusData() } returns Mono.just(StatusDataTestFixtures.BASIC)

        val numberOfEmissionsToWaitFor = 5
        val countdownLatch = CountDownLatch(numberOfEmissionsToWaitFor)
        val takeUntilPredicate = { _: StatusData ->
            countdownLatch.countDown()
            logger.info("Latch decremented by 1.")
            val numberOfRemainingValuesToConsume = countdownLatch.count
            if(numberOfRemainingValuesToConsume == 0L) {
                logger.info("Signalling consumer to take no more data.")
                true
            }
            else {
                false
            }
        }

        // When
        val statusDataList = lan2RfRepository
            .getStatusData()
            .takeUntil(takeUntilPredicate)
            .doOnNext({ logger.info("StatusData received.") })
            .doOnComplete({ logger.info("Take until predicate time limit reached.") })
            .collectList()
            // Add 1 extra publish duration to the block period for good measure.
            .block((publishDuration * (numberOfEmissionsToWaitFor + 1)).toJavaDuration())!!

        // Then
        statusDataList.size.shouldBe(numberOfEmissionsToWaitFor)
        statusDataList.forEach({ it.shouldBe(StatusDataTestFixtures.BASIC)})
    }

}){
    @MockBean(LAN2RFConfiguration::class)
    fun lan2rfConfiguration(): LAN2RFConfiguration = mockk<LAN2RFConfiguration>()

    @MockBean(IntergasService::class)
    fun intergasService(): IntergasService = mockk<IntergasService>()

    companion object {
        val logger = LoggerFactory.getLogger(LAN2RFPublisherTest::class.java)
    }
}
