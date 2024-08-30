package de.hbt.support.monitoring.statusprobe

import org.springframework.boot.actuate.health.Status
import java.time.Clock
import java.util.concurrent.atomic.AtomicInteger

class CountBasedStatusProbe(
    private val maxFailureCount: Int, clock: Clock, criticality: StatusProbeCriticality, name: String,
    statusProbeLogger: StatusProbeLogger
) : StatusProbe(clock, criticality, name, statusProbeLogger) {
    private val failureCount = AtomicInteger(0)

    @Synchronized
    override fun setStatus(status: Status, throwable: Throwable?, message: String?) {
        if (status === Status.DOWN) {
            val failureCount = failureCount.incrementAndGet()
            if (failureCount > maxFailureCount) {
                super.setStatus(status, throwable, message)
            }
        } else if (status === Status.UP) {
            failureCount.set(0)
            super.setStatus(status, throwable, message)
        }
    }
}
