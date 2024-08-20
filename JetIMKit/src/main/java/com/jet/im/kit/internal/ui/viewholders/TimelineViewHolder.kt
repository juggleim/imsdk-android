package com.jet.im.kit.internal.ui.viewholders

import android.view.View
import com.sendbird.android.channel.BaseChannel
import com.sendbird.android.message.BaseMessage
import com.jet.im.kit.activities.viewholder.MessageViewHolder
import com.jet.im.kit.databinding.SbViewTimeLineMessageBinding
import com.jet.im.kit.model.MessageListUIParams
import com.juggle.im.model.ConversationInfo
import com.juggle.im.model.Message

internal class TimelineViewHolder internal constructor(
    val binding: SbViewTimeLineMessageBinding,
    messageListUIParams: MessageListUIParams
) : MessageViewHolder(binding.root, messageListUIParams) {

    override fun bind(channel: ConversationInfo, message: Message, params: MessageListUIParams) {
        binding.timelineMessageView.messageUIConfig = messageUIConfig
        binding.timelineMessageView.drawTimeline(message)
    }

    override fun getClickableViewMap(): Map<String, View> = mapOf()
}
