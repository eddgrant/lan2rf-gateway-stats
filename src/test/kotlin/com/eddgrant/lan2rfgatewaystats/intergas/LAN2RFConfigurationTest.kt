package com.eddgrant.lan2rfgatewaystats.intergas

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

class LAN2RFConfigurationTest : StringSpec({

    val lan2rfConfiguration = LAN2RFConfiguration()

    "The default check interval is used" {
        lan2rfConfiguration.checkInterval.shouldBe(LAN2RFConfiguration.DEFAULT_CHECK_INTERVAL)
    }

    "The default source name is used" {
        lan2rfConfiguration.source.shouldBe(LAN2RFConfiguration.LAN2RF_DEFAULT_SOURCE_NAME)
    }

    "The default room1 name is used" {
        lan2rfConfiguration.room1Name.shouldBe(LAN2RFConfiguration.ROOM_1_DEFAULT_NAME)
    }

    "The default room2 name is used" {
        lan2rfConfiguration.room2Name.shouldBe(LAN2RFConfiguration.ROOM_2_DEFAULT_NAME)
    }
})
