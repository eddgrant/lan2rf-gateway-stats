package com.eddgrant.lan2rfgatewaystats.intergas

import io.kotest.core.spec.style.StringSpec
import io.micronaut.test.annotation.MockBean
import io.micronaut.test.extensions.kotest5.MicronautKotest5Extension.getMock
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
        val intergasServiceMock = getMock(intergasService)
        every { intergasServiceMock.getStatusData() } returns Mono.just(StatusDataTestFixtures.BASIC)

        val lan2rfConfigurationMock = getMock(lan2rfConfiguration)
        every { lan2rfConfigurationMock.checkInterval } returns Duration.ofMillis(10)

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
    fun intergasService(): IntergasService = mockk<IntergasService>()

    @MockBean(LAN2RFConfiguration::class)
    fun lan2rfConfiguration(): LAN2RFConfiguration = mockk<LAN2RFConfiguration>()
}
