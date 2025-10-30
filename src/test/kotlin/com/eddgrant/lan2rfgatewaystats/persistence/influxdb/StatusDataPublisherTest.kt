
package com.eddgrant.lan2rfgatewaystats.persistence.influxdb

import com.eddgrant.lan2rfgatewaystats.intergas.LAN2RFConfiguration
import com.eddgrant.lan2rfgatewaystats.intergas.StatusDataTestFixtures
import com.influxdb.client.domain.WritePrecision
import com.influxdb.client.kotlin.InfluxDBClientKotlin
import com.influxdb.client.kotlin.WriteKotlinApi
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Test
import reactor.core.publisher.Flux
import reactor.test.StepVerifier

class StatusDataPublisherTest {

    private val influxDBClientKotlin = mockk<InfluxDBClientKotlin>()
    private val lan2RFConfiguration = mockk<LAN2RFConfiguration>()
    private val underTest = StatusDataPublisher(influxDBClientKotlin, lan2RFConfiguration)

    @Test
    fun `Failure to write measurements to InfluxDB is propagated back to the caller`() {
        // Given
        val statusData = StatusDataTestFixtures.BASIC
        val statusDataFlux = Flux.just(statusData)

        val writeApi = mockk<WriteKotlinApi>()
        every { influxDBClientKotlin.getWriteKotlinApi() } returns writeApi
        coEvery { writeApi.writeMeasurements(any<Set<Any>>(), any<WritePrecision>()) } throws RuntimeException("Write failed")
        every { lan2RFConfiguration.source } returns "test"

        // When
        val result = underTest.publishAsDiscreteMeasurements(statusDataFlux)

        // Then
        StepVerifier.create(result)
                    .expectError(RuntimeException::class.java)
                    .verify()
    }
}
