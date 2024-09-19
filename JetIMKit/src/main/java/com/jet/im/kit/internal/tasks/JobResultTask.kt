package com.jet.im.kit.internal.tasks

import android.os.Handler
import android.os.Looper
import com.sendbird.android.exception.SendbirdError.ERR_REQUEST_FAILED
import com.sendbird.android.exception.SendbirdException
import java.util.concurrent.Callable
import java.util.concurrent.CountDownLatch

internal abstract class JobResultTask<T> {
    private val task: JobTask<T> = object : JobTask<T>() {
        @Throws(InterruptedException::class)
        public override fun call(): T? {
            var result: T? = null
            var ex: SendbirdException? = null
            try {
                result = this@JobResultTask.call()
            } catch (e: SendbirdException) {
                ex = e
            } catch (e: Exception) {
                ex = SendbirdException(e.message, ERR_REQUEST_FAILED)
            }
            val response = result
            val e = ex
            val lock = CountDownLatch(1)
            mainHandler.post {
                try {
                    onResultForUiThread(response, e)
                } finally {
                    lock.countDown()
                }
            }
            lock.await()
            return result
        }
    }

    @Throws(Exception::class)
    abstract fun call(): T?
    abstract fun onResultForUiThread(result: T?, e: SendbirdException?)
    val callable: Callable<T>
        get() = task.callable

    companion object {
        private val mainHandler = Handler(Looper.getMainLooper())
    }
}
