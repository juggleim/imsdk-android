package com.jet.im.kit.internal.contracts

import android.os.Handler
import android.os.Looper
import com.jet.im.JetIM
import com.jet.im.JetIMConst
import com.jet.im.kit.interfaces.MessageHandler
import com.jet.im.model.ConversationInfo
import com.sendbird.android.handler.MessageCollectionHandler
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

internal class MessageCollectionImpl(private val collection: ConversationInfo) : MessageCollectionContract {
    // 创建一个单一线程的线程池
    private val singleThreadExecutor: ExecutorService = Executors.newSingleThreadExecutor()
    private var mainHandler=Handler(Looper.getMainLooper());
    override fun initialize(handler: MessageHandler?) {
        singleThreadExecutor.submit {
            val messages = JetIM.getInstance().messageManager.getMessages(
                collection.conversation,
                50,
                0,
                JetIMConst.PullDirection.OLDER
            )
            mainHandler.post { handler?.onResult(messages, null) }

        }

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
