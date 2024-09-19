package com.juggle.chat.common.adapter;


import android.content.Context;
import android.view.LayoutInflater;

import java.util.List;

/**
 * Created by zhy on 16/4/9.
 */
public abstract class CommonAdapter<T> extends MultiItemTypeAdapter<T> {
    protected int mLayoutId;

    public CommonAdapter(final int layoutId) {
        super();
        mLayoutId = layoutId;
        addItemViewDelegate(
                new ItemViewDelegate<T>() {
                    @Override
                    public int getItemViewLayoutId() {
                        return layoutId;
                    }

                    @Override
                    public boolean isForViewType(T item, int position) {
                        return true;
                    }

                    @Override
                    public void convert(ViewHolder holder, T t, int position) {
                        CommonAdapter.this.bindData(holder, t, position);
                    }
                });
    }

    public abstract void bindData(ViewHolder holder, T t, int position);
}
