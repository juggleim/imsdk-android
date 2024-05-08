package com.jet.im.kit.internal.ui.notifications

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import com.sendbird.android.message.BaseMessage
import com.jet.im.kit.R
import com.jet.im.kit.fragments.ItemAnimator
import com.jet.im.kit.interfaces.OnNotificationTemplateActionHandler
import com.jet.im.kit.interfaces.OnPagedDataLoader
import com.jet.im.kit.internal.model.notifications.NotificationConfig
import com.jet.im.kit.internal.ui.widgets.NotificationRecyclerView
import com.jet.im.kit.internal.ui.widgets.PagerRecyclerView.OnScrollEndDetectListener
import com.jet.im.kit.internal.ui.widgets.PagerRecyclerView.ScrollDirection
import com.jet.im.kit.model.Action
import java.util.Locale
import java.util.concurrent.atomic.AtomicInteger

/**
 * This class creates and performs a view corresponding the notification message list area in Sendbird UIKit.
 *
 * since 3.5.0
 */
@JvmSuppressWildcards
internal open class NotificationListComponent @JvmOverloads constructor(
    private val params: Params = Params(),
    protected val uiConfig: NotificationConfig? = null
) {
    private val tooltipCount = AtomicInteger()
    protected var notificationListView: NotificationRecyclerView? = null

    var onTooltipClickListener: OnClickListener? = null
        set(value) {
            field = value
            notificationListView?.setOnTooltipClickListener(value)
        }

    var onMessageTemplateActionHandler: OnNotificationTemplateActionHandler? = null

    var pagedDataLoader: OnPagedDataLoader<List<BaseMessage>>? = null
        set(value) {
            field = value
            pagedDataLoader?.let { notificationListView?.recyclerView?.setPager(it) }
        }

    /**
     * Returns the view created by [.onCreateView].
     *
     * @return the topmost view containing this view
     * since 3.5.0
     */
    val rootView: View?
        get() = notificationListView

    /**
     * Called after the component was created to make views.
     *
     * **If this function is used override, [.getRootView] must also be override.**
     *
     * @param context  The `Context` this component is currently associated with
     * @param inflater The LayoutInflater object that can be used to inflate any views in the component
     * @param parent   The ViewGroup into which the new View will be added
     * @param args     The arguments supplied when the component was instantiated, if any
     * @return Return the View for the UI.
     * since 3.5.0
     */
    open fun onCreateView(context: Context, inflater: LayoutInflater, parent: ViewGroup, args: Bundle?): View {
        if (args != null) params.apply(context, args)

        val layout = NotificationRecyclerView(context, null, R.attr.sb_component_list).apply {
            recyclerView.setHasFixedSize(true)
            recyclerView.clipToPadding = false
            recyclerView.setThreshold(5)
            recyclerView.setUseDivider(false)
            recyclerView.itemAnimator = ItemAnimator()
            recyclerView.useReverseData()
            recyclerView.setOnScrollEndDetectListener(object : OnScrollEndDetectListener {
                override fun onScrollEnd(direction: ScrollDirection) {
                    onScrollEndReaches(
                        direction, this@apply
                    )
                }
            })
        }

        uiConfig?.let {
            val themeMode = it.themeMode
            it.theme.listTheme.apply {
                layout.setBackgroundColor(backgroundColor.getColor(themeMode))
                layout.setTooltipBackgroundColor(tooltip.backgroundColor.getColor(themeMode))
                layout.setTooltipTextColor(tooltip.textColor.getColor(themeMode))
                layout.setTooltipTextSize(tooltip.textSize)
                layout.setTooltipTextStyle(tooltip.fontWeight.value)
            }
        }
        notificationListView = layout
        return layout
    }

    /**
     * Called when the view that has an [com.jet.im.kit.model.Action] data is clicked.
     *
     * @param view the view that was clicked.
     * @param action the registered Action data
     * @param message the clicked message
     * since 3.5.0
     */
    protected fun onMessageTemplateActionClicked(view: View, action: Action, message: BaseMessage) {
        onMessageTemplateActionHandler?.onHandleAction(view, action, message)
    }

    /**
     * Scrolls to the first position of the recycler view.
     */
    fun scrollToFirst() {
        notificationListView?.recyclerView?.stopScroll()
        notificationListView?.recyclerView?.scrollToPosition(0)
    }

    private fun onScrollEndReaches(direction: ScrollDirection, notificationListView: NotificationRecyclerView) {
        val reverseLayout = notificationListView.isReverseLayout
        if (reverseLayout && direction === ScrollDirection.Bottom || !reverseLayout && direction === ScrollDirection.Top) {
            tooltipCount.set(0)
            notificationListView.hideTooltip()
        }
    }

    fun notifyMessagesFilled() {
        notificationListView?.let {
            val firstVisibleItemPosition: Int = it.recyclerView.findFirstVisibleItemPosition()
            if (firstVisibleItemPosition == 0) {
                scrollToFirst()
            }
        }
    }

    fun notifyNewNotificationReceived() {
        notificationListView?.let {
            val firstVisibleItemPosition: Int = it.recyclerView.findFirstVisibleItemPosition()
            if (firstVisibleItemPosition > 0) {
                it.showTooltip(
                    getTooltipText(
                        it.context, tooltipCount.incrementAndGet()
                    )
                )
                return
            }
            if (firstVisibleItemPosition == 0) {
                scrollToFirst()
            }
        }
    }

    /**
     * Returns the text on the tooltip.
     *
     * @param context The `Context` this view is currently associated with
     * @param count   Number of new messages
     * @return Text to be shown on the tooltip
     */
    open fun getTooltipText(context: Context, count: Int): String {
        return notificationListView?.let {
            "${
                String.format(
                    Locale.getDefault(), context.getString(R.string.sb_text_channel_tooltip), count
                )
            }${if (count > 1) "s" else ""}"
        } ?: ""
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
    open class Params {
        /**
         * Apply data that matches keys mapped to Params' properties.
         *
         * @param context The `Context` this component is currently associated with
         * @param args    The sets of arguments to apply at Params.
         * @return This Params object that applied with given data.
         * since 3.5.0
         */
        open fun apply(context: Context, args: Bundle): Params {
            return this
        }
    }
}
