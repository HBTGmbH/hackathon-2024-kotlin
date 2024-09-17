package de.hbt.routing.gti

import de.hbt.geofox.gti.model.*
import org.springframework.stereotype.Service
import org.springframework.web.client.RestClient

private const val GTI_PREFIX = "https://gti.geofox.de/gti/public"

@Service
open class GTIService(private val restClient: RestClient) {

    fun getRoute(start: SDName, destination: SDName, dateTime: GTITime): GRResponse {
        val request = GRRequest(
                start = start,
                dest = destination,
                time = dateTime,
                schedulesBefore = 0,
                schedulesAfter = 0,
                version = 58
        )
        val result = restClient.post()
                .uri("$GTI_PREFIX/getRoute")
                .body(request)
                .retrieve()
        return result.body(GRResponse::class.java)!!
    }

    fun init(): InitResponse {
        val request = InitRequest()
        val result = restClient.post()
                .uri("$GTI_PREFIX/init")
                .body(request)
                .retrieve()
        return result.body(InitResponse::class.java)!!
    }
}