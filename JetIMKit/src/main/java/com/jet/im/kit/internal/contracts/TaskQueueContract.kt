package com.jet.im.kit.internal.contracts

import com.jet.im.kit.internal.tasks.JobResultTask
import com.jet.im.kit.internal.tasks.JobTask
import java.util.concurrent.Future

internal interface TaskQueueContract {
    fun <T> addTask(task: JobTask<T>): Future<T>
    fun <T> addTask(task: JobResultTask<T>): Future<T>
}
