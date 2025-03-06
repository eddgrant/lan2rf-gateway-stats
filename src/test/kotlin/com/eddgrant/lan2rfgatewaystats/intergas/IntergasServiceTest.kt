package com.eddgrant.lan2rfgatewaystats.intergas

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.micronaut.http.HttpResponse
import io.micronaut.http.HttpStatus
import io.micronaut.json.JsonSyntaxException
import io.micronaut.serde.ObjectMapper
import io.micronaut.test.annotation.MockBean
import io.micronaut.test.extensions.kotest5.MicronautKotest5Extension.getMock
import io.micronaut.test.extensions.kotest5.annotation.MicronautTest
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertEquals
import reactor.core.publisher.Mono

@MicronautTest
class IntergasServiceTest(
    private val objectMapper: ObjectMapper,
    private val lan2RFClient : LAN2RFClient,
    private val intergasService: IntergasService
) : StringSpec({

    "it returns status data" {
        val statusDataJsonString = """
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

        // Given
        val lan2RFClientMock = getMock(lan2RFClient)
        val httpResponse = mockk<HttpResponse<String>>()
        every { httpResponse.status() } returns HttpStatus.OK
        every { httpResponse.body() } returns statusDataJsonString
        every { lan2RFClientMock.getStatusData() } returns Mono.just(httpResponse)

        // When
        val statusData = intergasService.getStatusData().block()!!

        // Then
        assertEquals(statusData, objectMapper.readValue(statusDataJsonString, StatusData::class.java))
    }

    "it raises an exception when it cannot parse the status data object" {
        // Given
        val lan2RFClientMock = getMock(lan2RFClient)
        val httpResponse = mockk<HttpResponse<String>>()
        every { httpResponse.status() } returns HttpStatus.OK
        every { httpResponse.body() } returns "This is not valid JSON"
        every { lan2RFClientMock.getStatusData() } returns Mono.just(httpResponse)

        // When
        val exception = shouldThrow<RuntimeException> {
            intergasService.getStatusData().block()!!
        }

        // Then
        exception.cause!!.javaClass shouldBe JsonSyntaxException::class.java
    }

}) {
    @MockBean(LAN2RFClient::class)
    fun lan2RFClient(): LAN2RFClient = mockk<LAN2RFClient>()
}
