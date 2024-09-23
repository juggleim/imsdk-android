package com.jet.im.kit.interfaces.providers

import android.content.Context
import android.os.Bundle
import com.jet.im.kit.modules.ChannelListModule
import com.jet.im.kit.modules.ChannelModule
import com.jet.im.kit.providers.ModuleProviders

/**
 * Interface definition to be invoked when ChannelListModule is created.
 * @see [ModuleProviders.channelList]
 * @since 3.9.0
 */
fun interface ChannelListModuleProvider {
    /**
     * Returns the ChannelListModule.
     *
     * @return The [ChannelListModule].
     * @since 3.9.0
     */
    fun provide(context: Context, args: Bundle): ChannelListModule
}

/**
 * Interface definition to be invoked when ChannelModule is created.
 * @see [ModuleProviders.channel]
 * @since 3.9.0
 */
fun interface ChannelModuleProvider {
    /**
     * Returns the ChannelModule.
     *
     * @return The [ChannelModule].
     * @since 3.9.0
     */
    fun provide(context: Context, args: Bundle): ChannelModule
}


