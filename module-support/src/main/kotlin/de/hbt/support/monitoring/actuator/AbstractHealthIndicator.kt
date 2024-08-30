package de.hbt.support.monitoring.actuator

import mu.KotlinLogging
import org.slf4j.MDC
import org.springframework.boot.actuate.health.Health
import org.springframework.boot.actuate.health.HealthIndicator
import org.springframework.boot.actuate.health.Status
import java.io.IOException
import java.time.Clock
import java.time.Duration
import java.util.*

abstract class AbstractHealthIndicator(
        private val clock: Clock,
        private val preparable: Preparable
) : HealthIndicator {

    private val logStatusDownMessage = "health indicator '${indicatorName()}' invoked with status '${Status.DOWN.code}'"
    private val logStatusUpMessage = "health indicator '${indicatorName()}' invoked with status '${Status.UP.code}'"

    private var firstTime = true

    /**
     * main method that determines the health of the service
     */
    protected abstract fun determineHealth(): Health

    override fun health(): Health {
        try {
            preparable.prepare().use {
                var result: Health? = null
                var exception: Exception? = null
                val start = clock.millis()
                try {
                    result = determineHealth()
                } catch (e: RuntimeException) {
                    exception = e
                    result = Health.down().withException(e).build()
                } finally {
                    logInvocation(result, exception, start, clock.millis())
                }
                return result!!
            }
        } catch (e: IOException) {
            log.error("unexpected exception occurred", e)
            return Health.down(e).build()
        }
    }

    private fun logInvocation(health: Health?, exception: Exception?, start: Long, end: Long) {
        val duration = Duration.ofMillis(end - start)
        MDC.putCloseable("event.duration", duration.toNanos().toString()).use {
            if (exception != null || health == null) {
                log.error(logStatusDownMessage, exception)
                firstTime = true
            } else if (health.status === Status.DOWN) {
                log.warn(logStatusDownMessage)
                firstTime = true
            } else if (firstTime) {
                log.info(logStatusUpMessage)
                firstTime = false
            } else {
                log.trace(logStatusUpMessage)
            }
        }
    }

    private fun indicatorName(): String {
        return this.javaClass.getSimpleName()
                .replace("HealthIndicator", "")
                .lowercase(Locale.getDefault())
    }

    companion object {
        const val HOST = "localhost"
        const val HTTP_PREFIX = "http://"
        const val HOST_PORT_SEPARATOR = ":"
        const val PATH_SEPARATOR = "/"
        const val PARAMETER_SEPARATOR = "?"
        const val DETAIL_ENDPOINT_KEY = "endpoint"
        private val log = KotlinLogging.logger {}
    }
}
