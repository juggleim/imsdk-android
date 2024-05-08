package com.jet.im.kit.internal.ui.notifications

import android.content.Context
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.view.ContextThemeWrapper
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.sendbird.android.channel.GroupChannel
import com.sendbird.android.message.BaseMessage
import com.jet.im.kit.R
import com.jet.im.kit.SendbirdUIKit
import com.jet.im.kit.activities.viewholder.MessageType
import com.jet.im.kit.activities.viewholder.MessageViewHolderFactory
import com.jet.im.kit.databinding.SbViewChatNotificationBinding
import com.jet.im.kit.databinding.SbViewTimeLineMessageBinding
import com.jet.im.kit.interfaces.OnMessageListUpdateHandler
import com.jet.im.kit.interfaces.OnNotificationTemplateActionHandler
import com.jet.im.kit.internal.model.NotificationDiffCallback
import com.jet.im.kit.internal.model.notifications.NotificationConfig
import com.jet.im.kit.internal.ui.viewholders.ChatNotificationViewHolder
import com.jet.im.kit.internal.ui.viewholders.NotificationTimelineViewHolder
import com.jet.im.kit.internal.ui.viewholders.NotificationViewHolder
import com.jet.im.kit.model.TimelineMessage
import java.util.Collections
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors

internal class ChatNotificationListAdapter(
    private var channel: GroupChannel,
    private val notificationConfig: NotificationConfig?
) : RecyclerView.Adapter<NotificationViewHolder>() {
    private var messageList: List<BaseMessage> = listOf()

    // the worker must be a single thread.
    private val differWorker by lazy { Executors.newSingleThreadExecutor() }
    var onMessageTemplateActionHandler: OnNotificationTemplateActionHandler? = null

    /**
     * Called when RecyclerView needs a new [NotificationViewHolder] of the given type to represent
     * an item.
     *
     * @param parent   The ViewGroup into which the new View will be added after it is bound to
     * an adapter position.
     * @param viewType The view type of the new View.
     * @return A new [NotificationViewHolder] that holds a View of the given view type.
     * @see .getItemViewType
     * @see .onBindViewHolder
     * since 3.5.0
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotificationViewHolder {
        val values = TypedValue()
        parent.context.theme.resolveAttribute(R.attr.sb_component_list, values, true)
        val contextWrapper: Context = ContextThemeWrapper(parent.context, values.resourceId)
        val inflater = LayoutInflater.from(contextWrapper)
        if (MessageType.from(viewType) == MessageType.VIEW_TYPE_TIME_LINE) {
            return NotificationTimelineViewHolder(SbViewTimeLineMessageBinding.inflate(inflater, parent, false))
        }
        return ChatNotificationViewHolder(SbViewChatNotificationBinding.inflate(inflater, parent, false)).apply {
            binding.chatNotification.onNotificationTemplateActionHandler = onMessageTemplateActionHandler
        }
    }

    /**
     * Called by RecyclerView to display the data at the specified position. This method should
     * update the contents of the [NotificationViewHolder.itemView] to reflect the item at the given
     * position.
     *
     * @param holder   The [NotificationViewHolder] which should be updated to represent
     * the contents of the item at the given position in the data set.
     * @param position The position of the item within the adapter's data set.
     * since 3.5.0
     */
    override fun onBindViewHolder(holder: NotificationViewHolder, position: Int) {
        val message = getItem(position)
        holder.bind(channel, message, notificationConfig)
    }

    /**
     * Return the view type of the [NotificationViewHolder].
     * Notification channel always returns [MessageType.VIEW_TYPE_CHAT_NOTIFICATION]
     *
     * @param position position to query
     * @return integer value identifying the type of the view needed to represent the item at `position`.
     * @see MessageViewHolderFactory.getViewType
     * since 3.5.0
     */
    override fun getItemViewType(position: Int): Int {
        return if (getItem(position) is TimelineMessage) {
            MessageType.VIEW_TYPE_TIME_LINE.value
        } else {
            MessageType.VIEW_TYPE_CHAT_NOTIFICATION.value
        }
    }

    /**
     * Sets the {@link List<BaseMessage>} to be displayed.
     *
     * @param messageList list to be displayed
     * since 3.5.0
     */
    fun setItems(channel: GroupChannel, messageList: List<BaseMessage>, callback: OnMessageListUpdateHandler?) {
        val copiedChannel = GroupChannel.clone(channel)
        val copiedMessage = Collections.unmodifiableList(messageList)
        differWorker.submit<Boolean> {
            val lock = CountDownLatch(1)
            val diffCallback = NotificationDiffCallback(
                this@ChatNotificationListAdapter.messageList,
                messageList
            )
            val diffResult = DiffUtil.calculateDiff(diffCallback)
            SendbirdUIKit.runOnUIThread {
                try {
                    this@ChatNotificationListAdapter.messageList = copiedMessage
                    this@ChatNotificationListAdapter.channel = copiedChannel
                    diffResult.dispatchUpdatesTo(this@ChatNotificationListAdapter)
                    callback?.onListUpdated(messageList)
                } finally {
                    lock.countDown()
                }
            }
            lock.await()
            true
        }
    }

    /**
     * Returns the total number of items in the data set held by the adapter.
     *
     * @return The total number of items in this adapter.
     * since 3.5.0
     */
    override fun getItemCount(): Int {
        return messageList.size
    }

    /**
     * Returns the [BaseMessage] in the data set held by the adapter.
     *
     * @param position The position of the item within the adapter's data set.
     * @return The [BaseMessage] to retrieve the position of in this adapter.
     * since 3.5.0
     */
    fun getItem(position: Int): BaseMessage {
        return messageList[position]
    }

    /**
     * Returns the {@link List<BaseMessage>} in the data set held by the adapter.
     *
     * @return The {@link List<BaseMessage>} in this adapter.
     * since 3.5.0
     */
    fun getItems(): List<BaseMessage> {
        return Collections.unmodifiableList(messageList)
    }
}
