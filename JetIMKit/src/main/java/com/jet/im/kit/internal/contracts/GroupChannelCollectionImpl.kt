package com.jet.im.kit.internal.contracts

import com.jet.im.kit.cust.handler.ConversationCallbackHandler
import com.juggle.im.JIMConst
import com.juggle.im.interfaces.IConversationManager
import com.juggle.im.model.Conversation
import com.juggle.im.model.ConversationInfo

internal class GroupChannelCollectionImpl(query: IConversationManager) :
    GroupChannelCollectionContract {
    private var collection: ArrayList<ConversationInfo> = ArrayList()
    private var hasMore: Boolean = true
    private val conversationManager: IConversationManager = query


    override fun setConversationCollectionHandler(handler: IConversationManager.IConversationListener) {
        conversationManager.addListener("GroupChannelCollectionImpl", handler)
    }

    override fun loadMore(handler: ConversationCallbackHandler) {
        val timestamp: Long =
            if (collection.isEmpty()) 0 else collection[collection.size - 1].sortTime;
        val conversationInfoList = conversationManager.getConversationInfoList(
            10,
            timestamp,
            JIMConst.PullDirection.OLDER
        )

        for (conversationInfo: ConversationInfo in conversationInfoList) {
            if (conversationInfo.conversation.equals(Conversation(Conversation.ConversationType.SYSTEM, "friend_apply"))) {
                return
            }
            var toBeRemove: ConversationInfo? = null
            for (existConversationInfo: ConversationInfo in collection) {
                if (existConversationInfo.conversation.equals(conversationInfo.conversation)) {
                    toBeRemove = existConversationInfo
                }
            }
            if (toBeRemove != null) {
                collection.remove(toBeRemove)
            }
            collection.add(conversationInfo)
        }
        val topConversationInfoList: ArrayList<ConversationInfo> = ArrayList()
        val notTopConversationInfoList: ArrayList<ConversationInfo> = ArrayList()
        for (conversationInfo: ConversationInfo in collection) {
            if (conversationInfo.isTop) {
                topConversationInfoList.add(conversationInfo)
            } else {
                notTopConversationInfoList.add(conversationInfo)
            }
        }
        val top = topConversationInfoList.sortedByDescending { it.topTime }
        val notTop = notTopConversationInfoList.sortedByDescending { it.sortTime }
        val result: ArrayList<ConversationInfo> = ArrayList()
        result.addAll(top)
        result.addAll(notTop)
        collection = result

        if (conversationInfoList.size < 10) {
            hasMore = false;
        }
        handler.onResult(conversationInfoList)
    }

    override fun getChannelList(): List<ConversationInfo> = collection

    override fun getHasMore(): Boolean = hasMore

    override fun dispose() {
        conversationManager.removeListener("GroupChannelCollectionImpl")
    }
}
