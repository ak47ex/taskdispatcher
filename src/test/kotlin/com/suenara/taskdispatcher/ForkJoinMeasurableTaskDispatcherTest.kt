package com.suenara.taskdispatcher

import com.suenara.taskdispatcher.api.TaskDispatcher
import com.suenara.taskdispatcher.impl.ForkJoinMeasurableTaskDispatcher

class ForkJoinMeasurableTaskDispatcherTest : BaseTaskDispatcherTest() {
    override fun provideDispatcher(): TaskDispatcher = ForkJoinMeasurableTaskDispatcher()
}