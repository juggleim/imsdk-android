package com.jet.im.kit.internal.ui.notifications

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.sendbird.android.channel.FeedChannel
import com.sendbird.android.message.BaseMessage
import com.jet.im.kit.interfaces.OnMessageListUpdateHandler
import com.jet.im.kit.interfaces.OnNotificationCategorySelectListener
import com.jet.im.kit.interfaces.OnNotificationTemplateActionHandler
import com.jet.im.kit.internal.extensions.intToDp
import com.jet.im.kit.internal.extensions.setTypeface
import com.jet.im.kit.internal.interfaces.OnNotificationViewedDetectedListener
import com.jet.im.kit.internal.model.notifications.NotificationConfig
import com.jet.im.kit.internal.ui.widgets.InnerLinearLayoutManager
import com.jet.im.kit.log.Logger
import com.jet.im.kit.model.Action
import com.jet.im.kit.utils.DrawableUtils
import java.lang.Exception
import java.util.concurrent.atomic.AtomicBoolean

/**
 * This class creates and performs a view corresponding the notification message list area in Sendbird UIKit.
 *
 * since 3.5.0
 */
@JvmSuppressWildcards
internal open class FeedNotificationListComponent @JvmOverloads constructor(
    params: Params = Params(),
    uiConfig: NotificationConfig? = null
) : NotificationListComponent(params, uiConfig) {

    var onNotificationViewedDetectedListener: OnNotificationViewedDetectedListener<BaseMessage>? = null

    /**
     * Returns the feed notification list adapter.
     *
     * @return The adapter applied to this list component
     * since 3.5.0
     */
    private var adapter: FeedNotificationListAdapter? = null
        private set(value) {
            field = value
            notificationListView?.recyclerView?.let {
                if (value?.onMessageTemplateActionHandler == null) {
                    value?.onMessageTemplateActionHandler =
                        OnNotificationTemplateActionHandler { view: View, action: Action, message: BaseMessage ->
                            onMessageTemplateActionClicked(
                                view,
                                action,
                                message
                            )
                        }
                }
                it.adapter = value
            }
        }

    /**
     * Sets or Gets the listener to be called when the notification category is selected.
     *
     * @since 3.8.0
     */
    var onNotificationCategorySelectListener: OnNotificationCategorySelectListener? = null

    private val isSetCategoryFilter = AtomicBoolean()

    override fun onCreateView(
        context: Context,
        inflater: LayoutInflater,
        parent: ViewGroup,
        args: Bundle?
    ): View {
        val layout = super.onCreateView(context, inflater, parent, args)
        notificationListView?.let {
            it.recyclerView.layoutManager = InnerLinearLayoutManager(context).apply { reverseLayout = false }
            it.onNotificationViewedDetectedListener = { firstVisibleItemPosition, lastVisibleItemPosition ->
                onVisibleItemDetected(firstVisibleItemPosition, lastVisibleItemPosition)
            }
        }
        return layout
    }

    private fun onVisibleItemDetected(firstVisibleItem: Int, lastVisibleItem: Int) {
        Logger.d(">> onVisibleItemDetected(), firstVisibleItem: $firstVisibleItem, lastVisibleItem: $lastVisibleItem")
        if (adapter == null) return
        if (firstVisibleItem < 0 || lastVisibleItem < 0) return

        onNotificationViewedDetectedListener?.let {
            val range = if (firstVisibleItem <= lastVisibleItem) firstVisibleItem..lastVisibleItem else lastVisibleItem..firstVisibleItem
            val copiedList = adapter?.getItems()?.toList()
            if (copiedList != null) {
                try {
                    val items = range.mapNotNull { i -> copiedList[i] }
                    it.onNotificationViewedDetected(items)
                } catch (ignore: Exception) {
                }
            }
        }
    }

    /**
     * Sets the last seen timestamp to update new badge UI.
     * This value is used to compare whether a message has been newly received.
     *
     * @param lastSeenAt the timestamp last viewed by the user.
     * since 3.5.0
     */
    @SuppressLint("NotifyDataSetChanged")
    fun notifyLastSeenUpdated(lastSeenAt: Long) {
        adapter?.let {
            it.updateLastSeenAt(lastSeenAt)
            it.notifyDataSetChanged()
        }
    }

    /**
     * Handles a new channel when data has changed.
     *
     * @param channel The latest group channel
     * since 3.5.0
     */
    fun notifyChannelChanged(channel: FeedChannel) {
        if (adapter == null) {
            adapter = FeedNotificationListAdapter(channel, uiConfig)
        }

        // if the category filter view is already set it shouldn't be set it again (spec)
        if (isSetCategoryFilter.get()) return
        notificationListView?.let { recyclerView ->
            recyclerView.enableCategoryFilterView(channel.isCategoryFilterEnabled && channel.notificationCategories.isNotEmpty())
            Logger.i("++ channel.categories size: ${channel.notificationCategories.size}")
            channel.notificationCategories.forEach { category ->
                Logger.i("++ category: $category")
                recyclerView.categoryFilterBox.addView(
                    recyclerView.createCategoryFilterItemView().apply {
                        text = category.name
                        id = category.id.hashCode()
                        isChecked = category.isDefault

                        if (uiConfig == null) return@apply
                        val themeMode = uiConfig.themeMode
                        recyclerView.setCategoryFilterBoxBackgroundColor(
                            uiConfig.theme.listTheme.backgroundColor.getColor(themeMode)
                        )

                        uiConfig.theme.listTheme.category?.let {
                            setTextColor(
                                DrawableUtils.createTextColorSelector(
                                    it.selectedTextColor.getColor(themeMode),
                                    it.textColor.getColor(themeMode)
                                )
                            )
                            setTextSize(TypedValue.COMPLEX_UNIT_SP, it.textSize.toFloat())
                            setTypeface(it.fontWeight.value)
                            background = DrawableUtils.createRoundedSelector(
                                it.selectedBackgroundColor.getColor(themeMode),
                                it.backgroundColor.getColor(themeMode),
                                resources.intToDp(it.radius),
                            )
                        }
                    }
                )
            }

            // Setting checked changes listener must be here after setup the category filter view.
            // if not, the callback of listener is always called whenever the view is created.
            recyclerView.categoryFilterBox.setOnCheckedChangeListener { _, checkedId ->
                channel.notificationCategories.find { it.id.hashCode() == checkedId }
                    ?.let { category ->
                        onNotificationCategorySelectListener?.onNotificationCategorySelected(category)
                    }
            }
            isSetCategoryFilter.set(true)
        }
    }

    /**
     * Handles the data needed to draw the message list has changed.
     *
     * @param notificationList The list of messages to be drawn
     * @param channel     The latest group channel
     * @param callback    Callback when the message list is updated
     * since 3.5.0
     */
    fun notifyDataSetChanged(
        notificationList: List<BaseMessage>,
        channel: FeedChannel,
        callback: OnMessageListUpdateHandler?
    ) {
        notificationListView?.let {
            adapter?.setItems(channel, notificationList, callback)
        }
    }

    /**
     * Clear all notification data.
     *
     * @since 3.8.0
     */
    fun clearData() {
        adapter?.clear()
    }

    /**
     * A collection of parameters, which can be applied to a default View. The values of params are not dynamically applied at runtime.
     * Params cannot be created directly, and it is automatically created together when components are created.
     *
     * **Since the onCreateView configuring View uses the values of the set Params, we recommend that you set up for Params before the onCreateView is called.**
     *
     * @see .getParams
     * since 3.5.0
     */
    open class Params : NotificationListComponent.Params() {
        /**
         * Apply data that matches keys mapped to Params' properties.
         *
         * @param context The `Context` this component is currently associated with
         * @param args    The sets of arguments to apply at Params.
         * @return This Params object that applied with given data.
         * since 3.5.0
         */
        override fun apply(context: Context, args: Bundle): Params {
            return this
        }
    }
}
