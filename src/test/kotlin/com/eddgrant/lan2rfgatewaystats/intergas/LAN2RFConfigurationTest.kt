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

    "The default measurements are enabled" {
        lan2rfConfiguration.measurements.boiler.shouldBe(true)
        lan2rfConfiguration.measurements.room1.shouldBe(true)
        lan2rfConfiguration.measurements.room2.shouldBe(true)
    }

    "Basic auth is disabled by default" {
        lan2rfConfiguration.basicAuth.username.shouldBe(null)
        lan2rfConfiguration.basicAuth.password.shouldBe(null)
        lan2rfConfiguration.isBasicAuthEnabled().shouldBe(false)
    }

    "Basic auth is disabled when only the username is set" {
        val config = LAN2RFConfiguration().apply {
            basicAuth.username = "admin"
        }
        config.isBasicAuthEnabled().shouldBe(false)
    }

    "Basic auth is disabled when only the password is set" {
        val config = LAN2RFConfiguration().apply {
            basicAuth.password = "s3cret"
        }
        config.isBasicAuthEnabled().shouldBe(false)
    }

    "Basic auth is disabled when username or password is blank" {
        val config = LAN2RFConfiguration().apply {
            basicAuth.username = "   "
            basicAuth.password = "s3cret"
        }
        config.isBasicAuthEnabled().shouldBe(false)
    }

    "Basic auth is enabled when both username and password are set" {
        val config = LAN2RFConfiguration().apply {
            basicAuth.username = "admin"
            basicAuth.password = "s3cret"
        }
        config.isBasicAuthEnabled().shouldBe(true)
    }
})
