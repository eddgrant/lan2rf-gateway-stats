package com.eddgrant.lan2rfgatewaystats.intergas

import io.kotest.core.spec.style.StringSpec
import io.micronaut.runtime.EmbeddedApplication
import io.micronaut.serde.ObjectMapper
import io.micronaut.test.extensions.kotest5.annotation.MicronautTest
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe

@MicronautTest(environments = ["lan2rf-integration-test"])
class LAN2RFClientIntegrationTest(
    private val application: EmbeddedApplication<*>,
    private val objectMapper: ObjectMapper,
    private val client: LAN2RFClient
) : StringSpec({

    "it can obtain status data from the LAN2RF device" {
        val httpResponse = client.getStatusData().doOnError({
            print(it)
        }).block()!!
        httpResponse.status.code shouldBe 200
        val statusDataString = httpResponse.body()
        val statusData = objectMapper.readValue(statusDataString, StatusData::class.java)
        statusData shouldNotBe null
    }

})