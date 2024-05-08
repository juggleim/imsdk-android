package com.jet.im.kit.internal.ui.viewholders

import androidx.recyclerview.widget.RecyclerView
import com.sendbird.android.channel.FeedChannel
import com.sendbird.android.message.BaseMessage
import com.jet.im.kit.databinding.SbViewFeedNotificationBinding
import com.jet.im.kit.internal.model.notifications.NotificationConfig

internal class FeedNotificationViewHolder internal constructor(
    val binding: SbViewFeedNotificationBinding
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(
        channel: FeedChannel,
        message: BaseMessage,
        lastSeenAt: Long,
        config: NotificationConfig?
    ) {
        binding.feedNotification.drawMessage(message, channel, lastSeenAt, config)
    }
}
