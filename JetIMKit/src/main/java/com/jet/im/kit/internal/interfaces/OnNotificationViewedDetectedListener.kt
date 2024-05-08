package com.jet.im.kit.internal.interfaces

/**
 * On visible item detect listener
 */
@JvmSuppressWildcards
internal fun interface OnNotificationViewedDetectedListener<T> {
    fun onNotificationViewedDetected(items: List<T>)
}
