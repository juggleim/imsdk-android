package com.jet.im.kit.interfaces;

import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.jet.im.kit.consts.ClickableViewIdentifier;

/**
 * Interface definition for a callback to be invoked when a item is clicked.
 */
public interface OnIdentifiableItemClickListener<T> {
    /**
     * Called when a view has been clicked.
     *
     * @param view The view that was clicked.
     * @param identifier The clicked item identifier.
     * @param position The position that was clicked.
     * @param data The data that was clicked.
     *
     * @see ClickableViewIdentifier
     * since 2.2.0
     */
    void onIdentifiableItemClick(@NonNull View view, @NonNull String identifier, int position, @Nullable T data);
}
