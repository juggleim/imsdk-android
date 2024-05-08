package com.jet.im.kit.internal.contracts

import com.sendbird.android.SendbirdChat.createGroupChannelCollection
import com.sendbird.android.channel.GroupChannel
import com.sendbird.android.channel.query.GroupChannelListQuery
import com.sendbird.android.collection.GroupChannelCollection
import com.sendbird.android.handler.GroupChannelCollectionHandler
import com.sendbird.android.handler.GroupChannelsCallbackHandler
import com.sendbird.android.params.GroupChannelCollectionCreateParams

internal class GroupChannelCollectionImpl(query: GroupChannelListQuery) : GroupChannelCollectionContract {
    private val collection: GroupChannelCollection

    init {
        collection = createGroupChannelCollection(GroupChannelCollectionCreateParams(query))
    }

    override fun setGroupChannelCollectionHandler(handler: GroupChannelCollectionHandler?) {
        collection.groupChannelCollectionHandler = handler
    }

    override fun loadMore(handler: GroupChannelsCallbackHandler) {
        collection.loadMore(handler)
    }

    override fun getChannelList(): List<GroupChannel> = collection.channelList

    override fun getHasMore(): Boolean = collection.hasMore

    override fun dispose() {
        collection.dispose()
    }
}
