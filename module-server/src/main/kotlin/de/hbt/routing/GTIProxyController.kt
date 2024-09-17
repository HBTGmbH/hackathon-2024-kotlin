package de.hbt.routing

import de.hbt.routing.service.GTIWrapper
import org.springframework.http.HttpEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.client.RestTemplate

@RestController
open class GTIProxyController(private val restTemplate: RestTemplate,
                              private val gtiWrapper: GTIWrapper) {

    @PostMapping(path = ["/gti/{requestMethod}"])
    fun requestGti(@PathVariable("requestMethod") method: String, @RequestBody body: String, @RequestHeader("api-key") key: String): String {
        val entity: HttpEntity<String> = gtiWrapper.sign(body, key)
        val result = restTemplate.postForEntity("https://gti.geofox.de/gti/public/${method}", entity, String::class.java)
        return result.body ?: ""
    }
}