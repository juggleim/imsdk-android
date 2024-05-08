package com.jet.im.kit.internal.ui.reactions

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.drawable.Drawable
import android.text.TextUtils
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import androidx.appcompat.content.res.AppCompatResources
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.jet.im.kit.R
import com.jet.im.kit.databinding.SbViewEmojiReactionCountComponentBinding
import com.jet.im.kit.internal.extensions.setAppearance
import com.jet.im.kit.utils.DrawableUtils

internal class EmojiReactionCountView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = R.attr.sb_widget_emoji_message
) : FrameLayout(context, attrs, defStyleAttr) {
    val layout: EmojiReactionCountView
        get() = this
    val binding: SbViewEmojiReactionCountComponentBinding

    private val emojiFailedDrawableRes: Int
    private val emojiFailedDrawableResTint: ColorStateList?

    init {
        val a = context.theme.obtainStyledAttributes(
            attrs,
            R.styleable.EmojiReactionCount,
            defStyleAttr,
            R.style.Widget_Sendbird_Emoji
        )
        try {
            binding = SbViewEmojiReactionCountComponentBinding.inflate(LayoutInflater.from(context), this, true)
            val textStyleId = a.getResourceId(
                R.styleable.EmojiReactionCount_sb_emoji_reaction_count_text_appearance,
                R.style.SendbirdButtonOnLight03
            )
            emojiFailedDrawableRes =
                a.getResourceId(R.styleable.EmojiReactionCount_sb_emoji_failed_src, R.drawable.icon_question)
            emojiFailedDrawableResTint = a.getColorStateList(R.styleable.EmojiReactionCount_sb_emoji_failed_src_tint)
            binding.tvCount.setAppearance(context, textStyleId)
            // letterSpacing should be 0 to use ellipsize as TextUtils.TruncateAt.MIDDLE
            binding.tvCount.letterSpacing = 0f
            binding.tvCount.ellipsize = TextUtils.TruncateAt.MIDDLE
            binding.tvCount.isSingleLine = true
        } finally {
            a.recycle()
        }
    }

    fun setCount(count: Int) {
        if (count <= 0) {
            binding.tvCount.visibility = GONE
        } else {
            binding.tvCount.visibility = VISIBLE
            val countText =
                if (count > 99) context.getString(R.string.sb_text_channel_reaction_count_max) else count.toString()
            binding.tvCount.text = countText
        }
    }

    fun setEmojiUrl(emojiUrl: String?) {
        val overrideSize = resources.getDimensionPixelSize(R.dimen.sb_size_38)
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
}
