package com.jet.im.kit.model

interface MessageDisplayData

/**
 * This data class contains the information of the message that is displayed.
 *
 * @since 3.5.7
 */
data class UserMessageDisplayData(
    /**
     * The message text to be sent or rendered.
     *
     * @since 3.5.7
     */
    val message: String? = null
) : MessageDisplayData
