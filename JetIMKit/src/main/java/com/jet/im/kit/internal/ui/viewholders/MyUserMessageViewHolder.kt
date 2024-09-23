package com.jet.im.kit.internal.ui.viewholders

import android.view.View
import com.sendbird.android.channel.BaseChannel
import com.sendbird.android.channel.GroupChannel
import com.sendbird.android.message.BaseMessage
import com.sendbird.android.message.Reaction
import com.sendbird.android.user.User
import com.jet.im.kit.activities.viewholder.GroupChannelMessageViewHolder
import com.jet.im.kit.consts.ClickableViewIdentifier
import com.jet.im.kit.databinding.SbViewMyUserMessageBinding
import com.jet.im.kit.interfaces.OnItemClickListener
import com.jet.im.kit.interfaces.OnItemLongClickListener
import com.jet.im.kit.model.MessageListUIParams
import com.juggle.im.model.ConversationInfo
import com.juggle.im.model.Message

internal class MyUserMessageViewHolder internal constructor(
    val binding: SbViewMyUserMessageBinding,
    messageListUIParams: MessageListUIParams
) : GroupChannelMessageViewHolder(binding.root, messageListUIParams) {

    override fun bind(channel: ConversationInfo, message: Message, params: MessageListUIParams) {
        binding.myUserMessage.messageUIConfig = messageUIConfig
        binding.myUserMessage.drawMessage(channel, message, params)
    }

    override fun setEmojiReaction(
        reactionList: List<Reaction>,
        emojiReactionClickListener: OnItemClickListener<String>?,
        emojiReactionLongClickListener: OnItemLongClickListener<String>?,
        moreButtonClickListener: View.OnClickListener?
    ) {
    }

    override fun getClickableViewMap(): Map<String, View> {
        return mapOf(
            ClickableViewIdentifier.Chat.name to binding.myUserMessage.binding.contentPanel,
        )
    }

    fun setOnMentionClickListener(listener: OnItemClickListener<User>?) {
        binding.myUserMessage.mentionClickListener = listener
    }
}
