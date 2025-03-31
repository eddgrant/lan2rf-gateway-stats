package com.eddgrant.lan2rfgatewaystats.intergas

import io.micronaut.context.annotation.ConfigurationProperties
import jakarta.validation.constraints.NotBlank
import java.time.Duration
import kotlin.time.toJavaDuration
import kotlin.time.Duration as KotlinDuration

@ConfigurationProperties("lan2rf")
class LAN2RFConfiguration {

    @NotBlank
    var source : String = LAN2RF_DEFAULT_SOURCE_NAME

    @NotBlank
    var room1Name: String = ROOM_1_DEFAULT_NAME

    @NotBlank
    var room2Name: String = ROOM_2_DEFAULT_NAME

    var checkInterval: Duration = DEFAULT_CHECK_INTERVAL

    companion object {
        const val LAN2RF_DEFAULT_SOURCE_NAME = "lan2rf"
        const val ROOM_1_DEFAULT_NAME = "room1_default_name"
        const val ROOM_2_DEFAULT_NAME = "room2_default_name"
        val DEFAULT_CHECK_INTERVAL = KotlinDuration.parse("1m").toJavaDuration()
    }
}