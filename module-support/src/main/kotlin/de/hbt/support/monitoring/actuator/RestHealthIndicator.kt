package de.hbt.support.monitoring.actuator

import de.hbt.support.interceptor.HeaderInterceptorRest
import de.hbt.support.property.HealthCheckProperties
import org.springframework.boot.actuate.health.Health
import org.springframework.boot.actuate.health.HealthIndicator
import org.springframework.boot.actuate.health.Status
import org.springframework.boot.autoconfigure.web.ServerProperties
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate
import java.security.SecureRandom
import java.time.Clock

/**
 * A Health check which checks if the rest services are working.
 *
 *
 * If you have a complex service, you should think about an easy greeting or echo service, which
 * only tests the
 * network/service stack and not the full application.
 *
 *
 * The health check will be called by kubernetes to check if the container/pod should be in load
 * balancing. It is possible
 * to have as much health checks as you like.
 *
 *
 * There should be a health check which is ok not before all data is loaded.
 */
@Component
class RestHealthIndicator(
    clock: Clock, interceptor: HeaderInterceptorRest,
    serverProperties: ServerProperties,
    private val restTemplateRestHealthIndicator: RestTemplate,
    private val serviceProperties: HealthCheckProperties
) : AbstractHealthIndicator(clock, Preparable { interceptor.markAsHealthCheck() }), HealthIndicator {
    private val randomizer = SecureRandom()
    private val urlPrefix: String = (HTTP_PREFIX + HOST + HOST_PORT_SEPARATOR
            + serverProperties.port
            + URL_PATH + PARAMETER_SEPARATOR + GET_PARAMETER)

    /**
     * main method that determines the health of the service
     */
    override fun determineHealth(): Health {
        val random = randomizer.nextInt(100000, 999999).toString()
        val url = "$urlPrefix{random}"
        val response = restTemplateRestHealthIndicator.getForEntity(url, String::class.java, random)
        val status = if (response.body == serviceProperties.greeting.format(random)) Status.UP else Status.DOWN
        return Health.status(status)
                .withDetail(DETAIL_ENDPOINT_KEY, url)
                .build()
    }

    companion object {
        private const val URL_PATH = "/timetable/healthCheck"
        private const val GET_PARAMETER = "message="
    }
}
