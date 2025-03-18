package com.eddgrant.lan2rfgatewaystats.intergas

class StatusDataTestFixtures {
    companion object {
        val BASIC = StatusData(
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
    }
}