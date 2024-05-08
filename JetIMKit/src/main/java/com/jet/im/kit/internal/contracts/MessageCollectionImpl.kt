package com.jet.im.kit.internal.contracts

import com.sendbird.android.collection.MessageCollection
import com.sendbird.android.collection.MessageCollectionInitPolicy
import com.sendbird.android.handler.BaseMessagesHandler
import com.sendbird.android.handler.MessageCollectionHandler
import com.sendbird.android.handler.MessageCollectionInitHandler
import com.sendbird.android.handler.RemoveFailedMessagesHandler
import com.sendbird.android.message.BaseMessage

internal class MessageCollectionImpl(private val collection: MessageCollection) : MessageCollectionContract {
    override fun initialize(initPolicy: MessageCollectionInitPolicy, handler: MessageCollectionInitHandler?) {
        collection.initialize(initPolicy, handler)
    }

    override fun loadPrevious(handler: BaseMessagesHandler?) {
        collection.loadPrevious(handler)
    }

    override fun loadNext(handler: BaseMessagesHandler?) {
        collection.loadNext(handler)
    }

    override fun getPendingMessages(): List<BaseMessage> = collection.pendingMessages

    override fun getFailedMessages(): List<BaseMessage> = collection.failedMessages

    override fun removeFailedMessages(failedMessages: List<BaseMessage>, handler: RemoveFailedMessagesHandler?) {
        collection.removeFailedMessages(failedMessages, handler)
    }

    override fun getStartingPoint(): Long = collection.startingPoint

    override fun getHasPrevious(): Boolean = collection.hasPrevious

    override fun getHasNext(): Boolean = collection.hasNext

    override fun setMessageCollectionHandler(listener: MessageCollectionHandler?) {
        collection.messageCollectionHandler = listener
    }

    override fun dispose() {
        collection.dispose()
    }
}
