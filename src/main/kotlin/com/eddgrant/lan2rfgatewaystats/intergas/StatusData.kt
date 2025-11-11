package com.eddgrant.lan2rfgatewaystats.intergas

import com.fasterxml.jackson.annotation.JsonProperty
import io.micronaut.serde.annotation.Serdeable

/**
 * The data and logic used in this class has been derived from
 * https://github.com/domoticz/domoticz/blob/532c505796d66b801b06d0a0dae23b00872ca5c2/hardware/InComfort.cpp
 */
@Serdeable
data class StatusData(
    @param:JsonProperty("nodenr") internal val nodenr: Int,
    @param:JsonProperty("ch_temp_msb") internal val centralHeatingTemperatureMsb: Int,
    @param:JsonProperty("ch_temp_lsb") internal val centralHeatingTemperatureLsb: Int,
    @param:JsonProperty("tap_temp_msb") internal val tapTemperatureMsb: Int,
    @param:JsonProperty("tap_temp_lsb") internal val tapTemperatureLsb: Int,
    @param:JsonProperty("ch_pressure_msb") internal val centralHeatingPressureMsb: Int,
    @param:JsonProperty("ch_pressure_lsb") internal val centralHeatingPressureLsb: Int,
    @param:JsonProperty("room_temp_1_msb") internal val room1TemperatureMsb: Int,
    @param:JsonProperty("room_temp_1_lsb") internal val room1TemperatureLsb: Int,
    @param:JsonProperty("room_temp_set_1_msb") internal val room1TemperatureSetpointMsb: Int,
    @param:JsonProperty("room_temp_set_1_lsb") internal val room1TemperatureSetpointLsb: Int,
    @param:JsonProperty("room_temp_2_msb") internal val room2TemperatureMsb: Int,
    @param:JsonProperty("room_temp_2_lsb") internal val room2Temperaturelsb: Int,
    @param:JsonProperty("room_temp_set_2_msb") internal val room2TemperatureSetpointMsb: Int,
    @param:JsonProperty("room_temp_set_2_lsb") internal val room2TemperatureSetpointLsb: Int,
    @param:JsonProperty("room_set_ovr_1_msb") internal val room1TemperatureSetpointOverrideMsb: Int,
    @param:JsonProperty("room_set_ovr_1_lsb") internal val room1TemperatureSetpointOverrideLsb: Int,
    @param:JsonProperty("room_set_ovr_2_msb") internal val room2TemperatureSetpointOverrideMsb: Int,
    @param:JsonProperty("room_set_ovr_2_lsb") internal val room2TemperatureSetpointOverrideLsb: Int,
    @param:JsonProperty("displ_code") internal val statusDisplayCode: Int,
    @param:JsonProperty("IO") internal val io: Int,
    @param:JsonProperty("serial_year") internal val serialYear: Int,
    @param:JsonProperty("serial_month") internal val serialMonth: Int,
    @param:JsonProperty("serial_line") internal val serialLine: Int,
    @param:JsonProperty("serial_sn1") internal val serialSn1: Int,
    @param:JsonProperty("serial_sn2") internal val serialSn2: Int,
    @param:JsonProperty("serial_sn3") internal val serialSn3: Int,
    @param:JsonProperty("rf_message_rssi") internal val rfMessageRssi: Int,
    @param:JsonProperty("rfstatus_cntr") internal val rfstatusCntr: Int
) {

    fun centralHeatingTemperature() : Double = combineMsbAndLsb(centralHeatingTemperatureMsb, centralHeatingTemperatureLsb)
    fun tapTemperature() : Double = combineMsbAndLsb(tapTemperatureMsb, tapTemperatureLsb)
    fun centralHeatingPressure(): Double = combineMsbAndLsb(centralHeatingPressureMsb, centralHeatingPressureLsb)
    fun room1Temperature(): Double = combineMsbAndLsb(room1TemperatureMsb, room1TemperatureLsb)
    fun room1TemperatureSetpoint(): Double = combineMsbAndLsb(room1TemperatureSetpointMsb, room1TemperatureSetpointLsb)
    fun room1TemperatureSetpointOverride(): Double = combineMsbAndLsb(room1TemperatureSetpointOverrideMsb, room1TemperatureSetpointOverrideLsb)
    fun room2Temperature(): Double = combineMsbAndLsb(room2TemperatureMsb, room2Temperaturelsb)
    fun room2TemperatureSetpoint(): Double = combineMsbAndLsb(room2TemperatureSetpointMsb, room2TemperatureSetpointLsb)
    fun room2TemperatureSetpointOverride(): Double = combineMsbAndLsb(room2TemperatureSetpointOverrideMsb, room2TemperatureSetpointOverrideLsb)
    fun isLockedOut(): Boolean = isBitSet(io, IO_STATUS_BIT_LOCKED_OUT)
    fun isPumpActive(): Boolean = isBitSet(io, IO_STATUS_BIT_PUMP_ACTIVE)
    fun isTapFunctionActive(): Boolean = isBitSet(io, IO_STATUS_BIT_TAP_FUNCTION_ACTIVE)
    fun isBurnerActive(): Boolean = isBitSet(io, IO_STATUS_BIT_BURNER_ACTIVE)
    fun getStatusDisplayCode(): String = statusDisplayCodes.getOrDefault(statusDisplayCode, "Unknown")

    private fun combineMsbAndLsb(msb: Int, lsb: Int) = ((msb shl 8) xor lsb) / 100.0
    private fun isBitSet(value: Int, bitIndex: Int) = ((value shr bitIndex) and 1) == 1

    companion object {
        const val STATUS_DISPLAY_CODE_NAME = "STATUS_DISPLAY_CODE"

        const val STATUS_TEXT_OPEN_THERM = "OpenTherm"
        const val STATUS_TEXT_BOILER_EXTERNAL = "Boiler External"
        const val STATUS_TEXT_FROST = "Frost"
        const val STATUS_TEXT_CENTRAL_HEATING_RF = "Central Heating RF"
        const val STATUS_TEXT_TAPWATER_INTERNAL = "Tapwater Internal"
        const val STATUS_TEXT_SENSOR_TEST = "Sensor Test"
        const val STATUS_TEXT_CENTRAL_HEATING = "Central Heating"
        const val STATUS_TEXT_STANDBY = "Standby"
        const val STATUS_TEXT_POSTRUN_BOILER = "Postrun Boiler"
        const val STATUS_TEXT_SERVICE = "Service"
        const val STATUS_TEXT_TAP_WATER = "Tap Water"
        const val STATUS_TEXT_POSTRUN_CENTRAL_HEATING = "Postrun Central Heating"
        const val STATUS_TEXT_BOILER_INTERNAL = "Boiler Internal"
        const val STATUS_TEXT_BUFFER = "Buffer"

        const val IO_STATUS_NAME_LOCKED_OUT = "LOCKED_OUT"
        const val IO_STATUS_NAME_PUMP_ACTIVE = "PUMP_ACTIVE"
        const val IO_STATUS_NAME_TAP_FUNCTION_ACTIVE = "TAP_FUNCTION_ACTIVE"
        const val IO_STATUS_NAME_BURNER_ACTIVE = "BURNER_ACTIVE"

        const val IO_STATUS_BIT_LOCKED_OUT = 0
        const val IO_STATUS_BIT_PUMP_ACTIVE = 1
        const val IO_STATUS_BIT_TAP_FUNCTION_ACTIVE = 2
        const val IO_STATUS_BIT_BURNER_ACTIVE = 3

        private val statusDisplayCodes: Map<Int, String> = mapOf(
            0 to STATUS_TEXT_OPEN_THERM,
            15 to STATUS_TEXT_BOILER_EXTERNAL,
            24 to STATUS_TEXT_FROST,
            37 to STATUS_TEXT_CENTRAL_HEATING_RF,
            51 to STATUS_TEXT_TAPWATER_INTERNAL,
            85 to STATUS_TEXT_SENSOR_TEST,
            102 to STATUS_TEXT_CENTRAL_HEATING,
            126 to STATUS_TEXT_STANDBY,
            153 to STATUS_TEXT_POSTRUN_BOILER,
            170 to STATUS_TEXT_SERVICE,
            204 to STATUS_TEXT_TAP_WATER,
            231 to STATUS_TEXT_POSTRUN_CENTRAL_HEATING,
            240 to STATUS_TEXT_BOILER_INTERNAL,
            255 to STATUS_TEXT_BUFFER,
        )
    }
}
