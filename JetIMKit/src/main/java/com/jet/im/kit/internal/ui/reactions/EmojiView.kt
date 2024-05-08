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
import com.sendbird.android.message.Emoji
import com.jet.im.kit.R
import com.jet.im.kit.databinding.SbViewEmojiComponentBinding
import com.jet.im.kit.model.EmojiManager
import com.jet.im.kit.utils.DrawableUtils

internal class EmojiView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = R.attr.sb_widget_emoji_message
) : FrameLayout(context, attrs, defStyleAttr) {
    val binding: SbViewEmojiComponentBinding
    val layout: View
        get() = binding.root
    private val emojiFailedDrawableRes: Int
    private val emojiFailedDrawableResTint: ColorStateList?

    init {
        val a =
            context.theme.obtainStyledAttributes(attrs, R.styleable.Emoji, defStyleAttr, R.style.Widget_Sendbird_Emoji)
        try {
            binding = SbViewEmojiComponentBinding.inflate(LayoutInflater.from(getContext()), this, true)
            val backgroundResource =
                a.getResourceId(R.styleable.Emoji_sb_emoji_background, R.drawable.sb_emoji_background_light)
            emojiFailedDrawableRes = a.getResourceId(R.styleable.Emoji_sb_emoji_failed_src, R.drawable.icon_question)
            emojiFailedDrawableResTint = a.getColorStateList(R.styleable.Emoji_sb_emoji_failed_src_tint)
            binding.emojiPanel.setBackgroundResource(backgroundResource)
            val failedDrawable: Drawable? = emojiFailedDrawableResTint?.let {
                DrawableUtils.setTintList(context, emojiFailedDrawableRes, emojiFailedDrawableResTint)
            } ?: AppCompatResources.getDrawable(getContext(), emojiFailedDrawableRes)
            binding.ivEmoji.setImageDrawable(failedDrawable)
        } finally {
            a.recycle()
        }
    }

    override fun setBackgroundResource(backgroundResource: Int) {
        binding.emojiPanel.setBackgroundResource(backgroundResource)
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

    fun drawEmoji(emoji: Emoji) {
        setEmojiUrl(EmojiManager.getEmojiUrl(emoji.key))
    }
}
