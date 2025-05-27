package com.jet.im.kit.internal.ui.viewholders

import android.view.View
import com.jet.im.kit.activities.viewholder.GroupChannelMessageViewHolder
import com.jet.im.kit.consts.ClickableViewIdentifier
import com.jet.im.kit.databinding.SbViewOtherFileImageMessageBinding
import com.jet.im.kit.interfaces.OnItemClickListener
import com.jet.im.kit.interfaces.OnItemLongClickListener
import com.jet.im.kit.model.MessageListUIParams
import com.juggle.im.model.ConversationInfo
import com.juggle.im.model.Message
import com.juggle.im.model.MessageReactionItem

internal class OtherImageFileMessageViewHolder internal constructor(
    val binding: SbViewOtherFileImageMessageBinding,
    messageListUIParams: MessageListUIParams
) : GroupChannelMessageViewHolder(binding.root, messageListUIParams) {

    override fun bind(channel: ConversationInfo, message: Message, reactionList: List<MessageReactionItem>, params: MessageListUIParams) {
        binding.otherImageFileMessageView.messageUIConfig = messageUIConfig
        binding.otherImageFileMessageView.drawMessage(channel, message, reactionList, params)
    }

    override fun setEmojiReaction(
        reactionList: List<MessageReactionItem>,
        totalEmojiList: List<String>,
        emojiReactionClickListener: OnItemClickListener<String>?,
        emojiReactionLongClickListener: OnItemLongClickListener<String>?,
        moreButtonClickListener: View.OnClickListener?
    ) {
        binding.otherImageFileMessageView.binding.rvEmojiReactionList.apply {
            setReactionList(reactionList, totalEmojiList)
            setClickListeners(emojiReactionClickListener, emojiReactionLongClickListener, moreButtonClickListener)
        }
    }

    override fun getClickableViewMap(): Map<String, View> {
        return mapOf(
            ClickableViewIdentifier.Chat.name to binding.otherImageFileMessageView.binding.ivThumbnailOverlay,
            ClickableViewIdentifier.Profile.name to binding.otherImageFileMessageView.binding.ivProfileView,
        )
    }

    override fun setEmojiReaction(
        reactionList: MutableList<out MessageReactionItem>,
        emojiReactionClickListener: OnItemClickListener<String>?,
        emojiReactionLongClickListener: OnItemLongClickListener<String>?,
        moreButtonClickListener: View.OnClickListener?
    ) {
    }
}
