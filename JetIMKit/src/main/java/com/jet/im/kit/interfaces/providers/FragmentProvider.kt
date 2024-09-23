package com.jet.im.kit.interfaces.providers

import android.os.Bundle
import com.jet.im.kit.fragments.ChannelFragment
import com.jet.im.kit.fragments.ChannelListFragment
import com.jet.im.kit.providers.FragmentProviders

/**
 * Interface definition to be invoked when ChannelListFragment is created.
 * @see [FragmentProviders.channelList]
 * @since 3.9.0
 */
fun interface ChannelListFragmentProvider {
    /**
     * Returns the ChannelListFragment.
     *
     * @return The [ChannelListFragment].
     * @since 3.9.0
     */
    fun provide(args: Bundle): ChannelListFragment
}

/**
 * Interface definition to be invoked when ChannelFragment is created.
 * @see [FragmentProviders.channel]
 * @since 3.9.0
 */
fun interface ChannelFragmentProvider {
    /**
     * Returns the ChannelFragment.
     *
     * @return The [ChannelFragment].
     * @since 3.9.0
     */
    fun provide(conversationType: Int, conversationId: String, args: Bundle): ChannelFragment
}

