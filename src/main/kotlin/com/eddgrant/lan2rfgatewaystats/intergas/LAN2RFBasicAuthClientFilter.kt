package com.eddgrant.lan2rfgatewaystats.intergas

import io.micronaut.http.MutableHttpRequest
import io.micronaut.http.annotation.ClientFilter
import io.micronaut.http.annotation.RequestFilter

@ClientFilter(serviceId = ["lan2rf"])
class LAN2RFBasicAuthClientFilter(private val config: LAN2RFConfiguration) {

    @RequestFilter
    fun addBasicAuth(request: MutableHttpRequest<*>) {
        if (config.isBasicAuthEnabled()) {
            request.basicAuth(config.basicAuth.username!!, config.basicAuth.password!!)
        }
    }
}
