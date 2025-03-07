package com.jet.im.kit.internal.ui.reactions

import android.content.Context
import android.view.LayoutInflater
import android.widget.FrameLayout
import com.sendbird.android.message.Emoji
import com.sendbird.android.message.Reaction
import com.jet.im.kit.R
import com.jet.im.kit.activities.adapter.EmojiListAdapter
import com.jet.im.kit.databinding.SbViewEmojiListBinding
import com.jet.im.kit.interfaces.OnItemClickListener
import kotlin.math.min

internal class EmojiListView private constructor(context: Context) : FrameLayout(context) {
    private val binding: SbViewEmojiListBinding
    private lateinit var adapter: EmojiListAdapter
    private val maxHeight: Int

    init {
        binding = SbViewEmojiListBinding.inflate(LayoutInflater.from(context), this, true)
        binding.rvEmojiList.setUseDivider(false)
        maxHeight = context.resources.getDimension(R.dimen.sb_emoji_reaction_dialog_max_height).toInt()
    }

    companion object {
        // TODO (Remove : after all codes are converted as kotlin this annotation doesn't need)
        @JvmStatic
        fun create(
            context: Context,
            emojiList: List<String>,
            reactionList: List<Reaction>? = null,
            showMoreButton: Boolean = false
        ): EmojiListView {
            val emojiListView = EmojiListView(context)
            val adapter = EmojiListAdapter(emojiList, reactionList, showMoreButton)
            emojiListView.adapter = adapter
            emojiListView.binding.rvEmojiList.adapter = adapter
            return emojiListView
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        var heightMeasured = heightMeasureSpec
        if (maxHeight > 0) {
            val hSize = MeasureSpec.getSize(heightMeasured)
            when (MeasureSpec.getMode(heightMeasured)) {
                MeasureSpec.AT_MOST ->
                    heightMeasured =
                        MeasureSpec.makeMeasureSpec(min(hSize, maxHeight), MeasureSpec.AT_MOST)
                MeasureSpec.UNSPECIFIED ->
                    heightMeasured =
                        MeasureSpec.makeMeasureSpec(maxHeight, MeasureSpec.AT_MOST)
                MeasureSpec.EXACTLY ->
                    heightMeasured =
                        MeasureSpec.makeMeasureSpec(min(hSize, maxHeight), MeasureSpec.EXACTLY)
            }
        }
        super.onMeasure(widthMeasureSpec, heightMeasured)
    }

    fun setEmojiClickListener(emojiClickListener: OnItemClickListener<String>?) {
        adapter.setEmojiClickListener(emojiClickListener)
    }

    fun setMoreButtonClickListener(moreButtonClickListener: OnClickListener?) {
        adapter.setMoreButtonClickListener(moreButtonClickListener)
    }
}
