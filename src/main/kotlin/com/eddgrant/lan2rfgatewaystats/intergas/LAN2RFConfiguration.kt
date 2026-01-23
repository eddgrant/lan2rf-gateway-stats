package com.eddgrant.lan2rfgatewaystats.intergas

import io.micronaut.context.annotation.ConfigurationProperties
import jakarta.annotation.PostConstruct
import jakarta.validation.constraints.NotBlank
import org.slf4j.LoggerFactory
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

    var measurements = Measurements()

    @ConfigurationProperties("measurements")
    data class Measurements(
        var boiler: Boolean = true,
        var room1: Boolean = true,
        var room2: Boolean = true
    )

    @PostConstruct
    fun logConfiguration() {
        LOGGER.info(
            "LAN2RF Configuration: source='{}', room1Name='{}', room2Name='{}', checkInterval='{}', measurements.boiler='{}', measurements.room1='{}', measurements.room2='{}'",
            source,
            room1Name,
            room2Name,
            checkInterval,
            measurements.boiler,
            measurements.room1,
            measurements.room2
        )
    }

    companion object {
        private val LOGGER = LoggerFactory.getLogger(LAN2RFConfiguration::class.java)

        const val LAN2RF_DEFAULT_SOURCE_NAME = "lan2rf"
        const val ROOM_1_DEFAULT_NAME = "room1"
        const val ROOM_2_DEFAULT_NAME = "room2"
        val DEFAULT_CHECK_INTERVAL = KotlinDuration.parse("1m").toJavaDuration()
    }
}