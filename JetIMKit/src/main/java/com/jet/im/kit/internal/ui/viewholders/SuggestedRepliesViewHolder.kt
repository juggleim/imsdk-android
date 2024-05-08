package com.jet.im.kit.internal.ui.viewholders

import android.view.View
import com.sendbird.android.channel.BaseChannel
import com.sendbird.android.message.BaseMessage
import com.jet.im.kit.activities.viewholder.MessageViewHolder
import com.jet.im.kit.databinding.SbViewSuggestedRepliesMessageBinding
import com.jet.im.kit.interfaces.OnItemClickListener
import com.jet.im.kit.model.MessageListUIParams
import com.jet.im.kit.model.SuggestedRepliesMessage

internal class SuggestedRepliesViewHolder internal constructor(
    val binding: SbViewSuggestedRepliesMessageBinding,
    messageListUIParams: MessageListUIParams
) : MessageViewHolder(binding.root, messageListUIParams) {
    var suggestedRepliesClickedListener: OnItemClickListener<String>? = null

    override fun bind(channel: BaseChannel, message: BaseMessage, messageListUIParams: MessageListUIParams) {
        binding.suggestedRepliesMessageView.messageUIConfig = messageUIConfig
        if (message is SuggestedRepliesMessage) {
            binding.suggestedRepliesMessageView.drawSuggestedReplies(message)
            binding.suggestedRepliesMessageView.onItemClickListener = OnItemClickListener { view, position, data ->
                suggestedRepliesClickedListener?.onItemClick(view, position, data)
            }
        }
    }

    override fun getClickableViewMap(): Map<String, View> = mapOf()
}
