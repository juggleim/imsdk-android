package com.jet.im.kit.internal.ui.viewholders

import android.view.View
import com.jet.im.kit.activities.viewholder.GroupChannelMessageViewHolder
import com.jet.im.kit.consts.ClickableViewIdentifier
import com.jet.im.kit.databinding.SbViewOtherContactCardMessageBinding
import com.jet.im.kit.interfaces.OnItemClickListener
import com.jet.im.kit.interfaces.OnItemLongClickListener
import com.jet.im.kit.model.MessageListUIParams
import com.juggle.im.model.ConversationInfo
import com.juggle.im.model.Message
import com.juggle.im.model.MessageReactionItem

class OtherContactCardMessageViewHolder internal constructor(
    val binding: SbViewOtherContactCardMessageBinding,
    messageListUIParams: MessageListUIParams
): GroupChannelMessageViewHolder(binding.root, messageListUIParams) {

    override fun bind(
        channel: ConversationInfo,
        message: Message,
        reactionItemList: MutableList<MessageReactionItem>,
        params: MessageListUIParams
    ) {
        binding.otherContactCardMessageView.messageUIConfig = messageUIConfig
        binding.otherContactCardMessageView.drawMessage(channel, message, reactionItemList, params)
    }

    override fun setEmojiReaction(
        reactionList: List<MessageReactionItem>,
        emojiReactionClickListener: OnItemClickListener<String>?,
        emojiReactionLongClickListener: OnItemLongClickListener<String>?,
        moreButtonClickListener: View.OnClickListener?
    ) {
    }

    override fun setEmojiReaction(
        reactionList: List<MessageReactionItem>,
        totalEmojiList: List<String>,
        emojiReactionClickListener: OnItemClickListener<String>?,
        emojiReactionLongClickListener: OnItemLongClickListener<String>?,
        moreButtonClickListener: View.OnClickListener?
    ) {
        binding.otherContactCardMessageView.binding.rvEmojiReactionList.apply {
            setReactionList(reactionList, totalEmojiList)
            setClickListeners(emojiReactionClickListener, emojiReactionLongClickListener, moreButtonClickListener)
        }
    }

    override fun getClickableViewMap(): Map<String, View> {
        return mapOf(
            ClickableViewIdentifier.Chat.name to binding.otherContactCardMessageView.binding.contentPanel,
        )
    }
}