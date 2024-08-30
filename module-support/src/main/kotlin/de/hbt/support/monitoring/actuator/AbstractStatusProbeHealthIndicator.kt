package de.hbt.support.monitoring.actuator

import de.hbt.support.monitoring.statusprobe.StatusProbe
import org.springframework.boot.actuate.health.Health
import org.springframework.boot.actuate.health.HealthIndicator
import java.time.Clock

abstract class AbstractStatusProbeHealthIndicator(
    timeProvider: Clock, headerInterceptor: Preparable,
    private val statusProbe: StatusProbe
) : AbstractHealthIndicator(timeProvider, headerInterceptor), HealthIndicator {
    override fun determineHealth(): Health {
        val healthBuilder = Health.status(statusProbe.status)
        if (statusProbe.lastStatusChange != null) {
            healthBuilder.withDetail(LAST_STATUS_CHANGE_KEY, statusProbe.lastStatusChange)
        }
        if (statusProbe.throwable != null) {
            healthBuilder.withException(statusProbe.throwable)
        }
        if (statusProbe.message != null) {
            healthBuilder.withDetail(MESSAGE_KEY, statusProbe.message)
        }
        return healthBuilder.build()
    }

    companion object {
        const val MESSAGE_KEY = "message"
        const val LAST_STATUS_CHANGE_KEY = "lastStatusChange"
    }
}
