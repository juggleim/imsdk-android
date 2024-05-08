package com.jet.im.kit.internal.ui.notifications

import android.annotation.SuppressLint
import android.content.Context
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.view.ContextThemeWrapper
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.sendbird.android.channel.FeedChannel
import com.sendbird.android.message.BaseMessage
import com.jet.im.kit.R
import com.jet.im.kit.SendbirdUIKit
import com.jet.im.kit.activities.viewholder.MessageType
import com.jet.im.kit.databinding.SbViewFeedNotificationBinding
import com.jet.im.kit.interfaces.OnMessageListUpdateHandler
import com.jet.im.kit.interfaces.OnNotificationTemplateActionHandler
import com.jet.im.kit.internal.model.NotificationDiffCallback
import com.jet.im.kit.internal.model.notifications.NotificationConfig
import com.jet.im.kit.internal.ui.viewholders.FeedNotificationViewHolder
import java.util.Collections
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors

internal class FeedNotificationListAdapter(
    private var channel: FeedChannel,
    private val notificationConfig: NotificationConfig?
) : RecyclerView.Adapter<FeedNotificationViewHolder>() {
    private var messageList: List<BaseMessage> = listOf()
    private var prevLastSeenAt = 0L
    private var currentLastSeenAt: Long = 0

    // the worker must be a single thread.
    private val dataWorker by lazy { Executors.newSingleThreadExecutor() }
    var onMessageTemplateActionHandler: OnNotificationTemplateActionHandler? = null

    init {
        this.prevLastSeenAt = channel.myLastRead
        this.currentLastSeenAt = channel.myLastRead
    }

    /**
     * Called when RecyclerView needs a new [FeedNotificationViewHolder] of the given type to represent
     * an item.
     *
     * @param parent   The ViewGroup into which the new View will be added after it is bound to
     * an adapter position.
     * @param viewType The view type of the new View.
     * @return A new [FeedNotificationViewHolder] that holds a View of the given view type.
     * @see .getItemViewType
     * @see .onBindViewHolder
     * since 3.5.0
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FeedNotificationViewHolder {
        val values = TypedValue()
        parent.context.theme.resolveAttribute(R.attr.sb_component_list, values, true)
        val contextWrapper: Context = ContextThemeWrapper(parent.context, values.resourceId)
        val inflater = LayoutInflater.from(contextWrapper)
        return FeedNotificationViewHolder(SbViewFeedNotificationBinding.inflate(inflater, parent, false)).apply {
            binding.feedNotification.onNotificationTemplateActionHandler = onMessageTemplateActionHandler
        }
    }

    /**
     * Called by RecyclerView to display the data at the specified position. This method should
     * update the contents of the [FeedNotificationViewHolder.itemView] to reflect the item at the given
     * position.
     *
     * @param holder   The [FeedNotificationViewHolder] which should be updated to represent
     * the contents of the item at the given position in the data set.
     * @param position The position of the item within the adapter's data set.
     * since 3.5.0
     */
    override fun onBindViewHolder(holder: FeedNotificationViewHolder, position: Int) {
        holder.bind(channel, getItem(position), currentLastSeenAt, notificationConfig)
    }

    /**
     * Return the view type of the [FeedNotificationViewHolder].
     * Notification channel always returns [MessageType.VIEW_TYPE_FEED_NOTIFICATION]
     *
     * @param position position to query
     * @return integer value identifying the type of the view needed to represent the item at `position`.
     * @see MessageViewHolderFactory.itemViewType
     * since 3.5.0
     */
    override fun getItemViewType(position: Int): Int {
        return MessageType.VIEW_TYPE_FEED_NOTIFICATION.value
    }

    /**
     * Sets the {@link List<BaseMessage>} to be displayed.
     *
     * @param messageList list to be displayed
     * since 3.5.0
     */
    fun setItems(channel: FeedChannel, messageList: List<BaseMessage>, callback: OnMessageListUpdateHandler?) {
        val copiedChannel = FeedChannel.clone(channel)
        val copiedMessage = Collections.unmodifiableList(messageList)
        dataWorker.submit {
            val lock = CountDownLatch(1)
            val diffCallback = NotificationDiffCallback(
                this@FeedNotificationListAdapter.messageList,
                messageList,
                prevLastSeenAt,
                currentLastSeenAt
            )
            val diffResult = DiffUtil.calculateDiff(diffCallback)
            SendbirdUIKit.runOnUIThread {
                try {
                    this@FeedNotificationListAdapter.messageList = copiedMessage
                    this@FeedNotificationListAdapter.channel = copiedChannel
                    diffResult.dispatchUpdatesTo(this@FeedNotificationListAdapter)
                    callback?.onListUpdated(messageList)
                } finally {
                    lock.countDown()
                }
            }
            lock.await()
        }
    }

    /**
     * Clear all data in the adapter.
     *
     * @since 3.8.0
     */
    @SuppressLint("NotifyDataSetChanged")
    fun clear() {
        // use dataWorker to prevent the data synchronization problem.
        dataWorker.submit {
            SendbirdUIKit.runOnUIThread {
                this.messageList = Collections.emptyList()
                notifyDataSetChanged()
            }
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

    /**
     * Set the current user's last read timestamp in channel.
     *
     * @param lastSeenAt the current user's last read timestamp in channel.
     * since 3.5.0
     */
    @Synchronized
    fun updateLastSeenAt(lastSeenAt: Long) {
        // set the previous lastSeenAt value due to compare the message changing status.
        prevLastSeenAt = currentLastSeenAt
        currentLastSeenAt = lastSeenAt
    }
}
