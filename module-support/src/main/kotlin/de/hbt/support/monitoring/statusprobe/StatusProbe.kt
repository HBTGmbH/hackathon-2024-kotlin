package de.hbt.support.monitoring.statusprobe

import lombok.Getter
import org.springframework.boot.actuate.health.Status
import java.time.Clock
import java.time.ZonedDateTime

@Getter
open class StatusProbe(
    private val clock: Clock, criticality: StatusProbeCriticality, private val name: String,
    private val statusProbeLogger: StatusProbeLogger
) {
    var status: Status = Status.UP
        private set
    var throwable: Throwable? = null
        private set
    var message: String? = null
        private set
    var lastStatusChange: ZonedDateTime? = null
        private set

    init {
        statusProbeLogger.registerStatusProbe(name, criticality)
    }

    protected open fun setStatus(status: Status, throwable: Throwable?, message: String?) {
        if (status !== this.status) {
            lastStatusChange = ZonedDateTime.now(clock)
            statusProbeLogger.logStatusChange(name, message, status, lastStatusChange, throwable)
        }
        this.status = status
        this.throwable = throwable
        this.message = message
    }

    fun up() {
        setStatus(Status.UP, null, null)
    }

    fun up(message: String) {
        setStatus(Status.UP, null, message)
    }

    fun down() {
        setStatus(Status.DOWN, null, null)
    }

    fun down(throwable: Throwable) {
        setStatus(Status.DOWN, throwable, null)
    }

    fun down(message: String) {
        setStatus(Status.DOWN, null, message)
    }

    protected fun down(throwable: Throwable, message: String) {
        setStatus(Status.DOWN, throwable, message)
    }
}
