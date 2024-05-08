package com.jet.im.kit.internal.ui.reactions

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import androidx.appcompat.content.res.AppCompatResources
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.sendbird.android.message.Reaction
import com.jet.im.kit.R
import com.jet.im.kit.databinding.SbViewEmojiReactionComponentBinding
import com.jet.im.kit.internal.extensions.setAppearance
import com.jet.im.kit.model.EmojiManager
import com.jet.im.kit.utils.DrawableUtils

internal class EmojiReactionView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = R.attr.sb_widget_emoji_message
) : FrameLayout(context, attrs, defStyleAttr) {
    val binding: SbViewEmojiReactionComponentBinding
    val layout: View
        get() = binding.root

    private val emojiFailedDrawableRes: Int
    private val emojiFailedDrawableResTint: ColorStateList?

    init {
        val a = context.theme.obtainStyledAttributes(
            attrs,
            R.styleable.EmojiReaction,
            defStyleAttr,
            R.style.Widget_Sendbird_Emoji
        )
        try {
            binding = SbViewEmojiReactionComponentBinding.inflate(LayoutInflater.from(getContext()), this, true)
            val backgroundRes = a.getResourceId(
                R.styleable.EmojiReaction_sb_emoji_reaction_background,
                R.drawable.sb_emoji_reaction_background_light
            )
            val textStyle = a.getResourceId(
                R.styleable.EmojiReaction_sb_emoji_reaction_text_appearance,
                R.style.SendbirdCaption4OnLight01
            )
            emojiFailedDrawableRes =
                a.getResourceId(R.styleable.EmojiReaction_sb_emoji_failed_src, R.drawable.icon_question)
            emojiFailedDrawableResTint = a.getColorStateList(R.styleable.EmojiReaction_sb_emoji_failed_src_tint)
            binding.root.setBackgroundResource(backgroundRes)
            binding.tvCount.setAppearance(context, textStyle)
        } finally {
            a.recycle()
        }
    }

    override fun setBackgroundResource(backgroundResource: Int) {
        binding.root.setBackgroundResource(backgroundResource)
    }

    fun setCount(count: Int) {
        if (count <= 0) {
            binding.empty.visibility = GONE
            binding.tvCount.visibility = GONE
        } else {
            binding.empty.visibility = VISIBLE
            binding.tvCount.visibility = VISIBLE
            val countText =
                if (count > 99) context.getString(R.string.sb_text_channel_reaction_count_max) else count.toString()
            binding.tvCount.text = countText
        }
    }

    fun setImageDrawable(drawable: Drawable?) {
        binding.ivEmoji.setImageDrawable(drawable)
    }

    private fun setEmojiUrl(emojiUrl: String?) {
        val overrideSize = resources
            .getDimensionPixelSize(R.dimen.sb_size_38)
        val failedDrawable: Drawable? = emojiFailedDrawableResTint?.let {
            DrawableUtils.setTintList(context, emojiFailedDrawableRes, emojiFailedDrawableResTint)
        } ?: AppCompatResources.getDrawable(context, emojiFailedDrawableRes)
        Glide.with(binding.ivEmoji)
            .load(emojiUrl)
            .override(overrideSize, overrideSize)
            .centerCrop()
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .error(failedDrawable)
            .placeholder(failedDrawable)
            .into(binding.ivEmoji)
    }

    fun drawReaction(reaction: Reaction) {
        setCount(reaction.userIds.size)
        setEmojiUrl(EmojiManager.getEmojiUrl(reaction.key))
    }
}
