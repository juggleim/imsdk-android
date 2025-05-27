package com.sendbird.uikit.internal.ui.viewholders

import android.view.View
import com.jet.im.kit.activities.viewholder.MessageViewHolder
import com.jet.im.kit.databinding.SbViewAdminMessageBinding
import com.jet.im.kit.model.MessageListUIParams
import com.juggle.im.model.ConversationInfo
import com.juggle.im.model.Message
import com.juggle.im.model.MessageReactionItem
import com.sendbird.android.channel.BaseChannel
import com.sendbird.android.message.BaseMessage
import com.sendbird.uikit.internal.ui.messages.AdminMessageView

internal class AdminMessageViewHolder constructor(
    binding: SbViewAdminMessageBinding,
    messageListUIParams: MessageListUIParams
) : MessageViewHolder(binding.root, messageListUIParams) {
    private val adminMessageView: AdminMessageView

    init {
        adminMessageView = binding.adminMessageView
    }

    override fun bind(channel: ConversationInfo, message: Message, reactionList: List<MessageReactionItem>, params: MessageListUIParams) {
        adminMessageView.messageUIConfig = messageUIConfig
        adminMessageView.drawMessage(message)
    }

    override fun getClickableViewMap(): Map<String, View> = mapOf()
}
