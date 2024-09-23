package com.jet.im.kit.activities.adapter;

import androidx.annotation.NonNull;

import com.jet.im.kit.activities.viewholder.BaseViewHolder;
import com.jet.im.kit.interfaces.OnItemClickListener;

import java.util.List;

/**
 * MutableBaseAdapter provides a binding from an app-specific data set to views that are displayed within a RecyclerView.
 *
 * @param <T> A class of data's type.
 * since 3.0.0
 */
abstract public class MutableBaseAdapter<T> extends BaseAdapter<T, BaseViewHolder<T>> {

    abstract public void setItems(@NonNull List<T> items);

    abstract public void setOnItemClickListener(@NonNull OnItemClickListener<T> listener);
}
