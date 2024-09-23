package com.jet.im.kit.internal.tasks

import com.jet.im.kit.log.Logger
import java.util.concurrent.Callable

internal abstract class JobTask<T> {
    @Throws(Exception::class)
    protected abstract fun call(): T?
    val callable: Callable<T>
        get() = Callable {
            var result: T? = null
            try {
                result = call()
            } catch (e: Exception) {
                Logger.e(e)
            }
            result
        }
}
