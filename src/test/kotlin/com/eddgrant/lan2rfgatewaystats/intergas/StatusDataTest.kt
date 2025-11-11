package com.eddgrant.lan2rfgatewaystats.intergas

import io.kotest.core.spec.style.StringSpec
import io.micronaut.serde.ObjectMapper
import io.micronaut.test.extensions.kotest5.annotation.MicronautTest
import org.junit.jupiter.api.Assertions.*

@MicronautTest(environments = ["lan2rf-integration-test"])
class StatusDataTest(
    private val objectMapper: ObjectMapper,
) : StringSpec({

    val statusData = StatusDataTestFixtures.BASIC

    /**
     * Converts a temperature to the corresponding MSB and LSB values.
     * The temperature must be expressable as 2 bytes. The MSB represents
     * the integer value before the decimal point, the LSB represents the
     * decimal point.
     */
    fun convertTemperatureDoubleToMsbAndLsb(number: Double) = (100 * number)
        .toInt()
        .toString(2)
        .padStart(16, '0')
        .chunked(8)
        .map { it.toInt(2) }

    "it calculates the central heating temperature" {
        assertEquals(26.19, statusData.centralHeatingTemperature())
        val msbAndLsb = convertTemperatureDoubleToMsbAndLsb(26.19)
        assertEquals(10, msbAndLsb.first())
        assertEquals(59, msbAndLsb.last())
    }

    "it calculates the tap temperature" {
        assertEquals(27.76, statusData.tapTemperature())
    }

    "it calculates the central heating pressure" {
        assertEquals(1.56, statusData.centralHeatingPressure())
    }

    "it calculates room 1's temperature" {
        assertEquals(19.50, statusData.room1Temperature())
    }

    "it calculates room 1's temperature setpoint" {
        assertEquals(20.0, statusData.room1TemperatureSetpoint())
    }

    "it calculates room 1's temperature setpoint override" {
        assertEquals(23.5, statusData.room1TemperatureSetpointOverride())
    }

    "it calculates room 2's temperature" {
        assertEquals(21.50, statusData.room2Temperature())
    }

    "it calculates room 2's temperature setpoint" {
        assertEquals(18.0, statusData.room2TemperatureSetpoint())
    }


    "it calculates room 2's temperature setpoint override" {
        assertEquals(25.5, statusData.room2TemperatureSetpointOverride())
    }

    // IO tests
    "it determines when the the boiler is not in a locked out state" {
        assertFalse(statusData.isLockedOut())
    }

    "it determines when the the boiler is in a locked out state" {
        val lockedOut = statusData.copy(io = 1)
        assertTrue(lockedOut.isLockedOut())
    }

    "it determines when the the pump is active" {
        val pumpActive = statusData.copy(io = 2)
        assertTrue(pumpActive.isPumpActive())
    }

    "it determines when the the tap function is active" {
        val tapFunctionActive = statusData.copy(io = 4)
        assertTrue(tapFunctionActive.isTapFunctionActive())
    }

    "it determines when the the burner is active" {
        val burnerActive = statusData.copy(io = 8)
        assertTrue(burnerActive.isBurnerActive())
    }

    /**
     * It might not be possible for _all_ IO states to be active at once,
     * as some states might be mutually exclusive e.g. tap function + burner,
     * but I think it is possible for more than 1 to be e.g. pump + burner.
     */
    "it determines when all IO are active" {
        val burnerActive = statusData.copy(io = 15)
        assertTrue(burnerActive.isLockedOut())
        assertTrue(burnerActive.isPumpActive())
        assertTrue(burnerActive.isTapFunctionActive())
        assertTrue(burnerActive.isBurnerActive())
    }
    // End of IO tests

    "it displays the correct status display code" {
        assertEquals(statusData.copy(statusDisplayCode = 0).getStatusDisplayCode(), StatusData.STATUS_TEXT_OPEN_THERM)
        assertEquals(statusData.copy(statusDisplayCode = 15).getStatusDisplayCode(), StatusData.STATUS_TEXT_BOILER_EXTERNAL)
        assertEquals(statusData.copy(statusDisplayCode = 24).getStatusDisplayCode(), StatusData.STATUS_TEXT_FROST)
        assertEquals(statusData.copy(statusDisplayCode = 37).getStatusDisplayCode(), StatusData.STATUS_TEXT_CENTRAL_HEATING_RF)
        assertEquals(statusData.copy(statusDisplayCode = 51).getStatusDisplayCode(), StatusData.STATUS_TEXT_TAPWATER_INTERNAL)
        assertEquals(statusData.copy(statusDisplayCode = 85).getStatusDisplayCode(), StatusData.STATUS_TEXT_SENSOR_TEST)
        assertEquals(statusData.copy(statusDisplayCode = 102).getStatusDisplayCode(), StatusData.STATUS_TEXT_CENTRAL_HEATING)
        assertEquals(statusData.copy(statusDisplayCode = 126).getStatusDisplayCode(), StatusData.STATUS_TEXT_STANDBY)
        assertEquals(statusData.copy(statusDisplayCode = 153).getStatusDisplayCode(), StatusData.STATUS_TEXT_POSTRUN_BOILER)
        assertEquals(statusData.copy(statusDisplayCode = 170).getStatusDisplayCode(), StatusData.STATUS_TEXT_SERVICE)
        assertEquals(statusData.copy(statusDisplayCode = 204).getStatusDisplayCode(), StatusData.STATUS_TEXT_TAP_WATER)
        assertEquals(statusData.copy(statusDisplayCode = 231).getStatusDisplayCode(), StatusData.STATUS_TEXT_POSTRUN_CENTRAL_HEATING)
        assertEquals(statusData.copy(statusDisplayCode = 240).getStatusDisplayCode(), StatusData.STATUS_TEXT_BOILER_INTERNAL)
        assertEquals(statusData.copy(statusDisplayCode = 255).getStatusDisplayCode(), StatusData.STATUS_TEXT_BUFFER)
        assertEquals(statusData.copy(statusDisplayCode = 12345).getStatusDisplayCode(), "Unknown")
    }

    "it can serialise a JSON object from a JSON string" {
        // This is an actual JSON response from the /data.json endpoint of a LAN2RF
        val jsonString = """
            {
                "nodenr": 200,
                "ch_temp_lsb": 124,
                "ch_temp_msb": 32,
                "tap_temp_lsb": 128,
                "tap_temp_msb": 19,
                "ch_pressure_lsb": 162,
                "ch_pressure_msb": 0,
                "room_temp_1_lsb": 208,
                "room_temp_1_msb": 7,
                "room_temp_set_1_lsb": 208,
                "room_temp_set_1_msb": 7,
                "room_temp_2_lsb": 255,
                "room_temp_2_msb": 127,
                "room_temp_set_2_lsb": 255,
                "room_temp_set_2_msb": 127,
                "displ_code": 126,
                "IO": 0,
                "serial_year": 18,
                "serial_month": 4,
                "serial_line": 15,
                "serial_sn1": 0,
                "serial_sn2": 70,
                "serial_sn3": 63,
                "room_set_ovr_1_msb": 0,
                "room_set_ovr_1_lsb": 0,
                "room_set_ovr_2_msb": 0,
                "room_set_ovr_2_lsb": 0,
                "rf_message_rssi": 31,
                "rfstatus_cntr": 0
            }
        """.trimIndent()

        val serialisedStatusData = objectMapper.readValue(jsonString, StatusData::class.java)

        assertNotNull(serialisedStatusData)
        assertEquals(200, serialisedStatusData.nodenr)
        assertEquals(124, serialisedStatusData.centralHeatingTemperatureLsb)
        assertEquals(32, serialisedStatusData.centralHeatingTemperatureMsb)
        assertEquals(128, serialisedStatusData.tapTemperatureLsb)
        assertEquals(19, serialisedStatusData.tapTemperatureMsb)
        assertEquals(162, serialisedStatusData.centralHeatingPressureLsb)
        assertEquals(0, serialisedStatusData.centralHeatingPressureMsb)
        assertEquals(208, serialisedStatusData.room1TemperatureLsb)
        assertEquals(7, serialisedStatusData.room1TemperatureMsb)
        assertEquals(208, serialisedStatusData.room1TemperatureLsb)
        assertEquals(7, serialisedStatusData.room1TemperatureSetpointMsb)
        assertEquals(255, serialisedStatusData.room2Temperaturelsb)
        assertEquals(127, serialisedStatusData.room2TemperatureMsb)
        assertEquals(255, serialisedStatusData.room2TemperatureSetpointLsb)
        assertEquals(127, serialisedStatusData.room2TemperatureSetpointMsb)
        assertEquals(126, serialisedStatusData.statusDisplayCode)
        assertEquals(0, serialisedStatusData.io)
        assertEquals(18, serialisedStatusData.serialYear)
        assertEquals(4, serialisedStatusData.serialMonth)
        assertEquals(15, serialisedStatusData.serialLine)
        assertEquals(0, serialisedStatusData.serialSn1)
        assertEquals(70, serialisedStatusData.serialSn2)
        assertEquals(63, serialisedStatusData.serialSn3)
        assertEquals(0, serialisedStatusData.room1TemperatureSetpointOverrideMsb)
        assertEquals(0, serialisedStatusData.room1TemperatureSetpointOverrideLsb)
        assertEquals(0, serialisedStatusData.room2TemperatureSetpointOverrideMsb)
        assertEquals(0, serialisedStatusData.room2TemperatureSetpointOverrideLsb)
        assertEquals(31, serialisedStatusData.rfMessageRssi)
        assertEquals(0, serialisedStatusData.rfstatusCntr)
    }
})