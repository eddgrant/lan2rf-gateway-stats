package com.eddgrant.lan2rfgatewaystats.intergas

import io.kotest.core.spec.style.StringSpec
import io.micronaut.runtime.EmbeddedApplication
import io.micronaut.serde.ObjectMapper
import io.micronaut.test.extensions.kotest5.annotation.MicronautTest
import org.junit.jupiter.api.Assertions.*

@MicronautTest(environments = ["integration-test"])
class LAN2RFClientIntegrationTest(
    private val application: EmbeddedApplication<*>,
    private val objectMapper: ObjectMapper,
    private val client: LAN2RFClient
) : StringSpec({

    "it can obtain status data from the LAN2RF device" {
        val httpResponse = client.getStatusData().doOnError({
            print(it)
        }).block()!!
        assertEquals(httpResponse.status.code, 200)
        val statusDataString = httpResponse.body()
        val statusData = objectMapper.readValue(statusDataString, StatusData::class.java)
        assertNotNull(statusData)
    }

})