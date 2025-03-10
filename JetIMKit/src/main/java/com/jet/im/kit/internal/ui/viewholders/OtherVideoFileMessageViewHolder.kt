package com.jet.im.kit.internal.ui.viewholders

import android.view.View
import com.sendbird.android.channel.BaseChannel
import com.sendbird.android.channel.GroupChannel
import com.sendbird.android.message.BaseMessage
import com.sendbird.android.message.Reaction
import com.jet.im.kit.activities.viewholder.GroupChannelMessageViewHolder
import com.jet.im.kit.consts.ClickableViewIdentifier
import com.jet.im.kit.databinding.SbViewOtherFileVideoMessageBinding
import com.jet.im.kit.interfaces.OnItemClickListener
import com.jet.im.kit.interfaces.OnItemLongClickListener
import com.jet.im.kit.model.MessageListUIParams
import com.juggle.im.model.ConversationInfo
import com.juggle.im.model.Message
import com.juggle.im.model.MessageReactionItem
import com.sendbird.android.message.Emoji

internal class OtherVideoFileMessageViewHolder internal constructor(
    val binding: SbViewOtherFileVideoMessageBinding,
    messageListUIParams: MessageListUIParams
) : GroupChannelMessageViewHolder(binding.root, messageListUIParams) {

    override fun bind(channel: ConversationInfo, message: Message, reactionList: List<MessageReactionItem>, params: MessageListUIParams){
        binding.otherVideoFileMessageView.messageUIConfig = messageUIConfig
        binding.otherVideoFileMessageView.drawMessage(channel, message, reactionList, params)
    }

    override fun setEmojiReaction(
        reactionList: List<MessageReactionItem>,
        totalEmojiList: List<String>,
        emojiReactionClickListener: OnItemClickListener<String>?,
        emojiReactionLongClickListener: OnItemLongClickListener<String>?,
        moreButtonClickListener: View.OnClickListener?
    ) {
        binding.otherVideoFileMessageView.binding.rvEmojiReactionList.apply {
            setReactionList(reactionList, totalEmojiList)
            setClickListeners(emojiReactionClickListener, emojiReactionLongClickListener, moreButtonClickListener)
        }
    }

    override fun getClickableViewMap(): Map<String, View> {
        return mapOf(
            ClickableViewIdentifier.Chat.name to binding.otherVideoFileMessageView.binding.ivThumbnailOverlay,
            ClickableViewIdentifier.Profile.name to binding.otherVideoFileMessageView.binding.ivProfileView,
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
