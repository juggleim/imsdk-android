package com.jet.im.kit.providers

import com.jet.im.kit.activities.adapter.*
import com.jet.im.kit.interfaces.providers.*

/**
 * A set of Providers that provide a RecyclerView.Adapter that binds to a RecyclerView among the screens used in UIKit.
 *
 * @since 3.9.0
 */
object AdapterProviders {
    /**
     * Returns the MessageListAdapter provider.
     *
     * @return The [MessageListAdapterProvider].
     * @since 3.9.0
     */
    @JvmStatic
    lateinit var messageList: MessageListAdapterProvider

    /**
     * Returns the ChannelListAdapter provider.
     *
     * @return The [ChannelListAdapterProvider].
     * @since 3.9.0
     */
    @JvmStatic
    lateinit var channelList: ChannelListAdapterProvider

    /**
     * Reset all providers to default provider.
     *
     * @since 3.10.1
     */
    @JvmStatic
    fun resetToDefault() {
        this.messageList = MessageListAdapterProvider { channel, messageListUIParams ->
            MessageListAdapter(channel, messageListUIParams)
        }
        this.channelList = ChannelListAdapterProvider { uiParams ->
            ChannelListAdapter(null, uiParams)
        }
    }

    init {
        resetToDefault()
    }
}
