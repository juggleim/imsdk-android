package com.jet.im.kit.internal.ui.viewholders

import androidx.appcompat.content.res.AppCompatResources
import com.jet.im.kit.R
import com.jet.im.kit.activities.viewholder.BaseViewHolder
import com.jet.im.kit.internal.ui.reactions.EmojiReactionView
import com.jet.im.kit.utils.DrawableUtils
import com.juggle.im.model.MessageReactionItem

internal class EmojiReactionMoreViewHolder(view: EmojiReactionView) : BaseViewHolder<MessageReactionItem>(view) {
    init {
        val a = view.context
            .theme
            .obtainStyledAttributes(
                null,
                R.styleable.EmojiReaction,
                R.attr.sb_widget_emoji_message,
                R.style.Widget_Sendbird_Emoji
            )
        try {
            val backgroundRes = a.getResourceId(
                R.styleable.EmojiReaction_sb_emoji_reaction_background,
                R.drawable.sb_emoji_reaction_background_light
            )
            val moreRes = a.getResourceId(
                R.styleable.EmojiReaction_sb_emoji_reaction_more_button_src,
                R.drawable.icon_emoji_more
            )
            val moreResTint = a.getColorStateList(R.styleable.EmojiReaction_sb_emoji_reaction_more_button_src_tint)
            view.setBackgroundResource(backgroundRes)
            moreResTint?.let { view.setEmoji("...") }
            view.setCount(0)
        } finally {
            a.recycle()
        }
    }

    override fun bind(item: MessageReactionItem) {}
}
