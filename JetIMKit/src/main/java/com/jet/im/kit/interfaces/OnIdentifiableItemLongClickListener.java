package com.jet.im.kit.interfaces;

import android.view.View;

import androidx.annotation.NonNull;

import com.jet.im.kit.consts.ClickableViewIdentifier;

/**
 * Interface definition for a callback to be invoked when a view is long clicked.
 */
public interface OnIdentifiableItemLongClickListener<T> {
    /**
     * Called when a view has been long clicked.
     *
     * @param view The view that was long clicked.
     * @param identifier The clicked item identifier.
     * @param position The position that was long clicked.
     * @param data The data that was long clicked.
     *
     * @see ClickableViewIdentifier
     * since 2.2.0
     */
    void onIdentifiableItemLongClick(@NonNull View view, @NonNull String identifier, int position, T data);
}
