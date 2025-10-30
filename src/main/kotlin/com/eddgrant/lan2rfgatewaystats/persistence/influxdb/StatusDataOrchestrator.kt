package com.eddgrant.lan2rfgatewaystats.persistence.influxdb

import com.eddgrant.lan2rfgatewaystats.intergas.LAN2RFRepository
import io.micronaut.context.annotation.Context
import jakarta.annotation.PreDestroy
import jakarta.inject.Singleton
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import reactor.core.Disposable

@Singleton
@Context
class StatusDataOrchestrator(
    private val laN2RFRepository: LAN2RFRepository,
    private val statusDataPublisher: StatusDataPublisher
) {
    lateinit var disposable: Disposable

    fun emitStatusData() {
        disposable = statusDataPublisher.publishStatusDataAsDiscreteMeasurements(
            laN2RFRepository.getStatusData()
        ).subscribe()
        LOGGER.info("Subscription to LAN2RF data created.")
    }

    @PreDestroy
    fun stopSendingStatusData() {
        LOGGER.info("Shutdown signal received: Cancelling subscription to LAN2RF data.")
        if(!disposable.isDisposed) {
            disposable.dispose()
        }
    }

    companion object {
        val LOGGER: Logger = LoggerFactory.getLogger(StatusDataOrchestrator::class.java)
    }
}