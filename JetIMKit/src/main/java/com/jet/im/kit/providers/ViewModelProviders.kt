package com.jet.im.kit.providers

import androidx.lifecycle.ViewModelProvider
import com.jet.im.kit.interfaces.providers.*
import com.jet.im.kit.vm.*

/**
 * A set of Providers that provide a [BaseViewModel] that binds to a Fragment among the screens used in UIKit.
 *
 * @since 3.9.0
 */
object ViewModelProviders {
    /**
     * Returns the ChannelListViewModel provider.
     *
     * @return The [ChannelListViewModelProvider].
     * @since 3.9.0
     */
    @JvmStatic
    lateinit var channelList: ChannelListViewModelProvider

    /**
     * Returns the ChannelViewModel provider.
     *
     * @return The [ChannelViewModelProvider].
     * @since 3.9.0
     */
    @JvmStatic
    lateinit var channel: ChannelViewModelProvider

    /**
     * Reset all providers to default provider.
     *
     * @since 3.10.1
     */
    @JvmStatic
    fun resetToDefault() {
        this.channelList = ChannelListViewModelProvider { owner, query ->
            ViewModelProvider(owner, ViewModelFactory(query))[ChannelListViewModel::class.java]
        }

        this.channel = ChannelViewModelProvider { owner, conversation, params, channelConfig ->
            ViewModelProvider(
                owner,
                ViewModelFactory(conversation, params, channelConfig)
            )[conversation.toString(), ChannelViewModel::class.java]
        }

    }

    init {
        resetToDefault()
    }
}
