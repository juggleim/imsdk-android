package com.jet.im.kit.internal.utils

import android.graphics.Rect
import android.view.ViewTreeObserver.OnGlobalLayoutListener
import androidx.recyclerview.widget.RecyclerView
import com.jet.im.kit.internal.extensions.runOnUiThread
import com.jet.im.kit.log.Logger
import com.jet.im.kit.utils.ClearableScheduledExecutorService
import java.util.concurrent.TimeUnit

/**
 * Notification Viewed Tracker fires events in two cases
 * 1. when an item is initially added to the recycler view.(Track only once for the first time.)
 * 2. when scrolling stops.
 *
 * The first time an item is added, the event is fired immediately with no delay, and if the scroll stops, the event is fired after the time value of [debounce].
 */
internal class NotificationViewedTracker(
    private val recyclerView: RecyclerView,
    private val debounce: Long = 500L
) : RecyclerView.OnScrollListener(), OnGlobalLayoutListener {
    private val scheduler by lazy { ClearableScheduledExecutorService() }
    private var initialDataLoaded = false
    private var isRunning = false
    var onNotificationViewedDetected: (() -> Unit)? = null

    override fun onScrollStateChanged(view: RecyclerView, scrollState: Int) {
        if (view.childCount <= 0) return
        if (scrollState == RecyclerView.SCROLL_STATE_IDLE) {
            startSchedule(debounce)
        } else {
            cancelSchedule()
        }
    }

    override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
        if (initialDataLoaded) return
        if (recyclerView.childCount <= 0) return

        // Make sure the current view is drawn on the screen
        if (!recyclerView.getGlobalVisibleRect(Rect())) return
        if (dx == 0 && dy == 0) {
            // This is the case when the item is initially loaded.
            startSchedule()
            initialDataLoaded = true
        }
    }

    override fun onGlobalLayout() {
        if (initialDataLoaded) return
        if (recyclerView.childCount <= 0) return

        // Once the layout has been drawn, it will continue to return true. But that's not a problem because we only need to catch it the first time.
        if (!recyclerView.getGlobalVisibleRect(Rect())) return
        startSchedule()
        initialDataLoaded = true
        recyclerView.viewTreeObserver.removeOnGlobalLayoutListener(this)
    }

    @Synchronized
    fun start() {
        Logger.d(">> NotificationViewedTracker::start()")
        if (isRunning) return
        recyclerView.addOnScrollListener(this)

        /**
         * If it is organized in tabs, the View's Visibility is View.Visible, but it may not actually be visible on the screen.
         * In this case, we need to watch for events that change the layout,
         * because we want the notifications viewed to act the moment it is actually visible.
         */
        recyclerView.viewTreeObserver.addOnGlobalLayoutListener(this)
        isRunning = true
    }

    @Synchronized
    fun stop() {
        Logger.d(">> NotificationViewedTracker stop()")
        isRunning = false
        recyclerView.removeOnScrollListener(this)
        recyclerView.viewTreeObserver.removeOnGlobalLayoutListener(this)
        cancelSchedule()
    }

    @Synchronized
    private fun startSchedule(initialDelay: Long = 300L) {
        Logger.d(">> NotificationViewedTracker::startSchedule(), initialDelay: $initialDelay")

        if (initialDelay > 0L) {
            scheduler.schedule({
                notifyNotificationViewed()
            }, debounce, TimeUnit.MILLISECONDS)
            return
        } else {
            notifyNotificationViewed()
        }
    }

    @Synchronized
    private fun cancelSchedule() {
        Logger.d(">> NotificationViewedTracker cancelSchedule()")
        scheduler.cancelAllJobs(true)
    }

    private fun notifyNotificationViewed() {
        runOnUiThread {
            Logger.i(">> notifications viewed detected")
            onNotificationViewedDetected?.invoke()
        }
    }
}
