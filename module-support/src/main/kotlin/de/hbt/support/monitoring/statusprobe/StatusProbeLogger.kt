package de.hbt.support.monitoring.statusprobe

import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.apache.logging.log4j.MarkerManager
import org.apache.logging.log4j.message.StringMapMessage
import org.springframework.boot.actuate.health.Status
import java.time.Clock
import java.time.ZonedDateTime

class StatusProbeLogger internal constructor(private val clock: Clock, private val commLog: Logger) {
    private val statusProbeToStatus: MutableMap<ProbeIdent, Status> = mutableMapOf()

    constructor(clock: Clock) : this(clock, LogManager.getLogger("statusprobe"))

    fun registerStatusProbe(name: String, criticality: StatusProbeCriticality) {
        statusProbeToStatus[ProbeIdent(name, criticality)] = Status.UP
        logStatusChange(name, "Startup", Status.UP, ZonedDateTime.now(clock), null)
    }

    fun logStatusChange(
            name: String, message: String?, status: Status, lastStatusChange: ZonedDateTime?,
            throwable: Throwable?
    ) {
        var probeIdent = getProbeIdent(name)
        if (probeIdent == null) {
            probeIdent = ProbeIdent(name, StatusProbeCriticality.K1)
        }
        statusProbeToStatus[probeIdent] = status
        createLog(message, lastStatusChange, throwable)
    }

    private fun getProbeIdent(name: String): ProbeIdent? {
        return statusProbeToStatus.keys.asSequence()
                .filter { it.name == name }
                .firstOrNull()
    }

    private fun createLog(message: String?, lastStatusChange: ZonedDateTime?, throwable: Throwable?) {
        val cleanedMessage = message ?: ""
        val overallStatus = overallStatus
        val criticality = overallCriticality
        if (Status.UP == overallStatus) {
            commLog.info(
                    MARKER, StringMapMessage()
                    .with(LABEL_CRITICALITY, criticality)
                    .with(LABEL_STATUS, overallStatus)
                    .with(LABEL_MESSAGE, cleanedMessage)
                    .with(LABEL_LAST_STATUS_CHANGE, lastStatusChange)
            )
        } else {
            commLog.error(
                    MARKER, StringMapMessage()
                    .with(LABEL_CRITICALITY, criticality)
                    .with(LABEL_STATUS, overallStatus)
                    .with(LABEL_MESSAGE, cleanedMessage)
                    .with(LABEL_REASON, reason)
                    .with(LABEL_LAST_STATUS_CHANGE, lastStatusChange), throwable
            )
        }
    }

    private val overallCriticality: StatusProbeCriticality
        get() {
            val crits = statusProbeToStatus.keys.asSequence()
                    .map { it.criticality }
                    .toList()
            return if (crits.contains(StatusProbeCriticality.K1)) StatusProbeCriticality.K1
            else if (crits.contains(StatusProbeCriticality.K2)) StatusProbeCriticality.K2
            else StatusProbeCriticality.K3
        }

    private val overallStatus: Status
        get() = if (statusProbeToStatus.containsValue(Status.DOWN)) Status.DOWN
        else Status.UP

    private val reason: String
        get() {
            val probesDown = statusProbeToStatus.entries.asSequence()
                    .filter { it.value == Status.DOWN }
                    .map { it.key }
                    .toList()
            val reasonK1 = getDownStatusProbes(probesDown, StatusProbeCriticality.K1)
            val reasonK2 = getDownStatusProbes(probesDown, StatusProbeCriticality.K2)
            val reasonK3 = getDownStatusProbes(probesDown, StatusProbeCriticality.K3)
            return "$reasonK1$reasonK2$reasonK3".trim { it <= ' ' }
        }

    private fun getDownStatusProbes(probesDown: List<ProbeIdent>, criticality: StatusProbeCriticality): String {
        val downProbeNames = probesDown.asSequence()
                .filter { it.criticality == criticality }
                .map { it.name }
                .toList()
        return if (downProbeNames.isNotEmpty()) {
            "$criticality failed: ${downProbeNames.joinToString(separator = ",")}\n"
        } else ""
    }

    data class ProbeIdent(val name: String, val criticality: StatusProbeCriticality)

    companion object {
        private val MARKER = MarkerManager.getMarker("statusprobe")
        private const val LABEL_CRITICALITY = "label.status.criticality"
        private const val LABEL_STATUS = "label.status.status"
        private const val LABEL_REASON = "label.status.reason"
        private const val LABEL_MESSAGE = "label.status.description"
        private const val LABEL_LAST_STATUS_CHANGE = "label.status.last_change"
    }
}
