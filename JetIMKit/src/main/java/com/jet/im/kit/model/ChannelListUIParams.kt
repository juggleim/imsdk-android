package com.jet.im.kit.model

import com.jet.im.kit.model.configurations.UIKitConfig

/**
 * This class is used to determine whether to display features in the channel list.
 * @since 3.6.0
 */
data class ChannelListUIParams(
    /**
     * Sets whether to display typing indicator in the channel list.
     * If this value is true, the typing indicator is displayed in the channel list.
     * @since 3.6.0
     */
    val enableTypingIndicator: Boolean = UIKitConfig.groupChannelListConfig.enableTypingIndicator,
    /**
     * Sets whether to display message receipt status in the channel list.
     * If this value is true, the message receipt status is displayed in the channel list.
     * @since 3.6.0
     */
    val enableMessageReceiptStatus: Boolean = UIKitConfig.groupChannelListConfig.enableMessageReceiptStatus
)
