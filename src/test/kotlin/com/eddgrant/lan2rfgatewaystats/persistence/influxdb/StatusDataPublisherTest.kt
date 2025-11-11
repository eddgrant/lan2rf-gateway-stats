
package com.eddgrant.lan2rfgatewaystats.persistence.influxdb

import com.eddgrant.lan2rfgatewaystats.intergas.LAN2RFConfiguration
import com.eddgrant.lan2rfgatewaystats.intergas.StatusData
import com.eddgrant.lan2rfgatewaystats.intergas.StatusData.Companion.IO_STATUS_NAME_BURNER_ACTIVE
import com.eddgrant.lan2rfgatewaystats.intergas.StatusData.Companion.IO_STATUS_NAME_LOCKED_OUT
import com.eddgrant.lan2rfgatewaystats.intergas.StatusData.Companion.IO_STATUS_NAME_PUMP_ACTIVE
import com.eddgrant.lan2rfgatewaystats.intergas.StatusData.Companion.IO_STATUS_NAME_TAP_FUNCTION_ACTIVE
import com.eddgrant.lan2rfgatewaystats.intergas.StatusData.Companion.STATUS_DISPLAY_CODE_NAME
import com.eddgrant.lan2rfgatewaystats.intergas.StatusDataTestFixtures
import com.influxdb.client.domain.WritePrecision
import com.influxdb.client.kotlin.InfluxDBClientKotlin
import com.influxdb.client.kotlin.WriteKotlinApi
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.collections.shouldHaveSize
import io.mockk.*
import reactor.core.publisher.Flux
import reactor.test.StepVerifier
import java.time.Instant

class StatusDataPublisherTest : FunSpec({

    val influxDBClientKotlin = mockk<InfluxDBClientKotlin>()
    val lan2RFConfiguration = mockk<LAN2RFConfiguration>()
    val writeApi = mockk<WriteKotlinApi>(relaxed = true)
    val underTest = StatusDataPublisher(influxDBClientKotlin, lan2RFConfiguration)

    beforeTest {
        every { lan2RFConfiguration.source } returns "test-source"
    }

    test("Successfully writes measurements to InfluxDB") {
        // Given
        val statusData = StatusDataTestFixtures.BASIC
        val statusDataFlux = Flux.just(statusData)

        every { influxDBClientKotlin.getWriteKotlinApi() } returns writeApi

        // When
        val result = underTest.publishAsDiscreteMeasurements(statusDataFlux)

        // Then
        StepVerifier.create(result)
                    .verifyComplete()

        val measurementsSlot = slot<Set<Any>>()
        coVerify(exactly = 1) {
            writeApi.writeMeasurements(capture(measurementsSlot), WritePrecision.MS)
        }

        val capturedMeasurements = measurementsSlot.captured
        val expectedMeasurements = buildExpectedMeasurements(statusData, lan2RFConfiguration.source)

        capturedMeasurements shouldHaveSize expectedMeasurements.size

        val comparableCaptured = capturedMeasurements.map { it.toComparable() }.toSet()
        val comparableExpected = expectedMeasurements.map { it.toComparable() }.toSet()
        comparableCaptured shouldContainExactlyInAnyOrder comparableExpected
    }

    test("Failure to write measurements to InfluxDB is propagated back to the caller") {
        // Given
        val statusData = StatusDataTestFixtures.BASIC
        val statusDataFlux = Flux.just(statusData)

        every { influxDBClientKotlin.getWriteKotlinApi() } returns writeApi
        coEvery { writeApi.writeMeasurements(any<Set<Any>>(), any<WritePrecision>()) } throws RuntimeException("Write failed")

        // When
        val result = underTest.publishAsDiscreteMeasurements(statusDataFlux)

        // Then
        StepVerifier.create(result)
                    .expectError(RuntimeException::class.java)
                    .verify()
    }

})

private fun buildExpectedMeasurements(statusData: StatusData, source: String): Set<Any> {
    val now = Instant.now() // Ignored in comparison
    return setOf(
        Temperature(source, "central_heating", statusData.centralHeatingTemperature(), Temperature.Type.RECORDED, now),
        Temperature(source, "room1", statusData.room1Temperature(), Temperature.Type.RECORDED, now),
        Temperature(source, "room2", statusData.room2Temperature(), Temperature.Type.RECORDED, now),
        Temperature(source, "tap", statusData.tapTemperature(), Temperature.Type.RECORDED, now),
        Temperature(source, "room1", statusData.room1TemperatureSetpoint(), Temperature.Type.SETPOINT, now),
        Temperature(source, "room1", statusData.room1TemperatureSetpointOverride(), Temperature.Type.SETPOINT_OVERRIDE, now),
        Temperature(source, "room2", statusData.room2TemperatureSetpoint(), Temperature.Type.SETPOINT, now),
        Temperature(source, "room2", statusData.room2TemperatureSetpointOverride(), Temperature.Type.SETPOINT_OVERRIDE, now),

        Pressure(source, "central_heating", statusData.centralHeatingPressure(), now),

        TextStatus(source, STATUS_DISPLAY_CODE_NAME, statusData.getStatusDisplayCode(), now),

        OperationalStatus(source, IO_STATUS_NAME_LOCKED_OUT, statusData.isLockedOut(), now),
        OperationalStatus(source, IO_STATUS_NAME_PUMP_ACTIVE, statusData.isPumpActive(), now),
        OperationalStatus(source, IO_STATUS_NAME_TAP_FUNCTION_ACTIVE, statusData.isTapFunctionActive(), now),
        OperationalStatus(source, IO_STATUS_NAME_BURNER_ACTIVE, statusData.isBurnerActive(), now)
    )
}

private fun Any.toComparable(): Any {
    val fixedTime = Instant.EPOCH
    return when (this) {
        is Temperature -> this.copy(time = fixedTime)
        is Pressure -> this.copy(time = fixedTime)
        is TextStatus -> this.copy(time = fixedTime)
        is OperationalStatus -> this.copy(time = fixedTime)
        else -> this
    }
}
