package com.eddgrant.lan2rfgatewaystats

import com.eddgrant.lan2rfgatewaystats.persistence.influxdb.StatusDataOrchestrator
import io.micronaut.context.event.ApplicationEventListener
import io.micronaut.runtime.Micronaut.run
import io.micronaut.runtime.server.event.ServerStartupEvent
import jakarta.annotation.PreDestroy
import jakarta.inject.Singleton
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.lang.Thread.sleep
import java.util.concurrent.atomic.AtomicBoolean

@Singleton
class LAN2RFGatewayStats(
    private val statusDataOrchestrator: StatusDataOrchestrator
) : ApplicationEventListener<ServerStartupEvent> {
    private val shutdownRequested = AtomicBoolean(false)

    override fun onApplicationEvent(event: ServerStartupEvent?) {
        LOGGER.info("Running...")
        statusDataOrchestrator.emitStatusData()
        while(!statusDataOrchestrator.disposable.isDisposed && !shutdownRequested.get()) {
            LOGGER.debug("Continuing subscription to LAN2RF data.")
            sleep(1000)
        }
        LOGGER.info("Subscription to LAN2RF data cancelled")
    }

    @PreDestroy
    fun onShutdown() {
        LOGGER.info("Shutdown signal received")
        shutdownRequested.set(true)
    }

    companion object {
        val LOGGER: Logger = LoggerFactory.getLogger(LAN2RFGatewayStats::class.java)
    }
}


fun main(args: Array<String>) {
    run(LAN2RFGatewayStats::class.java, *args)
}
