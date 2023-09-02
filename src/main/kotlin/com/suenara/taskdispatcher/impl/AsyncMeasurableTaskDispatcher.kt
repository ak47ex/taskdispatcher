package com.suenara.taskdispatcher.impl

import com.suenara.taskdispatcher.api.TaskDispatcher.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Future

class AsyncMeasurableTaskDispatcher(
    private val executorService: ExecutorService
) : MeasurableTaskDispatcher() {

    private val tasks = arrayListOf<Future<*>>()

    override fun dispatchInternal(block: () -> Unit): Task {
        return ExecutorTask(executorService.submit(block).also(tasks::add))
    }

    override fun awaitCompletion() {
        try {
            tasks.forEach(Future<*>::get)
        } catch (t: Throwable) {
            tasks.forEach { it.cancel(true) }
        } finally {
            tasks.clear()
        }
    }

    private class ExecutorTask(
        private val future: Future<*>
    ) : Task {
        override fun join() {
            future.get()
        }
    }
}
