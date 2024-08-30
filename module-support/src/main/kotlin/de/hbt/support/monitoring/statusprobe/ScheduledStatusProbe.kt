package de.hbt.support.monitoring.statusprobe

import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler
import org.springframework.scheduling.support.PeriodicTrigger
import java.time.Duration

interface ScheduledStatusProbe {
    fun runScheduledTask()
    fun scheduleTask(
            threadPoolTaskScheduler: ThreadPoolTaskScheduler,
            schedulePeriod: Duration
    ) {
        val periodicTrigger = PeriodicTrigger(
                Duration.ofSeconds(schedulePeriod.toSeconds())
        )
        threadPoolTaskScheduler.schedule(periodicTrigger) { runScheduledTask() }
    }
}

fun ThreadPoolTaskScheduler.schedule(periodicTrigger: PeriodicTrigger, task: Runnable) {
    this.schedule(task, periodicTrigger)
}