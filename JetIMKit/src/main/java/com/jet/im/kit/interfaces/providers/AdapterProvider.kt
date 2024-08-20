package com.jet.im.kit.interfaces.providers

import com.jet.im.kit.activities.adapter.ChannelListAdapter
import com.jet.im.kit.activities.adapter.MessageListAdapter
import com.jet.im.kit.model.ChannelListUIParams
import com.jet.im.kit.model.MessageListUIParams
import com.jet.im.kit.providers.AdapterProviders
import com.juggle.im.model.ConversationInfo

/**
 * Interface definition to be invoked when message list adapter is created.
 * @see [AdapterProviders.messageList]
 * @since 3.9.0
 */
fun interface MessageListAdapterProvider {
    /**
     * Returns the MessageListAdapter.
     *
     * @return The [MessageListAdapter].
     * @since 3.9.0
     */
    fun provide(channel: ConversationInfo, uiParams: MessageListUIParams): MessageListAdapter
}


/**
 * Interface definition to be invoked when channel list adapter is created.
 * @see [AdapterProviders.channelList]
 * @since 3.9.0
 */
fun interface ChannelListAdapterProvider {
    /**
     * Returns the ChannelListAdapter.
     *
     * @return The [ChannelListAdapter].
     * @since 3.9.0
     */
    fun provide(uiParams: ChannelListUIParams): ChannelListAdapter
}

