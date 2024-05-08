package com.jet.im.kit.internal.ui.messages

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.GridLayoutManager.SpanSizeLookup
import com.sendbird.android.message.MultipleFilesMessage
import com.jet.im.kit.R
import com.jet.im.kit.activities.adapter.MultipleFilesAdapter
import com.jet.im.kit.databinding.SbViewMultipleFilesMessageComponentBinding
import com.jet.im.kit.interfaces.OnItemClickListener
import com.jet.im.kit.interfaces.OnItemLongClickListener
import com.jet.im.kit.internal.extensions.setBackgroundColorAndRadius
import com.jet.im.kit.internal.ui.widgets.MultipleFilesMessageItemDecoration

internal class MultipleFilesMessageView @JvmOverloads internal constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : FrameLayout(context, attrs, defStyle) {
    val binding: SbViewMultipleFilesMessageComponentBinding
    var onItemClickListener: OnItemClickListener<MultipleFilesAdapter.ImageFileInfo>? = null
    private val isMine: Boolean
    private val adapter: MultipleFilesAdapter

    init {
        val a = context.theme.obtainStyledAttributes(attrs, R.styleable.MessageView_MultipleFiles, defStyle, 0)
        try {
            binding = SbViewMultipleFilesMessageComponentBinding.inflate(LayoutInflater.from(context), this, true)
            val bgColorStateList =
                a.getColorStateList(R.styleable.MessageView_MultipleFiles_sb_multiple_files_message_background_color)
            isMine = a.getBoolean(R.styleable.MessageView_MultipleFiles_sb_multiple_files_message_is_mine, false)
            val bgRadius = resources.getDimensionPixelSize(R.dimen.sb_size_16)
            binding.root.setBackgroundColorAndRadius(bgColorStateList, bgRadius.toFloat())
            val recyclerView = binding.multipleFilesMessageRecyclerView
            adapter = MultipleFilesAdapter().apply {
                onLongClickListener = OnItemLongClickListener { _, _, _ ->
                    this@MultipleFilesMessageView.performLongClick()
                }

                onItemClickListener = OnItemClickListener { view, position, data ->
                    this@MultipleFilesMessageView.onItemClickListener?.onItemClick(view, position, data)
                }
            }
            val spanCount = 2
            val gridLayoutManager = GridLayoutManager(context, spanCount)
            gridLayoutManager.spanSizeLookup = object : SpanSizeLookup() {
                override fun getSpanSize(position: Int): Int {
                    return if (adapter.itemCount % spanCount != 0 && adapter.itemCount - 1 == position) {
                        spanCount
                    } else {
                        1
                    }
                }
            }
            recyclerView.layoutManager = gridLayoutManager
            recyclerView.addItemDecoration(
                MultipleFilesMessageItemDecoration(
                    spanCount = spanCount,
                    spacing = resources.getDimensionPixelSize(R.dimen.sb_size_4),
                    isMine = isMine,
                    parentRadius = resources.getDimensionPixelSize(R.dimen.sb_size_12),
                    childRadius = resources.getDimensionPixelSize(R.dimen.sb_size_6)
                )
            )
            binding.multipleFilesMessageRecyclerView.adapter = adapter
        } finally {
            a.recycle()
        }
    }

    fun bind(message: MultipleFilesMessage) {
        adapter.setMultipleFilesMessage(message)
    }
}
