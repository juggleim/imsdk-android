package com.jet.im.kit.internal.contracts

import com.juggle.im.interfaces.IConversationManager
import com.jet.im.kit.cust.handler.ConversationCallbackHandler
import com.juggle.im.model.ConversationInfo

internal interface GroupChannelCollectionContract {
    fun setConversationCollectionHandler(handler: IConversationManager.IConversationListener)
    fun loadMore(handler: ConversationCallbackHandler)
    fun getChannelList(): List<ConversationInfo>
    fun getHasMore(): Boolean
    fun dispose()
}
