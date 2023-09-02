package com.suenara.taskdispatcher.api

import java.util.concurrent.TimeUnit

data class Measurement(val taskName: String, val duration: Long, val unit: TimeUnit) : Comparable<Measurement> {
    override fun compareTo(other: Measurement): Int {
        val nanosDuration = unit.toNanos(duration)
        val otherNanosDuration = other.unit.toNanos(other.duration)
        return nanosDuration.compareTo(otherNanosDuration).takeIf { it != 0 } ?: taskName.compareTo(other.taskName)
    }
}