package com.eddgrant.lan2rfgatewaystats.intergas

import io.micronaut.http.client.exceptions.HttpClientException
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
