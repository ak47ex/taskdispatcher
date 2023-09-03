package com.suenara.taskdispatcher.impl

import com.suenara.taskdispatcher.api.Measurement
import com.suenara.taskdispatcher.api.MeasurementsCollection
import com.suenara.taskdispatcher.api.TaskDispatcher
import java.util.*
import java.util.concurrent.ForkJoinTask
import java.util.concurrent.TimeUnit

class ForkJoinMeasurableTaskDispatcher : TaskDispatcher {
    private val internalDurations = TreeSet<Measurement>()
    override val durations: MeasurementsCollection =
        object : MeasurementsCollection, Collection<Measurement> by internalDurations {}

    private val tasks = arrayListOf<ForkJoinTask<*>>()

    override fun dispatch(
        taskName: String,
        dependents: Iterable<TaskDispatcher.Task>,
        task: Runnable
    ): TaskDispatcher.Task {
        return FjpTask {
            dependents.forEach { it.join() }
            val taskStartTime = System.nanoTime()
            task.run()
            internalDurations.add(Measurement(taskName, System.nanoTime() - taskStartTime, TimeUnit.NANOSECONDS))
        }.also {
            tasks.add(it.task)
        }
    }

    override fun awaitCompletion() {
        try {
            ForkJoinTask.invokeAll(tasks)
        } catch (t: Throwable) {
            tasks.forEach {
                it.cancel(true)
                it.completeExceptionally(t)
            }
            throw t
        } finally {
            tasks.clear()
        }
    }

    private class FjpTask(runnable: Runnable) : TaskDispatcher.Task {

        val task = ForkJoinTask.adapt(runnable)

        override fun join() {
            task.join()
        }
    }
}