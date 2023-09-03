package com.suenara.taskdispatcher

import com.suenara.taskdispatcher.api.TaskDispatcher
import com.suenara.taskdispatcher.impl.SequentialMeasurableTaskDispatcher

class SequentialMeasurableTaskDispatcherTest : BaseTaskDispatcherTest() {
    override fun provideDispatcher(): TaskDispatcher = SequentialMeasurableTaskDispatcher()
}