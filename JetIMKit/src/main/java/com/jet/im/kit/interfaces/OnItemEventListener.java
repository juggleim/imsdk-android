package com.jet.im.kit.interfaces;

import androidx.annotation.NonNull;

/**
 * Interface definition for a callback to be invoked when a item is invoked with an event.
 */
public interface OnItemEventListener<T> {
    /**
     * Called when a event has been invoked.
     *
     * @param data The data that was invoked.
     */
    void onItemEvent(@NonNull T data);
}
