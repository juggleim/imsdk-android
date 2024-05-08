package com.jet.im.kit.internal.ui.viewholders

import com.sendbird.android.channel.GroupChannel
import com.sendbird.android.message.BaseMessage
import com.jet.im.kit.databinding.SbViewTimeLineMessageBinding
import com.jet.im.kit.internal.model.notifications.NotificationConfig

internal class NotificationTimelineViewHolder internal constructor(
    val binding: SbViewTimeLineMessageBinding,
) : NotificationViewHolder(binding.root) {

    override fun bind(channel: GroupChannel, message: BaseMessage, config: NotificationConfig?) {
        binding.timelineMessageView.drawTimeline(message, config)
    }
}
