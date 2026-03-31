package com.eddgrant.lan2rfgatewaystats.intergas

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.micronaut.serde.ObjectMapper
import io.micronaut.test.extensions.kotest5.annotation.MicronautTest

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
        statusData.centralHeatingTemperature() shouldBe 26.19
        val msbAndLsb = convertTemperatureDoubleToMsbAndLsb(26.19)
        msbAndLsb.first() shouldBe 10
        msbAndLsb.last() shouldBe 59
    }

    "it calculates the tap temperature" {
        statusData.tapTemperature() shouldBe 27.76
    }

    "it calculates the central heating pressure" {
        statusData.centralHeatingPressure() shouldBe 1.56
    }

    "it calculates room 1's temperature" {
        statusData.room1Temperature() shouldBe 19.50
    }

    "it calculates room 1's temperature setpoint" {
        statusData.room1TemperatureSetpoint() shouldBe 20.0
    }

    "it calculates room 1's temperature setpoint override" {
        statusData.room1TemperatureSetpointOverride() shouldBe 23.5
    }

    "it calculates room 2's temperature" {
        statusData.room2Temperature() shouldBe 21.50
    }

    "it calculates room 2's temperature setpoint" {
        statusData.room2TemperatureSetpoint() shouldBe 18.0
    }


    "it calculates room 2's temperature setpoint override" {
        statusData.room2TemperatureSetpointOverride() shouldBe 25.5
    }

    // IO tests
    "it determines when the the boiler is not in a locked out state" {
        statusData.isLockedOut() shouldBe false
    }

    "it determines when the the boiler is in a locked out state" {
        val lockedOut = statusData.copy(io = 1)
        lockedOut.isLockedOut() shouldBe true
    }

    "it determines when the the pump is active" {
        val pumpActive = statusData.copy(io = 2)
        pumpActive.isPumpActive() shouldBe true
    }

    "it determines when the the tap function is active" {
        val tapFunctionActive = statusData.copy(io = 4)
        tapFunctionActive.isTapFunctionActive() shouldBe true
    }

    "it determines when the the burner is active" {
        val burnerActive = statusData.copy(io = 8)
        burnerActive.isBurnerActive() shouldBe true
    }

    /**
     * It might not be possible for _all_ IO states to be active at once,
     * as some states might be mutually exclusive e.g. tap function + burner,
     * but I think it is possible for more than 1 to be e.g. pump + burner.
     */
    "it determines when all IO are active" {
        val burnerActive = statusData.copy(io = 15)
        burnerActive.isLockedOut() shouldBe true
        burnerActive.isPumpActive() shouldBe true
        burnerActive.isTapFunctionActive() shouldBe true
        burnerActive.isBurnerActive() shouldBe true
    }
    // End of IO tests

    "it displays the correct status display code" {
        statusData.copy(statusDisplayCode = 0).getStatusDisplayCode() shouldBe StatusData.STATUS_TEXT_OPEN_THERM
        statusData.copy(statusDisplayCode = 15).getStatusDisplayCode() shouldBe StatusData.STATUS_TEXT_BOILER_EXTERNAL
        statusData.copy(statusDisplayCode = 24).getStatusDisplayCode() shouldBe StatusData.STATUS_TEXT_FROST
        statusData.copy(statusDisplayCode = 37).getStatusDisplayCode() shouldBe StatusData.STATUS_TEXT_CENTRAL_HEATING_RF
        statusData.copy(statusDisplayCode = 51).getStatusDisplayCode() shouldBe StatusData.STATUS_TEXT_TAPWATER_INTERNAL
        statusData.copy(statusDisplayCode = 85).getStatusDisplayCode() shouldBe StatusData.STATUS_TEXT_SENSOR_TEST
        statusData.copy(statusDisplayCode = 102).getStatusDisplayCode() shouldBe StatusData.STATUS_TEXT_CENTRAL_HEATING
        statusData.copy(statusDisplayCode = 126).getStatusDisplayCode() shouldBe StatusData.STATUS_TEXT_STANDBY
        statusData.copy(statusDisplayCode = 153).getStatusDisplayCode() shouldBe StatusData.STATUS_TEXT_POSTRUN_BOILER
        statusData.copy(statusDisplayCode = 170).getStatusDisplayCode() shouldBe StatusData.STATUS_TEXT_SERVICE
        statusData.copy(statusDisplayCode = 204).getStatusDisplayCode() shouldBe StatusData.STATUS_TEXT_TAP_WATER
        statusData.copy(statusDisplayCode = 231).getStatusDisplayCode() shouldBe StatusData.STATUS_TEXT_POSTRUN_CENTRAL_HEATING
        statusData.copy(statusDisplayCode = 240).getStatusDisplayCode() shouldBe StatusData.STATUS_TEXT_BOILER_INTERNAL
        statusData.copy(statusDisplayCode = 255).getStatusDisplayCode() shouldBe StatusData.STATUS_TEXT_BUFFER
        statusData.copy(statusDisplayCode = 12345).getStatusDisplayCode() shouldBe "Unknown"
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

        serialisedStatusData shouldNotBe null
        serialisedStatusData.nodenr shouldBe 200
        serialisedStatusData.centralHeatingTemperatureLsb shouldBe 124
        serialisedStatusData.centralHeatingTemperatureMsb shouldBe 32
        serialisedStatusData.tapTemperatureLsb shouldBe 128
        serialisedStatusData.tapTemperatureMsb shouldBe 19
        serialisedStatusData.centralHeatingPressureLsb shouldBe 162
        serialisedStatusData.centralHeatingPressureMsb shouldBe 0
        serialisedStatusData.room1TemperatureLsb shouldBe 208
        serialisedStatusData.room1TemperatureMsb shouldBe 7
        serialisedStatusData.room1TemperatureLsb shouldBe 208
        serialisedStatusData.room1TemperatureSetpointMsb shouldBe 7
        serialisedStatusData.room2TemperatureLsb shouldBe 255
        serialisedStatusData.room2TemperatureMsb shouldBe 127
        serialisedStatusData.room2TemperatureSetpointLsb shouldBe 255
        serialisedStatusData.room2TemperatureSetpointMsb shouldBe 127
        serialisedStatusData.statusDisplayCode shouldBe 126
        serialisedStatusData.io shouldBe 0
        serialisedStatusData.serialYear shouldBe 18
        serialisedStatusData.serialMonth shouldBe 4
        serialisedStatusData.serialLine shouldBe 15
        serialisedStatusData.serialSn1 shouldBe 0
        serialisedStatusData.serialSn2 shouldBe 70
        serialisedStatusData.serialSn3 shouldBe 63
        serialisedStatusData.room1TemperatureSetpointOverrideMsb shouldBe 0
        serialisedStatusData.room1TemperatureSetpointOverrideLsb shouldBe 0
        serialisedStatusData.room2TemperatureSetpointOverrideMsb shouldBe 0
        serialisedStatusData.room2TemperatureSetpointOverrideLsb shouldBe 0
        serialisedStatusData.rfMessageRssi shouldBe 31
        serialisedStatusData.rfstatusCntr shouldBe 0
    }
})