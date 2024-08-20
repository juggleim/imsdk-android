package com.jet.im.kit.internal.contracts

import com.jet.im.kit.interfaces.MessageHandler
import com.juggle.im.model.Message
import com.sendbird.android.collection.MessageCollectionInitPolicy
import com.sendbird.android.handler.BaseMessagesHandler
import com.sendbird.android.handler.MessageCollectionHandler
import com.sendbird.android.handler.MessageCollectionInitHandler
import com.sendbird.android.handler.RemoveFailedMessagesHandler
import com.sendbird.android.message.BaseMessage

internal interface MessageCollectionContract {
    fun initialize( handler: MessageHandler?)
    fun loadPrevious(handler: MessageHandler?)
    fun loadNext(handler: MessageHandler?)
    fun getHasPrevious(): Boolean
    fun getHasNext(): Boolean
    fun setMessageCollectionHandler(listener: MessageCollectionHandler?)
    fun dispose()
}
