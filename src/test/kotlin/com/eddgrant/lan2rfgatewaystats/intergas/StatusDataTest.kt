package com.eddgrant.lan2rfgatewaystats.intergas

import io.kotest.core.spec.style.StringSpec
import io.micronaut.serde.ObjectMapper
import io.micronaut.test.extensions.kotest5.annotation.MicronautTest
import org.junit.jupiter.api.Assertions.*

/**
 * {
 *     "nodenr": 200,
 *     [DONE] "ch_temp_lsb": 59,
 *     [DONE] "ch_temp_msb": 10,
 *     [DONE] "tap_temp_lsb": 216,
 *     [DONE] "tap_temp_msb": 10,
 *     [DONE] "ch_pressure_lsb": 156,
 *     [DONE] "ch_pressure_msb": 0,
 *     [DONE] "room_temp_1_lsb": 208,
 *     [DONE] "room_temp_1_msb": 7,
 *     [DONE] "room_temp_set_1_lsb": 208,
 *     [DONE] "room_temp_set_1_msb": 7,
 *     [DONE] "room_temp_2_lsb": 255,
 *     [DONE] "room_temp_2_msb": 127,
 *     [DONE] "room_temp_set_2_lsb": 255,
 *     [DONE] "room_temp_set_2_msb": 127,
 *     [DONE] "displ_code": 231,
 *     "IO": 2,
 *     "serial_year": 18,
 *     "serial_month": 4,
 *     "serial_line": 15,
 *     "serial_sn1": 0,
 *     "serial_sn2": 70,
 *     "serial_sn3": 63,
 *     [DONE] "room_set_ovr_1_msb": 0,
 *     [DONE] "room_set_ovr_1_lsb": 0,
 *     [DONE] "room_set_ovr_2_msb": 0,
 *     [DONE] "room_set_ovr_2_lsb": 0,
 *     "rf_message_rssi": 37,
 *     "rfstatus_cntr": 0
 * }
 */

/**
 * Value = 5
 * 5 as binary = 0000 0000 00000101
 * MSB: 0000 0000
 * LSB: 00000101
 *
 * 2700
 * MSB: 00001010 (10)
 * LSB: 10001100 (140)
 *
 * MSB >> 8: 0000101000000000 (2560)
 * 2700 - 2560 =
 *
 * MSB | LSB = 0000101010001100
 */


@OptIn(ExperimentalUnsignedTypes::class)
@MicronautTest
class StatusDataTest(
    private val objectMapper: ObjectMapper,
    ) : StringSpec({

    val statusData = StatusData(
        nodenr = 200,
        centralHeatingTemperatureMsb = 10,
        centralHeatingTemperatureLsb = 59,
        tapTemperatureMsb = 10,
        tapTemperatureLsb = 216,
        centralHeatingPressureMsb = 0,
        centralHeatingPressureLsb = 156,
        room1TemperatureMsb = 7,
        room1TemperatureLsb = 158,
        room1TemperatureSetpointMsb = 7,
        room1TemperatureSetpointLsb = 208,
        room2TemperatureMsb = 8,
        room2Temperaturelsb = 102,
        room2TemperatureSetpointMsb = 7,
        room2TemperatureSetpointLsb = 8,
        statusDisplayCode = 231,
        io = 2,
        serialYear = 18,
        serialMonth = 4,
        serialLine = 15,
        serialSn1 = 0,
        serialSn2 = 70,
        serialSn3 = 63,
        room1TemperatureSetpointOverrideMsb = 9,
        room1TemperatureSetpointOverrideLsb = 46,
        room2TemperatureSetpointOverrideMsb = 9,
        room2TemperatureSetpointOverrideLsb = 246,
        rfMessageRssi = 37,
        rfstatusCntr = 0
    )

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

    "it determines when the the boiler is not in a locked out state" {
        assertFalse(statusData.isLockedOut())
    }

    "it determines when the the boiler is in a locked out state" {
        assertTrue(statusData.copy(io = 1).isLockedOut())
    }

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