package com.jet.im.kit.internal.ui.viewholders

import com.sendbird.android.message.Reaction
import com.jet.im.kit.activities.viewholder.BaseViewHolder
import com.jet.im.kit.databinding.SbViewEmojiReactionBinding
import com.juggle.im.model.MessageReactionItem

internal class EmojiReactionViewHolder(
    private val binding: SbViewEmojiReactionBinding
) : BaseViewHolder<MessageReactionItem>(binding.root) {
    override fun bind(item: MessageReactionItem) {
        binding.emojiReactionView.drawReaction(item)
    }
}
