package com.jet.im.kit.internal.ui.viewholders

import android.view.View
import com.sendbird.android.channel.BaseChannel
import com.sendbird.android.message.BaseMessage
import com.jet.im.kit.activities.viewholder.MessageViewHolder
import com.jet.im.kit.databinding.SbViewTimeLineMessageBinding
import com.jet.im.kit.model.MessageListUIParams

internal class TimelineViewHolder internal constructor(
    val binding: SbViewTimeLineMessageBinding,
    messageListUIParams: MessageListUIParams
) : MessageViewHolder(binding.root, messageListUIParams) {

    override fun bind(channel: BaseChannel, message: BaseMessage, messageListUIParams: MessageListUIParams) {
        binding.timelineMessageView.messageUIConfig = messageUIConfig
        binding.timelineMessageView.drawTimeline(message)
    }

    override fun getClickableViewMap(): Map<String, View> = mapOf()
}
