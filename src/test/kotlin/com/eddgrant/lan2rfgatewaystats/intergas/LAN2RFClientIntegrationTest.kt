package com.eddgrant.lan2rfgatewaystats.intergas

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.BasicCredentials
import com.github.tomakehurst.wiremock.client.WireMock.aResponse
import com.github.tomakehurst.wiremock.client.WireMock.get
import com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor
import com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo
import com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.micronaut.context.ApplicationContext
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.micronaut.serde.ObjectMapper
import io.micronaut.test.extensions.kotest5.annotation.MicronautTest

@MicronautTest
class LAN2RFClientIntegrationTest(
    private val objectMapper: ObjectMapper,
    private val client: LAN2RFClient
) : StringSpec({

    afterSpec {
        wireMockServer.stop()
    }

    "it retrieves and deserialises status data from the LAN2RF device" {
        wireMockServer.resetAll()
        wireMockServer.stubFor(
            get(urlEqualTo("/data.json"))
                .willReturn(
                    aResponse()
                        .withStatus(200)
                        .withBody(STATUS_DATA_JSON)
                )
        )

        val httpResponse = client.getStatusData().block()!!

        httpResponse.status.code shouldBe 200
        val statusDataString = httpResponse.body()
        val statusData = objectMapper.readValue(statusDataString, StatusData::class.java)
        statusData shouldNotBe null
        statusData.centralHeatingTemperatureMsb shouldBe 32
        statusData.centralHeatingTemperatureLsb shouldBe 124
        statusData.nodenr shouldBe 200
    }

    "it handles a response with no Content-Type header" {
        wireMockServer.resetAll()
        wireMockServer.stubFor(
            get(urlEqualTo("/data.json"))
                .willReturn(
                    aResponse()
                        .withStatus(200)
                        .withBody(STATUS_DATA_JSON)
                )
        )

        val httpResponse = client.getStatusData().block()!!

        httpResponse.status.code shouldBe 200
        val statusData = objectMapper.readValue(httpResponse.body(), StatusData::class.java)
        statusData shouldNotBe null
    }

    // --- Basic Auth --------------------------------------------------------
    //
    // The tests below exercise the HTTP Basic Auth client filter. They use
    // a second, manually-built Micronaut ApplicationContext configured with
    // `lan2rf.basic-auth.*` credentials rather than the `@MicronautTest`
    // context used above (which has no credentials configured, matching
    // the app's default behaviour). Both contexts share the same WireMock
    // server — each test calls `resetAll()` and installs its own stubs.

    "it sends basic auth credentials when both username and password are configured" {
        wireMockServer.resetAll()
        wireMockServer.stubFor(
            get(urlEqualTo("/data.json"))
                .withBasicAuth(BASIC_AUTH_USERNAME, BASIC_AUTH_PASSWORD)
                .willReturn(
                    aResponse()
                        .withStatus(200)
                        .withBody(STATUS_DATA_JSON)
                )
        )

        val httpResponse = basicAuthClient.getStatusData().block()!!

        httpResponse.status.code shouldBe 200
        val statusData = objectMapper.readValue(httpResponse.body(), StatusData::class.java)
        statusData shouldNotBe null
        statusData.nodenr shouldBe 200

        wireMockServer.verify(
            getRequestedFor(urlEqualTo("/data.json"))
                .withBasicAuth(BasicCredentials(BASIC_AUTH_USERNAME, BASIC_AUTH_PASSWORD))
        )
    }

    "it surfaces a 401 as an HttpClientResponseException when basic auth credentials are rejected" {
        wireMockServer.resetAll()
        wireMockServer.stubFor(
            get(urlEqualTo("/data.json"))
                .willReturn(aResponse().withStatus(401))
        )

        val ex = shouldThrow<HttpClientResponseException> {
            basicAuthClient.getStatusData().block()
        }
        ex.status.code shouldBe 401
    }
}) {
    companion object {
        val wireMockServer = WireMockServer(wireMockConfig().dynamicPort()).apply { start() }

        init {
            // Set before Micronaut context creation so the LAN2RF URL placeholder resolves
            System.setProperty("LAN2RF_URL", wireMockServer.baseUrl())
        }

        private const val BASIC_AUTH_USERNAME = "admin"
        private const val BASIC_AUTH_PASSWORD = "s3cret"

        /**
         * A second Micronaut context, configured with basic auth credentials,
         * pointed at the same WireMock server as the `@MicronautTest` context.
         * Used by the basic-auth test cases.
         */
        private val basicAuthContext: ApplicationContext by lazy {
            ApplicationContext.run(
                mapOf(
                    "micronaut.http.services.lan2rf.urls" to listOf(wireMockServer.baseUrl()),
                    "lan2rf.basic-auth.username" to BASIC_AUTH_USERNAME,
                    "lan2rf.basic-auth.password" to BASIC_AUTH_PASSWORD,
                    // Keep the background poller from competing with the explicit test calls.
                    "lan2rf.check-interval" to "1h"
                )
            )
        }

        val basicAuthClient: LAN2RFClient by lazy { basicAuthContext.getBean(LAN2RFClient::class.java) }

        val STATUS_DATA_JSON = """
            {
                "nodenr": 200,
                "ch_temp_lsb": 124,
                "ch_temp_msb": 32,
                "tap_temp_lsb": 128,
                "tap_temp_msb": 19,
                "ch_pressure_lsb": 162,
                "ch_pressure_msb": 0,
                "room_temp_1_lsb": 208,
                "room_temp_1_msb": 7,
                "room_temp_set_1_lsb": 208,
                "room_temp_set_1_msb": 7,
                "room_temp_2_lsb": 255,
                "room_temp_2_msb": 127,
                "room_temp_set_2_lsb": 255,
                "room_temp_set_2_msb": 127,
                "displ_code": 126,
                "IO": 0,
                "serial_year": 18,
                "serial_month": 4,
                "serial_line": 15,
                "serial_sn1": 0,
                "serial_sn2": 70,
                "serial_sn3": 63,
                "room_set_ovr_1_msb": 0,
                "room_set_ovr_1_lsb": 0,
                "room_set_ovr_2_msb": 0,
                "room_set_ovr_2_lsb": 0,
                "rf_message_rssi": 31,
                "rfstatus_cntr": 0
            }
        """.trimIndent()
    }
}
