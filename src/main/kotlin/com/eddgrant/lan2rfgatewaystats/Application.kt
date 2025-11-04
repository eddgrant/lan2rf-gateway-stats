package com.eddgrant.lan2rfgatewaystats

import com.eddgrant.lan2rfgatewaystats.intergas.LAN2RFRepository
import com.eddgrant.lan2rfgatewaystats.persistence.influxdb.StatusDataPublisher
import io.micronaut.context.annotation.Context
import io.micronaut.runtime.Micronaut.run
import jakarta.annotation.PostConstruct
import jakarta.annotation.PreDestroy
import jakarta.inject.Singleton
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import reactor.core.Disposable

@Singleton
@Context
class LAN2RFGatewayStatsApp(
    private val laN2RFRepository: LAN2RFRepository,
    private val statusDataPublisher: StatusDataPublisher
) {
    private lateinit var disposable: Disposable

    @PostConstruct
    fun start() {
        LOGGER.info("Starting subscription to LAN2RF data.")
        disposable = statusDataPublisher.publishAsDiscreteMeasurements(
            laN2RFRepository.getStatusData()
        ).subscribe()
        LOGGER.info("Subscription to LAN2RF data created.")
    }

    @PreDestroy
    fun stopSendingStatusData() {
        LOGGER.info("Shutdown signal received: Cancelling subscription to LAN2RF data.")
        if (this::disposable.isInitialized && !disposable.isDisposed) {
            disposable.dispose()
        }
    }

    companion object {
        val LOGGER: Logger = LoggerFactory.getLogger(LAN2RFGatewayStatsApp::class.java)
    }
}

fun main(args: Array<String>) {
    run(*args)
}
