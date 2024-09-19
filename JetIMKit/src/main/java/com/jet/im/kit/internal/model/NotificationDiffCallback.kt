package com.jet.im.kit.internal.model

import androidx.recyclerview.widget.DiffUtil
import com.sendbird.android.message.BaseMessage

internal class NotificationDiffCallback(
    private val oldMessageList: List<BaseMessage>,
    private val newMessageList: List<BaseMessage>,
    private val oldLastSeenAt: Long = 0,
    private val newLastSeenAt: Long = 0
) : DiffUtil.Callback() {
    /**
     * Returns the size of the old list.
     *
     * @return The size of the old list.
     */
    override fun getOldListSize(): Int = oldMessageList.size

    /**
     * Returns the size of the new list.
     *
     * @return The size of the new list.
     */
    override fun getNewListSize(): Int = newMessageList.size

    /**
     * Called by the DiffUtil to decide whether two object represent the same Item.
     *
     *
     * For example, if your items have unique ids, this method should check their id equality.
     *
     * @param oldItemPosition The position of the item in the old list
     * @param newItemPosition The position of the item in the new list
     * @return True if the two items represent the same object or false if they are different.
     */
    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        val oldMessage = oldMessageList[oldItemPosition]
        val newMessage = newMessageList[newItemPosition]
        return oldMessage.messageId == newMessage.messageId
    }

    /**
     * Called by the DiffUtil when it wants to check whether two items have the same data.
     * DiffUtil uses this information to detect if the contents of an item has changed.
     *
     *
     * DiffUtil uses this method to check equality instead of [Object.equals]
     * so that you can change its behavior depending on your UI.
     * For example, if you are using DiffUtil with a
     * [RecyclerView.Adapter], you should
     * return whether the items' visual representations are the same.
     *
     *
     * This method is called only if [.areItemsTheSame] returns
     * `true` for these items.
     *
     * @param oldItemPosition The position of the item in the old list
     * @param newItemPosition The position of the item in the new list which replaces the
     * oldItem
     * @return True if the contents of the items are the same or false if they are different.
     */
    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        val oldMessage = oldMessageList[oldItemPosition]
        val newMessage = newMessageList[newItemPosition]

        if (oldMessage.customType != newMessage.customType) {
            return false
        }

        if (oldMessage.createdAt != newMessage.createdAt) {
            return false
        }

        if (oldMessage.updatedAt != newMessage.updatedAt) {
            return false
        }

        val prevIsNew: Boolean = oldMessage.createdAt > oldLastSeenAt
        val currentIsNew: Boolean = newMessage.createdAt > newLastSeenAt
        if (prevIsNew != currentIsNew) {
            return false
        }
        return true
    }
}
