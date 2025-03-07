package com.jet.im.kit.internal.ui.reactions

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import androidx.recyclerview.widget.GridLayoutManager
import com.sendbird.android.message.Emoji
import com.sendbird.android.message.Reaction
import com.jet.im.kit.activities.adapter.EmojiReactionListAdapter
import com.jet.im.kit.databinding.SbViewEmojiReactionListBinding
import com.jet.im.kit.interfaces.OnItemClickListener
import com.jet.im.kit.interfaces.OnItemLongClickListener
import com.jet.im.kit.model.EmojiManager
import com.jet.im.kit.model.EmojiManager2
import com.juggle.im.model.MessageReactionItem
import kotlin.math.min

internal class EmojiReactionListView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
) : FrameLayout(context, attrs) {
    val binding: SbViewEmojiReactionListBinding
    val layout: EmojiReactionListView
        get() = this

    private val adapter: EmojiReactionListAdapter
    private val layoutManager: GridLayoutManager
    var maxSpanSize: Int = 4

    init {
        binding = SbViewEmojiReactionListBinding.inflate(LayoutInflater.from(context), this, true)
        binding.rvEmojiReactionList.setUseDivider(false)
        layoutManager = GridLayoutManager(context, maxSpanSize)
        binding.rvEmojiReactionList.layoutManager = layoutManager
        binding.rvEmojiReactionList.setHasFixedSize(true)
        adapter = EmojiReactionListAdapter()
        binding.rvEmojiReactionList.adapter = adapter
    }

    fun setReactionList(reactionList: List<MessageReactionItem?>, totalEmojiList: List<String> = EmojiManager2.emojiList) {
        adapter.setTotalEmojiList(totalEmojiList)
        adapter.setReactionList(reactionList.filterNotNull())
        resetSpanSize()
    }

    private fun resetSpanSize() {
        val itemSize = adapter.itemCount
        if (itemSize > 0) {
            layoutManager.spanCount = min(itemSize, maxSpanSize)
        }
    }

    fun setEmojiReactionClickListener(emojiReactionClickListener: OnItemClickListener<String>?) {
        adapter.setEmojiReactionClickListener(emojiReactionClickListener)
    }

    fun setEmojiReactionLongClickListener(emojiReactionLongClickListener: OnItemLongClickListener<String>?) {
        adapter.setEmojiReactionLongClickListener(emojiReactionLongClickListener)
    }

    fun setMoreButtonClickListener(moreButtonClickListener: OnClickListener?) {
        adapter.setMoreButtonClickListener(moreButtonClickListener)
    }

    internal fun setClickListeners(
        emojiReactionClickListener: OnItemClickListener<String>?,
        emojiReactionLongClickListener: OnItemLongClickListener<String>?,
        moreButtonClickListener: OnClickListener?
    ) {
        setEmojiReactionClickListener(emojiReactionClickListener)
        setEmojiReactionLongClickListener(emojiReactionLongClickListener)
        setMoreButtonClickListener(moreButtonClickListener)
    }

    fun setUseMoreButton(useMoreButton: Boolean) {
        adapter.setUseMoreButton(useMoreButton)
    }

    fun useMoreButton(): Boolean {
        return adapter.useMoreButton()
    }

    fun refresh() {
        resetSpanSize()
        adapter.notifyItemRangeChanged(0, adapter.itemCount)
    }

    override fun setClickable(clickable: Boolean) {
        super.setClickable(clickable)
        adapter.setClickable(clickable)
    }

    override fun setLongClickable(clickable: Boolean) {
        super.setLongClickable(clickable)
        adapter.setClickable(clickable)
    }
}
