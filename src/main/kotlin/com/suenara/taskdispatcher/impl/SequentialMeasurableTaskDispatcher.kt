package com.suenara.taskdispatcher.impl

import com.suenara.taskdispatcher.api.TaskDispatcher.*

class SequentialMeasurableTaskDispatcher : MeasurableTaskDispatcher() {

    override fun dispatchInternal(block: () -> Unit): Task = SynchronousTask().apply { wrap(Runnable(block)) }

    override fun awaitCompletion() = Unit

    private class SynchronousTask : Task {
        fun wrap(runnable: Runnable) = runnable.run()

        override fun join() = Unit
    }

}
