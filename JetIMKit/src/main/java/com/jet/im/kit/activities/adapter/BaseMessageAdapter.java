package com.jet.im.kit.activities.adapter;

import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

/**
 * BaseMessageAdapter provides a binding from an app-specific data set to views that are displayed within a RecyclerView.
 *
 * @param <T> A class of data's type.
 * @param <VH> A class that extends RecyclerView.ViewHolder that will be used by the adapter.
 */
abstract class BaseMessageAdapter<T, VH extends RecyclerView.ViewHolder> extends RecyclerView.Adapter<VH> {
    /**
     * Returns item that located given position.
     *
     * @param position Adapter position to query
     * @return the item at position
     */
    abstract public T getItem(int position);

    /**
     * Returns items which bound a RecyclerView.
     *
     * @return A whole items that bound a RecyclerView.
     */
    abstract public List<T> getItems();
}
