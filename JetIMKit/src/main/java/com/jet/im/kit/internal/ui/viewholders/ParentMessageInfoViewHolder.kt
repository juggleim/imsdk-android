package com.jet.im.kit.internal.ui.viewholders

import android.view.View
import com.sendbird.android.channel.BaseChannel
import com.sendbird.android.channel.GroupChannel
import com.sendbird.android.message.BaseMessage
import com.sendbird.android.message.Reaction
import com.sendbird.android.user.User
import com.jet.im.kit.activities.viewholder.GroupChannelMessageViewHolder
import com.jet.im.kit.consts.ClickableViewIdentifier
import com.jet.im.kit.databinding.SbViewParentMessageInfoHolderBinding
import com.jet.im.kit.interfaces.OnItemClickListener
import com.jet.im.kit.interfaces.OnItemLongClickListener
import com.jet.im.kit.model.MessageListUIParams

internal class ParentMessageInfoViewHolder(val binding: SbViewParentMessageInfoHolderBinding) :
    GroupChannelMessageViewHolder(binding.root) {

    override fun bind(channel: BaseChannel, message: BaseMessage, params: MessageListUIParams) {
        binding.parentMessageInfoView.drawMessage((channel as GroupChannel), message, params)
    }

    override fun setEmojiReaction(
        reactionList: List<Reaction>,
        emojiReactionClickListener: OnItemClickListener<String>?,
        emojiReactionLongClickListener: OnItemLongClickListener<String>?,
        moreButtonClickListener: View.OnClickListener?
    ) {
        binding.parentMessageInfoView.binding.rvEmojiReactionList.apply {
            setReactionList(reactionList)
            setEmojiReactionClickListener(emojiReactionClickListener)
            setEmojiReactionLongClickListener(emojiReactionLongClickListener)
            setMoreButtonClickListener(moreButtonClickListener)
        }
    }

    override fun getClickableViewMap(): Map<String, View> {
        return mapOf(
            ClickableViewIdentifier.ParentMessageMenu.name to binding.parentMessageInfoView.binding.ivMoreIcon,
            ClickableViewIdentifier.Chat.name to binding.parentMessageInfoView.binding.contentPanel
        )
    }

    fun setOnMentionClickListener(listener: OnItemClickListener<User>?) {
        binding.parentMessageInfoView.mentionClickListener = listener
    }
}
