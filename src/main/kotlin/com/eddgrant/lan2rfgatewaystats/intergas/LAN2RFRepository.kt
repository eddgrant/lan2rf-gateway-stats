package com.eddgrant.lan2rfgatewaystats.intergas

import io.micronaut.http.client.exceptions.HttpClientException
import io.micronaut.http.client.exceptions.HttpClientResponseException
import jakarta.inject.Singleton
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Singleton
class LAN2RFRepository(
    private val intergasService: IntergasService,
    private val laN2RFConfiguration: LAN2RFConfiguration,
) {

    fun getStatusData() : Flux<StatusData> {
        return Flux.interval(laN2RFConfiguration.checkInterval)
            .flatMap {
                intergasService.getStatusData()
                    .onErrorResume(HttpClientResponseException::class.java) { e ->
                        val code = e.status.code
                        if (code == 401 || code == 403) {
                            LOGGER.error(
                                "LAN2RF authentication failed (HTTP {}). " +
                                    "Check LAN2RF_BASIC_AUTH_USERNAME and LAN2RF_BASIC_AUTH_PASSWORD. " +
                                    "Will retry on next interval.",
                                code
                            )
                        } else {
                            LOGGER.error("LAN2RF returned HTTP {}. Will retry on next interval.", code, e)
                        }
                        Mono.empty()
                    }
                    .onErrorResume(HttpClientException::class.java) { e ->
                        LOGGER.error("Failed to get status data from LAN2RF device. Will retry on next interval.", e)
                        Mono.empty()
                    }
            }
            .doOnNext { LOGGER.debug("LAN2RF status data obtained") }
    }

    companion object {
        val LOGGER: Logger = LoggerFactory.getLogger(LAN2RFRepository::class.java)
    }
}
