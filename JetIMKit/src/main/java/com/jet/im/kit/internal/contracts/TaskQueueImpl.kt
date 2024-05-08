package com.jet.im.kit.internal.contracts

import com.jet.im.kit.internal.tasks.JobResultTask
import com.jet.im.kit.internal.tasks.JobTask
import com.jet.im.kit.internal.tasks.TaskQueue
import java.util.concurrent.Future

internal class TaskQueueImpl : TaskQueueContract {
    override fun <T> addTask(task: JobTask<T>): Future<T> {
        return TaskQueue.addTask(task)
    }

    override fun <T> addTask(task: JobResultTask<T>): Future<T> {
        return TaskQueue.addTask(task)
    }
}
