package com.jet.im.kit.model

import com.sendbird.android.message.BaseMessage
import com.sendbird.android.message.CustomizableMessage

internal class SuggestedRepliesMessage(
    val anchor: BaseMessage
) : CustomizableMessage(anchor.channelUrl, anchor.messageId + anchor.createdAt, anchor.createdAt + 1) {
    override val requestId: String
        get() = anchor.requestId + createdAt
}
