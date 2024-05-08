package com.jet.im.kit.activities.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import com.sendbird.android.message.MultipleFilesMessage
import com.sendbird.android.message.Thumbnail
import com.jet.im.kit.activities.viewholder.BaseViewHolder
import com.jet.im.kit.databinding.SbViewImageFileBinding
import com.jet.im.kit.interfaces.OnItemClickListener
import com.jet.im.kit.interfaces.OnItemLongClickListener
import com.jet.im.kit.internal.extensions.getCacheKey
import java.util.Collections

internal class MultipleFilesAdapter :
    BaseAdapter<MultipleFilesAdapter.ImageFileInfo, BaseViewHolder<MultipleFilesAdapter.ImageFileInfo>>() {
    internal var onLongClickListener: OnItemLongClickListener<ImageFileInfo>? = null
    internal var onItemClickListener: OnItemClickListener<ImageFileInfo>? = null
    private val imageFileInfoList: MutableList<ImageFileInfo> = mutableListOf()

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): BaseViewHolder<ImageFileInfo> {
        return ImageFileInfoViewHolder(
            SbViewImageFileBinding.inflate(LayoutInflater.from(parent.context))
        ).apply {
            this.binding.imageFileView.setOnLongClickListener {
                val item = getItem(absoluteAdapterPosition)
                val index = item.index
                if (index == -1) return@setOnLongClickListener false
                onLongClickListener?.onItemLongClick(it, index, item)
                return@setOnLongClickListener true
            }
            this.binding.imageFileView.setOnClickListener {
                val item = getItem(absoluteAdapterPosition)
                val index = item.index
                if (index == -1) return@setOnClickListener
                onItemClickListener?.onItemClick(it, index, item)
            }
        }
    }

    fun setMultipleFilesMessage(message: MultipleFilesMessage) {
        val imageFileInfoList: List<ImageFileInfo> = if (message.files.isNotEmpty()) {
            message.files.mapIndexed { index, it ->
                ImageFileInfo(
                    it.url,
                    it.plainUrl,
                    it.fileType,
                    it.thumbnails,
                    message.getCacheKey(index),
                    index
                )
            }
        } else {
            message.messageCreateParams?.uploadableFileInfoList?.mapIndexed { index, it ->
                val url = it.url
                    .takeIf { url -> !url.isNullOrEmpty() }
                    ?: it.file?.absolutePath
                    ?: ""
                ImageFileInfo(
                    url,
                    url,
                    it.fileType.orEmpty(),
                    listOf(),
                    message.getCacheKey(index),
                    index
                )
            } ?: emptyList()
        }
        val diffResult = DiffUtil.calculateDiff(
            ImageFileInfoDiffCallback(
                this.imageFileInfoList,
                imageFileInfoList
            )
        )
        this.imageFileInfoList.clear()
        this.imageFileInfoList.addAll(imageFileInfoList)
        diffResult.dispatchUpdatesTo(this)
    }

    override fun getItemCount(): Int {
        return imageFileInfoList.size
    }

    override fun getItem(position: Int): ImageFileInfo {
        return imageFileInfoList[position]
    }

    override fun getItems(): List<ImageFileInfo> {
        return Collections.unmodifiableList(imageFileInfoList)
    }

    override fun onBindViewHolder(holder: BaseViewHolder<ImageFileInfo>, position: Int) {
        holder.bind(getItem(position))
    }

    private class ImageFileInfoViewHolder(
        val binding: SbViewImageFileBinding
    ) : BaseViewHolder<ImageFileInfo>(binding.root) {
        override fun bind(item: ImageFileInfo) {
            binding.imageFileView.draw(
                item.url,
                item.plainUrl,
                item.fileType,
                item.thumbnails,
                item.cacheKey
            )
        }
    }

    internal data class ImageFileInfo(
        val url: String,
        val plainUrl: String,
        val fileType: String,
        val thumbnails: List<Thumbnail>,
        val cacheKey: String,
        val index: Int = -1
    )

    internal class ImageFileInfoDiffCallback(
        private val oldFileInfos: List<ImageFileInfo>,
        private val newFileInfos: List<ImageFileInfo>
    ) : DiffUtil.Callback() {
        override fun getOldListSize(): Int = oldFileInfos.size

        override fun getNewListSize(): Int = newFileInfos.size

        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldFileInfos[oldItemPosition].url == newFileInfos[newItemPosition].url
        }

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldFileInfos[oldItemPosition] == newFileInfos[newItemPosition]
        }
    }
}
