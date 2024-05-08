package com.jet.im.kit.internal.ui.viewholders

import android.view.View
import com.sendbird.android.channel.BaseChannel
import com.sendbird.android.message.BaseMessage
import com.jet.im.kit.activities.viewholder.MessageViewHolder
import com.jet.im.kit.databinding.SbViewTypingIndicatorMessageBinding
import com.jet.im.kit.model.MessageListUIParams
import com.jet.im.kit.model.TypingIndicatorMessage

internal class TypingIndicatorViewHolder internal constructor(
    val binding: SbViewTypingIndicatorMessageBinding,
    messageListUIParams: MessageListUIParams
) : MessageViewHolder(binding.root, messageListUIParams) {

    override fun bind(channel: BaseChannel, message: BaseMessage, messageListUIParams: MessageListUIParams) {
        if (message is TypingIndicatorMessage) {
            binding.typingIndicatorMessageView.updateTypingMembers(message.typingUsers)
        }
    }

    override fun getClickableViewMap(): Map<String, View> = mapOf()
}
