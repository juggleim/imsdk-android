package com.jet.im.kit.interfaces

import com.sendbird.android.channel.NotificationCategory

/**
 * Interface definition for a callback to be invoked when a category filter is selected.
 *
 * @since 3.8.0
 */
fun interface OnNotificationCategorySelectListener {
    /**
     * Called when a category filter is selected.
     *
     * @param category The selected category
     * @since 3.8.0
     * @see NotificationCategory
     */
    fun onNotificationCategorySelected(category: NotificationCategory)
}
