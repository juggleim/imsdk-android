package com.jet.im.kit.model

import com.sendbird.android.message.BaseMessage
import com.sendbird.android.message.CustomizableMessage
import com.jet.im.kit.utils.DateUtils

open class TimelineMessage(private val anchor: BaseMessage) :
    CustomizableMessage(anchor.channelUrl, anchor.messageId + anchor.createdAt, anchor.createdAt - 1) {
    override val requestId: String
        get() = anchor.requestId + createdAt
    override var message: String
        get() = DateUtils.formatTimelineMessage(createdAt)
        set(message) {
            super.message = message
        }
}
