
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
        clearMocks(influxDBClientKotlin, lan2RFConfiguration, writeApi)
        every { lan2RFConfiguration.source } returns "test-source"
        every { lan2RFConfiguration.room1Name } returns "room1"
        every { lan2RFConfiguration.room2Name } returns "room2"
    }

    test("Successfully writes all measurements to InfluxDB when all measurements are enabled") {
        // Given
        every { lan2RFConfiguration.measurements } returns LAN2RFConfiguration.Measurements()
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
        val expectedMeasurements = buildExpectedBoilerMeasurements(statusData, lan2RFConfiguration.source) +
                buildExpectedRoom1Measurements(statusData, lan2RFConfiguration.source) +
                buildExpectedRoom2Measurements(statusData, lan2RFConfiguration.source)

        capturedMeasurements shouldHaveSize expectedMeasurements.size

        val comparableCaptured = capturedMeasurements.map { it.toComparable() }.toSet()
        val comparableExpected = expectedMeasurements.map { it.toComparable() }.toSet()
        comparableCaptured shouldContainExactlyInAnyOrder comparableExpected
    }

    test("Successfully writes only boiler measurements to InfluxDB when only boiler measurements are enabled") {
        // Given
        every { lan2RFConfiguration.measurements } returns LAN2RFConfiguration.Measurements(boiler = true, room1 = false, room2 = false)
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
        val expectedMeasurements = buildExpectedBoilerMeasurements(statusData, lan2RFConfiguration.source)

        capturedMeasurements shouldHaveSize expectedMeasurements.size

        val comparableCaptured = capturedMeasurements.map { it.toComparable() }.toSet()
        val comparableExpected = expectedMeasurements.map { it.toComparable() }.toSet()
        comparableCaptured shouldContainExactlyInAnyOrder comparableExpected
    }

    test("Uses configured room names in measurements") {
        // Given
        every { lan2RFConfiguration.room1Name } returns "living_room"
        every { lan2RFConfiguration.room2Name } returns "bedroom"
        every { lan2RFConfiguration.measurements } returns LAN2RFConfiguration.Measurements(boiler = false, room1 = true, room2 = true)
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
        val expectedMeasurements =
            buildExpectedRoom1Measurements(statusData, lan2RFConfiguration.source, "living_room") +
            buildExpectedRoom2Measurements(statusData, lan2RFConfiguration.source, "bedroom")

        capturedMeasurements shouldHaveSize expectedMeasurements.size

        val comparableCaptured = capturedMeasurements.map { it.toComparable() }.toSet()
        val comparableExpected = expectedMeasurements.map { it.toComparable() }.toSet()
        comparableCaptured shouldContainExactlyInAnyOrder comparableExpected
    }

    test("Failure to write measurements to InfluxDB is logged and the stream continues") {
        // Given
        every { lan2RFConfiguration.measurements } returns LAN2RFConfiguration.Measurements()
        val statusData = StatusDataTestFixtures.BASIC
        val statusDataFlux = Flux.just(statusData)

        every { influxDBClientKotlin.getWriteKotlinApi() } returns writeApi
        coEvery { writeApi.writeMeasurements(any<Set<Any>>(), any<WritePrecision>()) } throws RuntimeException("Write failed")

        // When
        val result = underTest.publishAsDiscreteMeasurements(statusDataFlux)

        // Then
        StepVerifier.create(result)
            .verifyComplete()
    }

    test("Stream continues processing after an InfluxDB write failure") {
        // Given
        every { lan2RFConfiguration.measurements } returns LAN2RFConfiguration.Measurements()
        val statusData = StatusDataTestFixtures.BASIC
        val statusDataFlux = Flux.just(statusData, statusData)

        every { influxDBClientKotlin.getWriteKotlinApi() } returns writeApi
        coEvery { writeApi.writeMeasurements(any<Set<Any>>(), any<WritePrecision>()) } throws RuntimeException("Write failed") andThen Unit

        // When
        val result = underTest.publishAsDiscreteMeasurements(statusDataFlux)

        // Then
        StepVerifier.create(result)
            .verifyComplete()

        coVerify(exactly = 2) {
            writeApi.writeMeasurements(any<Set<Any>>(), WritePrecision.MS)
        }
    }

})

private fun buildExpectedBoilerMeasurements(statusData: StatusData, source: String): Set<Any> {
    val now = Instant.now() // Ignored in comparison
    return setOf(
        Temperature(source, "central_heating", statusData.centralHeatingTemperature(), Temperature.Type.RECORDED, now),
        Temperature(source, "tap", statusData.tapTemperature(), Temperature.Type.RECORDED, now),

        Pressure(source, "central_heating", statusData.centralHeatingPressure(), now),

        TextStatus(source, STATUS_DISPLAY_CODE_NAME, statusData.getStatusDisplayCode(), now),

        OperationalStatus(source, IO_STATUS_NAME_LOCKED_OUT, statusData.isLockedOut(), now),
        OperationalStatus(source, IO_STATUS_NAME_PUMP_ACTIVE, statusData.isPumpActive(), now),
        OperationalStatus(source, IO_STATUS_NAME_TAP_FUNCTION_ACTIVE, statusData.isTapFunctionActive(), now),
        OperationalStatus(source, IO_STATUS_NAME_BURNER_ACTIVE, statusData.isBurnerActive(), now)
    )
}

private fun buildExpectedRoom1Measurements(statusData: StatusData, source: String, roomName: String = "room1"): Set<Any> {
    val now = Instant.now() // Ignored in comparison
    return setOf(
        Temperature(source, roomName, statusData.room1Temperature(), Temperature.Type.RECORDED, now),
        Temperature(source, roomName, statusData.room1TemperatureSetpoint(), Temperature.Type.SETPOINT, now),
        Temperature(source, roomName, statusData.room1TemperatureSetpointOverride(), Temperature.Type.SETPOINT_OVERRIDE, now)
    )
}

private fun buildExpectedRoom2Measurements(statusData: StatusData, source: String, roomName: String = "room2"): Set<Any> {
    val now = Instant.now() // Ignored in comparison
    return setOf(
        Temperature(source, roomName, statusData.room2Temperature(), Temperature.Type.RECORDED, now),
        Temperature(source, roomName, statusData.room2TemperatureSetpoint(), Temperature.Type.SETPOINT, now),
        Temperature(source, roomName, statusData.room2TemperatureSetpointOverride(), Temperature.Type.SETPOINT_OVERRIDE, now)
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
