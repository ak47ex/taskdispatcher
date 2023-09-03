package com.suenara.taskdispatcher

import com.suenara.taskdispatcher.api.TaskDispatcher
import com.suenara.taskdispatcher.impl.AsyncMeasurableTaskDispatcher
import java.util.concurrent.Executors

class AsyncMeasurableTaskDispatcherTest : BaseTaskDispatcherTest() {
    override fun provideDispatcher(): TaskDispatcher = AsyncMeasurableTaskDispatcher(Executors.newCachedThreadPool())
}