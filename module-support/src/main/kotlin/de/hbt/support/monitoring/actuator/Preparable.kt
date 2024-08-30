package de.hbt.support.monitoring.actuator

import java.io.Closeable

fun interface Preparable {
    fun prepare(): Closeable
}
