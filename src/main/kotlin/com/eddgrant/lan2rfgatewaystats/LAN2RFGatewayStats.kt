package com.eddgrant.lan2rfgatewaystats

import com.eddgrant.lan2rfgatewaystats.persistence.influxdb.StatusDataOrchestrator
import io.micronaut.runtime.Micronaut.run
import jakarta.annotation.PreDestroy
import jakarta.inject.Singleton
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.lang.Thread.sleep
import java.util.concurrent.atomic.AtomicBoolean

@Singleton
class LAN2RFGatewayStats(
    private val statusDataOrchestrator: StatusDataOrchestrator
) {
    private val shutdownRequested = AtomicBoolean(false)

    fun run() {
        statusDataOrchestrator.emitStatusData()
        while(!statusDataOrchestrator.disposable.isDisposed && !shutdownRequested.get()) {
            LOGGER.debug("Continuing subscription to LAN2RF data.")
            sleep(1000)
        }
    }

    @PreDestroy
    fun onShutdown() {
        LOGGER.info("Shutdown signal received: exiting loop.")
        shutdownRequested.set(true)
    }

    companion object {
        val LOGGER: Logger = LoggerFactory.getLogger(LAN2RFGatewayStats::class.java)
    }
}


fun main(args: Array<String>) {
    run(LAN2RFGatewayStats::class.java, *args)
}

