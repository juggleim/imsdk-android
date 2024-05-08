package com.jet.im.kit.interfaces;

import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * Interface definition for a callback to be invoked when a item is clicked.
 *
 * since 1.2.0
 */
public interface OnMenuItemClickListener<T, R> {
    /**
     * Called when a view has been clicked.
     *
     * @param view The view that was clicked.
     * @param menu The menu item that was clicked.
     * @param data The data that was clicked.
     * @return true if the callback consumed the menu item click, false otherwise.
     */
    @SuppressWarnings("UnusedReturnValue")
    boolean onMenuItemClicked(@NonNull View view, @NonNull T menu, @Nullable R data);
}
