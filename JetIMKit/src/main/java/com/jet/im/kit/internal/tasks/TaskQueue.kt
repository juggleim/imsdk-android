package com.jet.im.kit.internal.tasks

import java.util.concurrent.Executors
import java.util.concurrent.Future

internal object TaskQueue {
    private val taskExecutor = Executors.newCachedThreadPool()

    @JvmStatic
    fun <T> addTask(task: JobTask<T>): Future<T> {
        return taskExecutor.submit(task.callable)
    }

    @JvmStatic
    fun <T> addTask(task: JobResultTask<T>): Future<T> {
        return taskExecutor.submit(task.callable)
    }
}
