package com.eddgrant.lan2rfgatewaystats.intergas

import jakarta.inject.Singleton
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import reactor.core.publisher.Flux

@Singleton
class LAN2RFRepository(
    private val intergasService: IntergasService,
    private val laN2RFConfiguration: LAN2RFConfiguration,
) {

    fun getStatusData() : Flux<StatusData> {
        return Flux.interval(laN2RFConfiguration.checkInterval)
            .flatMap { intergasService.getStatusData() }
            .doOnNext({LOGGER.info("LAN2RF status data obtained")})
    }

    companion object {
        val LOGGER: Logger = LoggerFactory.getLogger(LAN2RFRepository::class.java)
    }
}