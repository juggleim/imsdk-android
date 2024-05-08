package com.jet.im.kit.interfaces;

import android.view.View;

import androidx.annotation.NonNull;

/**
 * Interface definition for a callback to be invoked when a view is long clicked.
 */
public interface OnItemLongClickListener<T> {
    /**
     * Called when a view has been long clicked.
     *
     * @param view The view that was long clicked.
     * @param position The position that was long clicked.
     * @param data The data that was long clicked.
     */
    void onItemLongClick(@NonNull View view, int position, @NonNull T data);
}
