package com.suenara.taskdispatcher

import com.suenara.taskdispatcher.api.TaskDispatcher
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.*
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.test.assertEquals
import kotlin.test.assertTrue

abstract class BaseTaskDispatcherTest() {

    lateinit var taskDispatcher: TaskDispatcher
    abstract fun provideDispatcher(): TaskDispatcher
    @BeforeEach
    fun setup() {
        taskDispatcher = provideDispatcher()
    }

    @Test
    fun `all independend tasks are executed`() {
        val count = 15
        val completion = Array(count) { AtomicBoolean() }
        repeat(count) {
            taskDispatcher.dispatch("task #$it") { completion[it].set(true) }
        }
        taskDispatcher.awaitCompletion()
        assertTrue(completion.all(AtomicBoolean::get))
    }

    @Test
    fun `all tasks executed in depended order`() {
        val count = 15
        val completion = LinkedList<Int>()
        var taskList = listOf<TaskDispatcher.Task>()
        repeat(count) {
            val task = taskDispatcher.dispatch("task #$it", taskList) {
                completion.add(it)
            }
            taskList = listOf(task)
        }
        taskDispatcher.awaitCompletion()
        assertEquals(count, completion.size)
        val isIncreases = completion.windowed(2) { window ->
            val cur = window.first()
            val next = window[1]
            next - 1 == cur
        }.all { it }
        assertTrue(isIncreases, "order is $completion")
    }
}