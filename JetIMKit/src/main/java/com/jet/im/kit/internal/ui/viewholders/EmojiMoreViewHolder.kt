package com.jet.im.kit.internal.ui.viewholders

import androidx.appcompat.content.res.AppCompatResources
import com.sendbird.android.message.Emoji
import com.jet.im.kit.R
import com.jet.im.kit.activities.viewholder.BaseViewHolder
import com.jet.im.kit.internal.ui.reactions.EmojiView
import com.jet.im.kit.utils.DrawableUtils

internal class EmojiMoreViewHolder(emojiView: EmojiView) : BaseViewHolder<Emoji>(emojiView) {
    init {
        val a = emojiView.context
            .theme
            .obtainStyledAttributes(
                null,
                R.styleable.Emoji,
                R.attr.sb_widget_emoji_message,
                R.style.Widget_Sendbird_Emoji
            )
        try {
            val backgroundRes = a.getResourceId(
                R.styleable.Emoji_sb_emoji_background,
                R.drawable.sb_emoji_background_light
            )
            val moreRes = a.getResourceId(
                R.styleable.Emoji_sb_emoji_more_button_src,
                R.drawable.icon_emoji_more
            )
            val moreResTint = a.getColorStateList(R.styleable.Emoji_sb_emoji_more_button_src_tint)
            emojiView.setBackgroundResource(backgroundRes)
            moreResTint?.let { emojiView.setImageDrawable(DrawableUtils.setTintList(emojiView.context, moreRes, it)) }
                ?: emojiView.setImageDrawable(AppCompatResources.getDrawable(emojiView.context, moreRes))
        } finally {
            a.recycle()
        }
    }

    override fun bind(item: Emoji) {}
}
