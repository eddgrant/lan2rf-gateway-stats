package com.eddgrant.lan2rfgatewaystats.intergas

import jakarta.inject.Singleton
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import reactor.core.publisher.Flux

@Singleton
class StatusDataPublisher(
    private val intergasService: IntergasService,
    private val laN2RFConfiguration: LAN2RFConfiguration,
) {

    fun publishStatusData() : Flux<StatusData> {
        return Flux.interval(laN2RFConfiguration.checkInterval)
            .flatMap { intergasService.getStatusData() }
    }

    companion object {
        val LOGGER: Logger = LoggerFactory.getLogger(StatusDataPublisher::class.java)
    }
}