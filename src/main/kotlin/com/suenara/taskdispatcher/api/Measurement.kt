package com.suenara.taskdispatcher.api

import java.util.concurrent.TimeUnit

data class Measurement(val taskName: String, val duration: Long, val unit: TimeUnit) : Comparable<Measurement> {
    override fun compareTo(other: Measurement): Int {
        return duration.compareTo(other.duration).takeIf { it != 0 } ?: taskName.compareTo(other.taskName)
    }
}