package com.jet.im.kit.model

import com.sendbird.android.message.CustomizableMessage
import com.sendbird.android.user.User

internal class TypingIndicatorMessage(channelUrl: String, val typingUsers: List<User>) :
    CustomizableMessage(channelUrl, Long.MAX_VALUE, Long.MAX_VALUE) {
    override val requestId: String
        get() = Long.MAX_VALUE.toString()
}
