package com.jet.im.kit.internal.ui.viewholders

import com.sendbird.android.message.Reaction
import com.jet.im.kit.activities.viewholder.BaseViewHolder
import com.jet.im.kit.databinding.SbViewEmojiReactionBinding

internal class EmojiReactionViewHolder(
    private val binding: SbViewEmojiReactionBinding
) : BaseViewHolder<Reaction>(binding.root) {
    override fun bind(item: Reaction) {
        binding.emojiReactionView.drawReaction(item)
    }
}
