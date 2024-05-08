package com.jet.im.kit.internal.ui.viewholders

import com.sendbird.android.channel.GroupChannel
import com.sendbird.android.message.BaseMessage
import com.jet.im.kit.databinding.SbViewChatNotificationBinding
import com.jet.im.kit.internal.model.notifications.NotificationConfig

internal class ChatNotificationViewHolder internal constructor(
    val binding: SbViewChatNotificationBinding
) : NotificationViewHolder(binding.root) {

    override fun bind(channel: GroupChannel, message: BaseMessage, config: NotificationConfig?) {
        binding.chatNotification.drawMessage(channel, message, config)
    }
}
