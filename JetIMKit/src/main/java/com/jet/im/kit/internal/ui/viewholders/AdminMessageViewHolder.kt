package com.jet.im.kit.internal.ui.viewholders

import android.view.View
import com.sendbird.android.channel.BaseChannel
import com.sendbird.android.message.BaseMessage
import com.jet.im.kit.activities.viewholder.MessageViewHolder
import com.jet.im.kit.databinding.SbViewAdminMessageBinding
import com.jet.im.kit.internal.ui.messages.AdminMessageView
import com.jet.im.kit.model.MessageListUIParams

internal class AdminMessageViewHolder constructor(
    binding: SbViewAdminMessageBinding,
    messageListUIParams: MessageListUIParams
) : MessageViewHolder(binding.root, messageListUIParams) {
    private val adminMessageView: AdminMessageView

    init {
        adminMessageView = binding.adminMessageView
    }

    override fun bind(channel: BaseChannel, message: BaseMessage, params: MessageListUIParams) {
        adminMessageView.messageUIConfig = messageUIConfig
        adminMessageView.drawMessage(message)
    }

    override fun getClickableViewMap(): Map<String, View> = mapOf()
}
