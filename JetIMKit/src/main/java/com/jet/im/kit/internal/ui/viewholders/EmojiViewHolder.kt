package com.jet.im.kit.internal.ui.viewholders

import com.sendbird.android.message.Emoji
import com.jet.im.kit.activities.viewholder.BaseViewHolder
import com.jet.im.kit.databinding.SbViewEmojiBinding

internal class EmojiViewHolder(
    private val binding: SbViewEmojiBinding
) : BaseViewHolder<String>(binding.root) {
    override fun bind(item: String) {
        binding.emojiView.drawEmoji(item)
    }
}
