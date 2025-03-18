package com.eddgrant.lan2rfgatewaystats.intergas

import io.micronaut.context.annotation.ConfigurationProperties
import jakarta.validation.constraints.NotBlank

@ConfigurationProperties("lan2rf")
class LAN2RFConfiguration {

    @NotBlank
    var source : String = LAN2RF_DEFAULT_SOURCE_NAME

    @NotBlank
    var room1Name: String = ROOM_1_DEFAULT_NAME

    @NotBlank
    var room2Name: String = ROOM_2_DEFAULT_NAME

    companion object {
        val LAN2RF_DEFAULT_SOURCE_NAME = "lan2rf"
        val ROOM_1_DEFAULT_NAME = "room1_default_name"
        val ROOM_2_DEFAULT_NAME = "room2_default_name"
    }
}