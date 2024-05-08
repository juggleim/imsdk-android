package com.jet.im.kit.activities.viewholder;

import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

/**
 * A ViewHolder describes an item view and data about its place within the RecyclerView.
 */
public abstract class BaseViewHolder<T> extends RecyclerView.ViewHolder {
    /**
     * Constructor
     *
     * @param itemView View to be displayed.
     */
    public BaseViewHolder(@NonNull View itemView) {
        super(itemView);
    }

    /**
     * Binds as item view and data.
     *
     * @param item Data used for as item view.
     */
    public abstract void bind(@NonNull T item);
}
