package com.juggle.chat.common.widgets.refresh.listener;

import static androidx.annotation.RestrictTo.Scope.LIBRARY;
import static androidx.annotation.RestrictTo.Scope.LIBRARY_GROUP;
import static androidx.annotation.RestrictTo.Scope.SUBCLASSES;

import androidx.annotation.NonNull;
import androidx.annotation.RestrictTo;

import com.juggle.chat.common.widgets.refresh.api.RefreshLayout;
import com.juggle.chat.common.widgets.refresh.constant.RefreshState;


/** RefreshstateListener Created by scwang on 2017/5/26. */
public interface OnStateChangedListener {
    /**
     * 【】state {@link RefreshState}
     *
     * @param refreshLayout RefreshLayout
     * @param oldState state
     * @param newState state
     */
    @RestrictTo({LIBRARY, LIBRARY_GROUP, SUBCLASSES})
    void onStateChanged(
            @NonNull RefreshLayout refreshLayout,
            @NonNull RefreshState oldState,
            @NonNull RefreshState newState);
}
