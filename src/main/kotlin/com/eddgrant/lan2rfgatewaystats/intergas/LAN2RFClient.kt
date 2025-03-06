package com.eddgrant.lan2rfgatewaystats.intergas

import io.micronaut.http.HttpResponse
import io.micronaut.http.annotation.Get
import io.micronaut.http.client.annotation.Client
import reactor.core.publisher.Mono

@Client("lan2rf")
interface LAN2RFClient {

    /**
     * The LAN2RF doesn't return a Content-Type response header,
     * which means that Micronaut isn't able to infer how to serialise the
     * response payload in to an object.
     *
     * https://github.com/micronaut-projects/micronaut-core/issues/5978
     *
     * Unfortunately this means that we get a null response object if we try to
     * specify some @Serdable type. Instead we have no option but to return a string
     * and do the serialisation manually, outside of the HTTP client.
     */
    @Get("/data.json")
    fun getStatusData() : Mono<HttpResponse<String>>
}