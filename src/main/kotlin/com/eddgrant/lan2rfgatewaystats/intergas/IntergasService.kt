package com.eddgrant.lan2rfgatewaystats.intergas

import io.micronaut.serde.ObjectMapper
import jakarta.inject.Inject
import jakarta.inject.Singleton
import reactor.core.publisher.Mono

@Singleton
class IntergasService(
    @Inject private val laN2RFClient: LAN2RFClient,
    @Inject private val objectMapper: ObjectMapper
) {
    fun getStatusData(): Mono<StatusData> {
        return laN2RFClient.getStatusData()
            .map {
                objectMapper.readValue(
                    it.body(),
                    StatusData::class.java
                )
            }
    }
}