package com.suenara.taskdispatcher.impl

import com.suenara.taskdispatcher.api.Measurement
import com.suenara.taskdispatcher.api.TaskDispatcher.Task
import com.suenara.taskdispatcher.api.MeasurementsCollection
import com.suenara.taskdispatcher.api.TaskDispatcher
import java.util.*
import java.util.concurrent.TimeUnit

abstract class MeasurableTaskDispatcher : TaskDispatcher {

    private val internalDurations = TreeSet<Measurement>()
    override val durations: MeasurementsCollection =
        object : MeasurementsCollection, Collection<Measurement> by internalDurations {}

    @JvmOverloads
    override fun dispatch(
        taskName: String,
        dependents: Iterable<Task>,
        task: Runnable
    ): Task = dispatchInternal {
        dependents.forEach(Task::join)
        val taskStartTime = System.nanoTime()
        task.run()
        internalDurations.add(Measurement(taskName, System.nanoTime() - taskStartTime, TimeUnit.NANOSECONDS))
    }

    protected abstract fun dispatchInternal(block: () -> Unit): Task
}
