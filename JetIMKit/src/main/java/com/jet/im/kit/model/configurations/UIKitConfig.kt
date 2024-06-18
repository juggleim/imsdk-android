package com.jet.im.kit.model.configurations

/**
 * UIKitConfig is a object that provides the configurations for the UIKit.
 * This configuration has the higher priority than the dashboard settings, therefore will override the UIKit config from dashboard.
 * @since 3.6.0
 */
object UIKitConfig {

    @JvmStatic
    private val config: Configurations = Configurations()

    @JvmStatic
    @get:JvmSynthetic
    internal val uikitConfig: UIKitConfigurations = config.uikitConfig

    /**
     * Returns the common configuration for the UIKit.
     * @since 3.6.0
     */
    @JvmStatic
    val common: Common = config.uikitConfig.common

    /**
     * Returns the channel configuration for the UIKit.
     * @since 3.6.0
     */
    @JvmStatic
    val groupChannelConfig: ChannelConfig = config.uikitConfig.group.channel

    /**
     * Returns the channel list configuration for the UIKit.
     * @since 3.6.0
     */
    @JvmStatic
    val groupChannelListConfig: ChannelListConfig = config.uikitConfig.group.channelList

    /**
     * Returns the open channel configuration for the UIKit.
     * @since 3.6.0
     */
    @JvmStatic
    val openChannelConfig: OpenChannelConfig = config.uikitConfig.open.channel
}
