package com.eddgrant.lan2rfgatewaystats

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.micronaut.health.HealthStatus
import io.micronaut.management.health.indicator.HealthResult
import io.mockk.every
import io.mockk.mockk
import reactor.core.publisher.Mono

class SubscriptionHealthIndicatorTest : StringSpec({

    "it reports UP when the subscription is active" {
        val app = mockk<LAN2RFGatewayStatsApp>()
        every { app.isSubscriptionActive() } returns true

        val indicator = SubscriptionHealthIndicator(app)
        val result = Mono.from(indicator.result).block()!!

        result.status shouldBe HealthStatus.UP
        @Suppress("UNCHECKED_CAST")
        val details = result.details as Map<String, Any>
        details["subscriptionActive"] shouldBe true
    }

    "it reports DOWN when the subscription is not active" {
        val app = mockk<LAN2RFGatewayStatsApp>()
        every { app.isSubscriptionActive() } returns false

        val indicator = SubscriptionHealthIndicator(app)
        val result = Mono.from(indicator.result).block()!!

        result.status shouldBe HealthStatus.DOWN
        @Suppress("UNCHECKED_CAST")
        val details = result.details as Map<String, Any>
        details["subscriptionActive"] shouldBe false
    }
})
