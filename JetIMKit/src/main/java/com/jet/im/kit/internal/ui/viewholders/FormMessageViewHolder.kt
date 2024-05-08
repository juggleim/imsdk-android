package com.jet.im.kit.internal.ui.viewholders

import android.view.View
import com.sendbird.android.channel.BaseChannel
import com.sendbird.android.message.BaseMessage
import com.sendbird.android.message.UserMessage
import com.jet.im.kit.activities.viewholder.MessageViewHolder
import com.jet.im.kit.databinding.SbViewFormMessageBinding
import com.jet.im.kit.interfaces.FormSubmitButtonClickListener
import com.jet.im.kit.model.MessageListUIParams

internal class FormMessageViewHolder internal constructor(
    val binding: SbViewFormMessageBinding,
    messageListUIParams: MessageListUIParams
) : MessageViewHolder(binding.root, messageListUIParams) {
    var onSubmitClickListener: FormSubmitButtonClickListener? = null
    override fun bind(channel: BaseChannel, message: BaseMessage, messageListUIParams: MessageListUIParams) {
        if (message !is UserMessage) return
        val form = message.forms.firstOrNull() ?: return
        binding.formsMessageView.messageUIConfig = messageUIConfig
        binding.formsMessageView.drawFormMessage(message, messageListUIParams)
        binding.formsMessageView.setSubmitButtonClickListener {
            onSubmitClickListener?.onClicked(message, form)
        }
    }

    override fun getClickableViewMap(): Map<String, View> = mapOf()
}
