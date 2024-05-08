package com.jet.im.kit.internal.queries

import androidx.annotation.WorkerThread
import com.sendbird.android.exception.SendbirdException
import com.sendbird.android.message.BaseMessage
import com.sendbird.android.params.ThreadMessageListParams
import java.util.concurrent.CountDownLatch
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicLong
import java.util.concurrent.atomic.AtomicReference

internal class MessageThreadListQuery @JvmOverloads constructor(
    private val parentMessage: BaseMessage,
    val startingPoint: Long = 0
) {
    private val hasNext = AtomicBoolean()
    private val hasPrev = AtomicBoolean()
    private val nextTs = AtomicLong()
    private val prevTs = AtomicLong()
    private val limit = 20

    init {
        nextTs.set(startingPoint)
        prevTs.set(startingPoint)
        hasPrev.set(startingPoint > 0L)
        hasNext.set(startingPoint < Long.MAX_VALUE)
    }

    @WorkerThread
    @Throws(Exception::class)
    fun loadPrevious(params: ThreadMessageListParams): List<BaseMessage> {
        if (!hasPrevious()) return ArrayList()
        params.apply {
            inclusive = true
            previousResultSize = limit
            nextResultSize = 0
        }

        val latch = CountDownLatch(1)
        val result = AtomicReference<List<BaseMessage>>()
        val exceptionAtomicReference = AtomicReference<SendbirdException?>()
        parentMessage.getThreadedMessagesByTimestamp(
            prevTs.get(), params
        ) { _: BaseMessage?, list: List<BaseMessage>?, e: SendbirdException? ->
            try {
                if (e != null) {
                    result.set(ArrayList())
                    exceptionAtomicReference.set(e)
                } else if (list != null) {
                    result.set(list)
                    hasPrev.set(list.size >= limit)
                    if (list.isNotEmpty()) {
                        prevTs.set(list[list.size - 1].createdAt)
                    }
                } else {
                    result.set(ArrayList())
                }
            } finally {
                latch.countDown()
            }
        }
        latch.await()
        exceptionAtomicReference.get()?.let {
            throw it
        }
        return result.get()
    }

    @WorkerThread
    @Throws(Exception::class)
    fun loadNext(params: ThreadMessageListParams): List<BaseMessage> {
        if (!hasNext()) return ArrayList()
        params.apply {
            inclusive = true
            previousResultSize = 0
            nextResultSize = limit
        }

        val latch = CountDownLatch(1)
        val result = AtomicReference<List<BaseMessage>>()
        val exceptionAtomicReference = AtomicReference<SendbirdException?>()
        parentMessage.getThreadedMessagesByTimestamp(
            nextTs.get(), params
        ) { _: BaseMessage?, list: List<BaseMessage>?, e: SendbirdException? ->
            try {
                if (e != null) {
                    result.set(ArrayList())
                    exceptionAtomicReference.set(e)
                } else if (list != null) {
                    result.set(list)
                    hasNext.set(list.size >= limit)
                    if (list.isNotEmpty()) {
                        nextTs.set(list[0].createdAt)
                    }
                } else {
                    result.set(ArrayList())
                }
            } finally {
                latch.countDown()
            }
        }
        latch.await()

        exceptionAtomicReference.get()?.let {
            throw it
        }
        return result.get()
    }

    operator fun hasNext(): Boolean {
        return hasNext.get()
    }

    fun hasPrevious(): Boolean {
        return hasPrev.get()
    }
}
