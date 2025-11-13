package com.eddgrant.lan2rfgatewaystats.intergas

import io.kotest.core.spec.style.StringSpec
import io.micronaut.http.client.exceptions.HttpClientException
import io.micronaut.test.annotation.MockBean
import io.micronaut.test.extensions.kotest5.annotation.MicronautTest
import io.mockk.every
import io.mockk.mockk
import reactor.core.publisher.Mono
import reactor.test.StepVerifier
import java.time.Duration

@MicronautTest(environments = ["lan2rf-integration-test"])
class LAN2RFRepositoryTest(
    private val intergasService: IntergasService,
    private val lan2rfConfiguration: LAN2RFConfiguration,
    private val lan2rfRepository: LAN2RFRepository
) : StringSpec({

    "it returns status data on a schedule" {
        // Given
        every { intergasService.getStatusData() } returns Mono.just(StatusDataTestFixtures.BASIC)

        // When / Then
        StepVerifier.withVirtualTime { lan2rfRepository.getStatusData() }
            .expectSubscription()
            .thenAwait(lan2rfConfiguration.checkInterval)
            .expectNext(StatusDataTestFixtures.BASIC)
            .thenCancel()
            .verify()
    }

    "it continues to poll for data when there is a connect exception" {
        // Given
        every { intergasService.getStatusData() } returnsMany listOf(
            Mono.error(HttpClientException("Connect Error")),
            Mono.just(StatusDataTestFixtures.BASIC)
        )

        // When / Then
        StepVerifier.withVirtualTime { lan2rfRepository.getStatusData() }
            .expectSubscription()
            // First tick. An error is emitted and then swallowed by the 'onErrorResume' operator.
            // The StepVerifier does not see the error and the stream does not complete.
            .thenAwait(lan2rfConfiguration.checkInterval)

            // Second tick. A value is successfully emitted.
            .thenAwait(lan2rfConfiguration.checkInterval)
            .expectNext(StatusDataTestFixtures.BASIC)
            .thenCancel()
            .verify()
    }

}) {
    @MockBean(IntergasService::class)
    fun intergasService(): IntergasService {
        return mockk<IntergasService>()
    }

    @MockBean(LAN2RFConfiguration::class)
    fun lan2rfConfiguration(): LAN2RFConfiguration {
        val mock = mockk<LAN2RFConfiguration>()
        every { mock.checkInterval } returns Duration.ofMillis(50)
        return mock
    }
}
