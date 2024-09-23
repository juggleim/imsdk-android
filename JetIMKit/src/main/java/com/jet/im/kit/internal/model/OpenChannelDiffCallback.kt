package com.jet.im.kit.internal.model

import androidx.recyclerview.widget.DiffUtil

internal class OpenChannelDiffCallback constructor(
    private val oldOpenChannelList: List<OpenChannelInfo>,
    private val newOpenChannelList: List<OpenChannelInfo>
) : DiffUtil.Callback() {

    override fun getOldListSize() = oldOpenChannelList.size

    override fun getNewListSize() = newOpenChannelList.size

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        val oldChannelInfo: OpenChannelInfo = oldOpenChannelList[oldItemPosition]
        val newChannelInfo: OpenChannelInfo = newOpenChannelList[newItemPosition]
        if (newChannelInfo.channelUrl != oldChannelInfo.channelUrl) {
            return false
        }
        return newChannelInfo.createdAt == oldChannelInfo.createdAt
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldOpenChannelList[oldItemPosition] == newOpenChannelList[newItemPosition]
    }
}
