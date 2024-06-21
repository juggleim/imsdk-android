package com.jet.im.kit.providers

import com.jet.im.kit.interfaces.providers.*
import com.jet.im.kit.modules.*

/**
 * UIKit for Android, you need a module and components to create a view.
 * Components are the smallest unit of customizable views that can make up a whole screen and the module coordinates these components to be shown as the fragment's view.
 * Each module also has its own customizable style per screen.
 * A set of Providers that provide a Module that binds to a Fragment among the screens used in UIKit.
 *
 * @since 3.9.0
 */
object ModuleProviders {

    /**
     * Returns the ChannelListModule provider.
     *
     * @return The [ChannelListModuleProvider].
     * @since 3.9.0
     */
    @JvmStatic
    lateinit var channelList: ChannelListModuleProvider

    /**
     * Returns the ChannelModule provider.
     *
     * @return The [ChannelModuleProvider].
     * @since 3.9.0
     */
    @JvmStatic
    lateinit var channel: ChannelModuleProvider

    /**
     * Reset all providers to default provider.
     *
     * @since 3.10.1
     */
    @JvmStatic
    fun resetToDefault() {
        this.channelList = ChannelListModuleProvider { context, _ -> ChannelListModule(context) }

        this.channel = ChannelModuleProvider { context, _ -> ChannelModule(context) }
    }

    init {
        resetToDefault()
    }
}
