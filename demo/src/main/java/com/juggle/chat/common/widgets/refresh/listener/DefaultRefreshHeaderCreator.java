package com.juggle.chat.common.widgets.refresh.listener;

import android.content.Context;

import androidx.annotation.NonNull;

import com.juggle.chat.common.widgets.refresh.api.RefreshHeader;
import com.juggle.chat.common.widgets.refresh.api.RefreshLayout;


/** DefaultHeaderCreate Created by scwang on 2018/1/26. */
public interface DefaultRefreshHeaderCreator {
    @NonNull
    RefreshHeader createRefreshHeader(@NonNull Context context, @NonNull RefreshLayout layout);
}
