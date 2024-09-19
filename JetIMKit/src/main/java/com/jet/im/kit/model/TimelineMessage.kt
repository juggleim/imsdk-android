package com.jet.im.kit.model

import com.sendbird.android.message.BaseMessage
import com.sendbird.android.message.CustomizableMessage
import com.jet.im.kit.utils.DateUtils
import com.juggle.im.model.Message
import com.juggle.im.model.MessageContent

open class TimelineMessage(private val time:Long) :
    MessageContent() {
    var message: String
        get() = DateUtils.formatTimelineMessage(time)
        set(message) {
            this.message = message
        }

    override fun encode(): ByteArray {
        return ByteArray(0)
    }

    override fun decode(data: ByteArray?) {
        return
    }
}
