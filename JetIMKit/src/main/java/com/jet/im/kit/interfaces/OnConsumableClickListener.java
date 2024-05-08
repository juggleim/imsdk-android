package com.jet.im.kit.interfaces;

import android.view.View;

import androidx.annotation.NonNull;

/**
 * Interface definition for a callback to be invoked when a view is clicked.
 *
 * since 3.2.2
 */
public interface OnConsumableClickListener {
    /**
     * Called when a view has been clicked. This returns a boolean to indicate whether you have consumed the event and
     * it should not be carried further. That is, return <code>true</code> to indicate that you have handled the event and
     * it should stop here; return <code>false</code> if you have not handled it and/or the event should continue to any other on-click listeners.
     *
     * @param view The view that was clicked.
     * @return <code>true</code> if the callback consumed the long click, <code>false</code> otherwise.
     * since 3.2.2
     */
    boolean onClick(@NonNull View view);
}
