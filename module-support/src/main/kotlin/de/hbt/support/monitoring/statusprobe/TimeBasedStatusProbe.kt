package de.hbt.support.monitoring.statusprobe

import org.springframework.boot.actuate.health.Status
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler
import java.time.Clock
import java.time.Duration
import java.time.ZonedDateTime

class TimeBasedStatusProbe(
    private val maxFailureDuration: Duration, private val clock: Clock,
    threadPoolTaskScheduler: ThreadPoolTaskScheduler, schedulePeriod: Duration, criticality: StatusProbeCriticality,
    name: String, statusProbeLogger: StatusProbeLogger
) : StatusProbe(clock, criticality, name, statusProbeLogger), ScheduledStatusProbe {
    private var lastSuccess: ZonedDateTime? = null
    private var temporaryThrowable: Throwable? = null
    private var temporaryMessage: String? = null

    init {
        scheduleTask(threadPoolTaskScheduler, schedulePeriod)
    }

    @Synchronized
    override fun setStatus(status: Status, throwable: Throwable?, message: String?) {
        if (status === Status.DOWN) {
            this.temporaryThrowable = throwable
            this.temporaryMessage = message
        } else if (status === Status.UP) {
            lastSuccess = ZonedDateTime.now(clock)
            super.setStatus(status, throwable, message)
        }
    }

    private val isOverdue: Boolean
        get() {
            if (lastSuccess == null) {
                return false
            }
            val timeSinceLastSuccess = Duration.between(lastSuccess, ZonedDateTime.now(clock))
            return maxFailureDuration.minus(timeSinceLastSuccess).isNegative
        }

    @Synchronized
    override fun runScheduledTask() {
        if (isOverdue) {
            super.setStatus(Status.DOWN, temporaryThrowable, temporaryMessage)
        }
    }
}
