package com.jet.im.kit.internal.contracts

import com.jet.im.interfaces.IConversationManager
import com.jet.im.kit.cust.handler.ConversationCallbackHandler
import com.jet.im.model.ConversationInfo

internal interface GroupChannelCollectionContract {
    fun setConversationCollectionHandler(handler: IConversationManager.IConversationListener)
    fun loadMore(handler: ConversationCallbackHandler)
    fun getChannelList(): List<ConversationInfo>
    fun getHasMore(): Boolean
    fun dispose()
}
