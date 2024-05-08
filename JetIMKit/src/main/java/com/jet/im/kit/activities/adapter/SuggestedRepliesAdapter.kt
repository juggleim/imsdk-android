package com.jet.im.kit.activities.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import com.jet.im.kit.activities.viewholder.BaseViewHolder
import com.jet.im.kit.databinding.SbViewSuggestedReplyBinding
import com.jet.im.kit.interfaces.OnItemClickListener

internal class SuggestedRepliesAdapter : BaseAdapter<String, BaseViewHolder<String>>() {
    var onItemClickListener: OnItemClickListener<String>? = null
    var suggestedReplies: List<String> = listOf()
        set(value) {
            val diffResult = DiffUtil.calculateDiff(SuggestedReplyDiffCallback(field, value))
            field = value
            diffResult.dispatchUpdatesTo(this)
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder<String> {
        return SuggestedReplyViewHolder(
            SbViewSuggestedReplyBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        ).apply {
            this.binding.suggestedReplyView.setOnClickListener {
                val item = getItem(absoluteAdapterPosition)
                val index = suggestedReplies.indexOf(item)
                if (index == -1) return@setOnClickListener
                onItemClickListener?.onItemClick(binding.root, index, item)
            }
        }
    }

    override fun getItemCount(): Int {
        return suggestedReplies.size
    }

    override fun getItem(position: Int): String {
        return suggestedReplies[position]
    }

    override fun getItems(): List<String> {
        return suggestedReplies.toList()
    }

    override fun onBindViewHolder(holder: BaseViewHolder<String>, position: Int) {
        holder.bind(getItem(position))
    }

    private class SuggestedReplyViewHolder(
        val binding: SbViewSuggestedReplyBinding
    ) : BaseViewHolder<String>(binding.root) {
        override fun bind(item: String) {
            binding.suggestedReplyView.drawSuggestedReplies(item)
        }
    }

    private class SuggestedReplyDiffCallback(
        private val oldList: List<String>,
        private val newList: List<String>
    ) : DiffUtil.Callback() {
        override fun getOldListSize(): Int {
            return oldList.size
        }

        override fun getNewListSize(): Int {
            return newList.size
        }

        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldList[oldItemPosition] == newList[newItemPosition]
        }

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldList[oldItemPosition] == newList[newItemPosition]
        }
    }
}
