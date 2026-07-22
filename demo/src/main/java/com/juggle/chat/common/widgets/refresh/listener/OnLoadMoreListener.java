package com.juggle.chat.common.widgets.refresh.listener;

import androidx.annotation.NonNull;

import com.juggle.chat.common.widgets.refresh.api.RefreshLayout;


/** LoadListener Created by scwang on 2017/5/26. */
public interface OnLoadMoreListener {
    void onLoadMore(@NonNull RefreshLayout refreshLayout);
}
