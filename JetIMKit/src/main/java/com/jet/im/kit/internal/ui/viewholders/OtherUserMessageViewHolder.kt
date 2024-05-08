package com.jet.im.kit.internal.ui.viewholders

import android.view.View
import com.sendbird.android.channel.BaseChannel
import com.sendbird.android.channel.GroupChannel
import com.sendbird.android.message.BaseMessage
import com.sendbird.android.message.Reaction
import com.sendbird.android.user.User
import com.jet.im.kit.activities.viewholder.GroupChannelMessageViewHolder
import com.jet.im.kit.consts.ClickableViewIdentifier
import com.jet.im.kit.databinding.SbViewOtherUserMessageBinding
import com.jet.im.kit.interfaces.OnItemClickListener
import com.jet.im.kit.interfaces.OnItemLongClickListener
import com.jet.im.kit.internal.interfaces.OnFeedbackRatingClickListener
import com.jet.im.kit.model.MessageListUIParams

internal class OtherUserMessageViewHolder internal constructor(
    val binding: SbViewOtherUserMessageBinding,
    messageListUIParams: MessageListUIParams
) : GroupChannelMessageViewHolder(binding.root, messageListUIParams) {

    override fun bind(channel: BaseChannel, message: BaseMessage, messageListUIParams: MessageListUIParams) {
        binding.otherMessageView.messageUIConfig = messageUIConfig
        if (channel is GroupChannel) {
            binding.otherMessageView.drawMessage(channel, message, messageListUIParams)
        }
    }

    override fun setEmojiReaction(
        reactionList: List<Reaction>,
        emojiReactionClickListener: OnItemClickListener<String>?,
        emojiReactionLongClickListener: OnItemLongClickListener<String>?,
        moreButtonClickListener: View.OnClickListener?
    ) {
        binding.otherMessageView.binding.rvEmojiReactionList.apply {
            setReactionList(reactionList)
            setEmojiReactionClickListener(emojiReactionClickListener)
            setEmojiReactionLongClickListener(emojiReactionLongClickListener)
            setMoreButtonClickListener(moreButtonClickListener)
        }
    }

    override fun getClickableViewMap(): Map<String, View> {
        return mapOf(
            ClickableViewIdentifier.Chat.name to binding.otherMessageView.binding.contentPanel,
            ClickableViewIdentifier.Profile.name to binding.otherMessageView.binding.ivProfileView,
            ClickableViewIdentifier.QuoteReply.name to binding.otherMessageView.binding.quoteReplyPanel,
            ClickableViewIdentifier.ThreadInfo.name to binding.otherMessageView.binding.threadInfo
        )
    }

    fun setOnMentionClickListener(listener: OnItemClickListener<User>?) {
        binding.otherMessageView.mentionClickListener = listener
    }

    fun setOnFeedbackRatingClickListener(listener: OnFeedbackRatingClickListener?) {
        binding.otherMessageView.onFeedbackRatingClickListener = listener
    }
}
