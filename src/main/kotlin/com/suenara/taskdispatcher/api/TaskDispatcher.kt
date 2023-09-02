package com.suenara.taskdispatcher.api

interface TaskDispatcher {
    val durations: MeasurementsCollection

    val durationsSum: Long
        get() = durations.sumOf(Measurement::duration)

    fun dispatch(
        taskName: String,
        dependents: Iterable<Task> = emptyList(),
        task: Runnable
    ): Task

    fun awaitCompletion()

    interface Task {
        fun join()
    }
}