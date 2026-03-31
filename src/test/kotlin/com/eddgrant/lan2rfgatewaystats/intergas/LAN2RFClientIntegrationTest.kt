package com.eddgrant.lan2rfgatewaystats.intergas

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock.aResponse
import com.github.tomakehurst.wiremock.client.WireMock.get
import com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo
import com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.micronaut.serde.ObjectMapper
import io.micronaut.test.extensions.kotest5.annotation.MicronautTest
import io.micronaut.test.support.TestPropertyProvider

@MicronautTest
class LAN2RFClientIntegrationTest(
    private val objectMapper: ObjectMapper,
    private val client: LAN2RFClient
) : StringSpec(), TestPropertyProvider {

    companion object {
        val wireMockServer = WireMockServer(wireMockConfig().dynamicPort()).apply { start() }

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

    override fun getProperties(): Map<String, String> = mapOf(
        "micronaut.http.services.lan2rf.urls[0]" to "http://localhost:${wireMockServer.port()}"
    )

    init {
        afterSpec {
            wireMockServer.stop()
        }

        "it retrieves and deserialises status data from the LAN2RF device" {
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
    }
}
