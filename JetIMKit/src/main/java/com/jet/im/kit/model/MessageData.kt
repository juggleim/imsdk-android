package com.jet.im.kit.model

import com.sendbird.android.message.BaseMessage

/**
 * Class that holds message data in a channel.
 *
 * @since 3.5.0
 */
data class MessageData(
    /**
     * Returns data indicating how the message list was updated.
     *
     * @return The String that traces the path of the message list
     * @since 3.5.0
     */
    val traceName: String?,
    /**
     * Returns a list of messages for the current channel.
     *
     * @return A list of the latest messages on the current channel
     * @since 3.5.0
     */
    val messages: List<BaseMessage>
)
