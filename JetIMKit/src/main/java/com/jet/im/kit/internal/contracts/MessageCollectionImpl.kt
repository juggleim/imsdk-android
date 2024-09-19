package com.jet.im.kit.internal.contracts

import android.os.Handler
import android.os.Looper
import com.jet.im.kit.interfaces.MessageHandler
import com.juggle.im.JIM
import com.juggle.im.JIMConst
import com.juggle.im.model.ConversationInfo
import com.sendbird.android.handler.MessageCollectionHandler
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

internal class MessageCollectionImpl(private val collection: ConversationInfo) : MessageCollectionContract {
    // 创建一个单一线程的线程池
    private val singleThreadExecutor: ExecutorService = Executors.newSingleThreadExecutor()
    private var mainHandler=Handler(Looper.getMainLooper());
    override fun initialize(handler: MessageHandler?) {
    }

    override fun loadPrevious(handler: MessageHandler?) {
//        collection.loadPrevious(handler)
    }

    override fun loadNext(handler: MessageHandler?) {
//        collection.loadNext(handler)
    }
    override fun getHasPrevious(): Boolean = true

    override fun getHasNext(): Boolean = false

    override fun setMessageCollectionHandler(listener: MessageCollectionHandler?) {
//        collection.messageCollectionHandler = listener
    }

    override fun dispose() {

    }
}
