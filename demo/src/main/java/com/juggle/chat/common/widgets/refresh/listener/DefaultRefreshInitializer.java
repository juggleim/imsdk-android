package com.juggle.chat.common.widgets.refresh.listener;

import android.content.Context;

import androidx.annotation.NonNull;

import com.juggle.chat.common.widgets.refresh.api.RefreshLayout;


/** Default Created by scwang on 2018/5/29 0029. */
public interface DefaultRefreshInitializer {
    void initialize(@NonNull Context context, @NonNull RefreshLayout layout);
}
