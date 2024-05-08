package com.jet.im.kit.internal.ui.messages

import android.content.Context
import android.graphics.Rect
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.jet.im.kit.R
import com.jet.im.kit.activities.adapter.SuggestedRepliesAdapter
import com.jet.im.kit.databinding.SbViewSuggestedRepliesMessageComponentBinding
import com.jet.im.kit.interfaces.OnItemClickListener
import com.jet.im.kit.model.SuggestedRepliesMessage

internal class SuggestedRepliesMessageView @JvmOverloads internal constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : BaseMessageView(context, attrs, defStyle) {
    var onItemClickListener: OnItemClickListener<String>? = null
    private val suggestedRepliesAdapter = SuggestedRepliesAdapter()
    override val binding: SbViewSuggestedRepliesMessageComponentBinding = SbViewSuggestedRepliesMessageComponentBinding.inflate(
        LayoutInflater.from(getContext()),
        this,
        true
    )

    override val layout: View
        get() = binding.root

    init {
        binding.rvSuggestedReplies.layoutManager = LinearLayoutManager(context)
        val spacing = resources.getDimensionPixelSize(R.dimen.sb_size_8)
        binding.rvSuggestedReplies.addItemDecoration(LinearLayoutManagerItemDecoration(spacing))
        binding.rvSuggestedReplies.adapter = suggestedRepliesAdapter
        suggestedRepliesAdapter.onItemClickListener = OnItemClickListener { v, position, data ->
            onItemClickListener?.onItemClick(v, position, data)
        }
    }

    fun drawSuggestedReplies(message: SuggestedRepliesMessage) {
        suggestedRepliesAdapter.suggestedReplies = message.anchor.suggestedReplies
    }

    private class LinearLayoutManagerItemDecoration(
        private val spacing: Int
    ) : RecyclerView.ItemDecoration() {
        override fun getItemOffsets(
            outRect: Rect,
            view: View,
            parent: RecyclerView,
            state: RecyclerView.State
        ) {
            super.getItemOffsets(outRect, view, parent, state)
            if (parent.layoutManager is LinearLayoutManager) {
                val layoutManager = parent.layoutManager as LinearLayoutManager
                if (layoutManager.orientation == LinearLayoutManager.VERTICAL) {
                    outRect.bottom = spacing
                } else {
                    outRect.right = spacing
                }
            }
        }
    }
}
