package com.jet.im.kit.internal.contracts

import com.sendbird.android.collection.MessageCollectionInitPolicy
import com.sendbird.android.handler.BaseMessagesHandler
import com.sendbird.android.handler.MessageCollectionHandler
import com.sendbird.android.handler.MessageCollectionInitHandler
import com.sendbird.android.handler.RemoveFailedMessagesHandler
import com.sendbird.android.message.BaseMessage

internal interface MessageCollectionContract {
    fun initialize(initPolicy: MessageCollectionInitPolicy, handler: MessageCollectionInitHandler?)
    fun loadPrevious(handler: BaseMessagesHandler?)
    fun loadNext(handler: BaseMessagesHandler?)
    fun getPendingMessages(): List<BaseMessage>
    fun getFailedMessages(): List<BaseMessage>
    fun removeFailedMessages(failedMessages: List<BaseMessage>, handler: RemoveFailedMessagesHandler?)
    fun getStartingPoint(): Long
    fun getHasPrevious(): Boolean
    fun getHasNext(): Boolean
    fun setMessageCollectionHandler(listener: MessageCollectionHandler?)
    fun dispose()
}
