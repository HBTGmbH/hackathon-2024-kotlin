package de.hbt.support.monitoring.statusprobe

import org.springframework.boot.actuate.health.Status
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler
import java.time.Clock
import java.time.Duration

/**
 * uses the percentage of down statuses within a given period (default: 1 min) to determine if status of probe is down.
 * this is meant to be used to avoid flickering status probes on services that have lots of status updates. When there
 * is no significant amount of requests during one scheduling period, the behavior may be arbitrary.
 */
class PercentageBasedStatusProbe(
    private val maxFailurePercent: Int, clock: Clock,
    threadPoolTaskScheduler: ThreadPoolTaskScheduler, schedulePeriod: Duration, criticality: StatusProbeCriticality,
    name: String, statusProbeLogger: StatusProbeLogger
) : StatusProbe(clock, criticality, name, statusProbeLogger), ScheduledStatusProbe {
    private var requestCount = 0
    private var downCount = 0
    private var temporaryMessage: String? = null
    private var temporaryThrowable: Throwable? = null

    init {
        scheduleTask(threadPoolTaskScheduler, schedulePeriod)
    }

    @Synchronized
    override fun setStatus(status: Status, throwable: Throwable?, message: String?) {
        if (status === Status.DOWN) {
            downCount++
            this.temporaryThrowable = throwable
            this.temporaryMessage = message
        }
        requestCount++
    }

    private fun reset() {
        requestCount = 0
        downCount = 0
    }

    @Synchronized
    override fun runScheduledTask() {
        if (requestCount > 0 && downCount * 100.0 / requestCount > maxFailurePercent) {
            super.setStatus(Status.DOWN, temporaryThrowable, temporaryMessage)
        } else {
            super.setStatus(Status.UP, null, null)
        }
        reset()
    }
}
