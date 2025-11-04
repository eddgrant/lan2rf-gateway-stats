package com.eddgrant.lan2rfgatewaystats.intergas

import io.kotest.core.spec.style.StringSpec
import io.micronaut.test.annotation.MockBean
import io.micronaut.test.extensions.kotest5.annotation.MicronautTest
import io.mockk.every
import io.mockk.mockk
import reactor.core.publisher.Mono
import reactor.test.StepVerifier
import kotlin.time.toJavaDuration

@MicronautTest(environments = ["lan2rf-integration-test"])
class LAN2RFRepositoryTest(
    private val intergasService: IntergasService,
    private val lan2rfConfiguration: LAN2RFConfiguration,
    private val lan2rfRepository: LAN2RFRepository
) : StringSpec({

    "it returns status data on a schedule" {
        // When
        val statusDataFlux = lan2rfRepository.getStatusData()

        // Then
        StepVerifier.create(statusDataFlux)
                    .expectNext(StatusDataTestFixtures.BASIC)
                    .thenCancel()
                    .verify()
    }

}) {
    @MockBean(IntergasService::class)
    fun intergasService(): IntergasService {
        val mock = mockk<IntergasService>()
        every { mock.getStatusData() } returns Mono.just(StatusDataTestFixtures.BASIC)
        return mock
    }

    @MockBean(LAN2RFConfiguration::class)
    fun lan2rfConfiguration(): LAN2RFConfiguration {
        val mock = mockk<LAN2RFConfiguration>()
        every { mock.checkInterval } returns kotlin.time.Duration.parse("50ms").toJavaDuration()
        return mock
    }
}
