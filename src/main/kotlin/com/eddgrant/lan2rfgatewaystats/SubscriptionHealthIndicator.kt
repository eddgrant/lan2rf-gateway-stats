package com.eddgrant.lan2rfgatewaystats

import io.micronaut.health.HealthStatus
import io.micronaut.management.health.indicator.HealthIndicator
import io.micronaut.management.health.indicator.HealthResult
import jakarta.inject.Singleton
import org.reactivestreams.Publisher
import reactor.core.publisher.Mono

@Singleton
class SubscriptionHealthIndicator(
    private val app: LAN2RFGatewayStatsApp
) : HealthIndicator {

    override fun getResult(): Publisher<HealthResult> {
        val active = app.isSubscriptionActive()
        val status = if (active) HealthStatus.UP else HealthStatus.DOWN
        val details = mapOf("subscriptionActive" to active)
        return Mono.just(
            HealthResult.builder("subscription", status)
                .details(details)
                .build()
        )
    }
}
