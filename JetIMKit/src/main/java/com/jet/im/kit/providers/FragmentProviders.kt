package com.jet.im.kit.providers

import com.jet.im.kit.fragments.*
import com.jet.im.kit.interfaces.providers.*

/**
 * Create a Fragment provider.
 * In situations where you need to create a fragment, create the fragment through the following providers.
 * If you need to use Custom Fragment, change the provider
 *
 * @since 3.9.0
 */
object FragmentProviders {
    /**
     * Returns the ChannelListFragment provider.
     *
     * @return The [ChannelListFragmentProvider]
     * @since 3.9.0
     */
    @JvmStatic
    lateinit var channelList: ChannelListFragmentProvider

    /**
     * Returns the ChannelFragment provider.
     *
     * @return The [ChannelFragmentProvider]
     * @since 3.9.0
     */
    @JvmStatic
    lateinit var channel: ChannelFragmentProvider

    /**
     * Reset all providers to default provider.
     *
     * @since 3.10.1
     */
    @JvmStatic
    fun resetToDefault() {
        this.channelList = ChannelListFragmentProvider { args ->
            ChannelListFragment.Builder().withArguments(args).setUseHeader(true).build()
        }

        this.channel = ChannelFragmentProvider { type, id, args ->
            ChannelFragment.Builder(type,id).withArguments(args)
                .setUseHeader(true)
                .build()
        }
    }

    init {
        resetToDefault()
    }
}
