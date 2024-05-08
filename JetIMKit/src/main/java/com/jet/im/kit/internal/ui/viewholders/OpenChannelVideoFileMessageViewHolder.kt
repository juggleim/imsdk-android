package com.jet.im.kit.internal.ui.viewholders

import android.view.View
import com.sendbird.android.channel.BaseChannel
import com.sendbird.android.channel.OpenChannel
import com.sendbird.android.message.BaseMessage
import com.jet.im.kit.activities.viewholder.MessageViewHolder
import com.jet.im.kit.consts.ClickableViewIdentifier
import com.jet.im.kit.databinding.SbViewOpenChannelFileVideoMessageBinding
import com.jet.im.kit.model.MessageListUIParams

internal class OpenChannelVideoFileMessageViewHolder internal constructor(
    val binding: SbViewOpenChannelFileVideoMessageBinding,
    messageListUIParams: MessageListUIParams
) : MessageViewHolder(binding.root, messageListUIParams) {

    override fun bind(channel: BaseChannel, message: BaseMessage, messageListUIParams: MessageListUIParams) {
        binding.openChannelVideoFileMessageView.messageUIConfig = messageUIConfig
        if (channel is OpenChannel) {
            binding.openChannelVideoFileMessageView.drawMessage(channel, message, messageListUIParams)
        }
    }

    override fun getClickableViewMap(): Map<String, View> {
        return mapOf(
            ClickableViewIdentifier.Chat.name to binding.openChannelVideoFileMessageView.binding.ivThumbnailOverlay,
            ClickableViewIdentifier.Profile.name to binding.openChannelVideoFileMessageView.binding.ivProfileView
        )
    }
}
