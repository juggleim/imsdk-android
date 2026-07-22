package com.juggle.chat.common.widgets.refresh.listener;

import android.view.View;

/** Scroll Created by scwang on 2017/7/8. */
public interface ScrollBoundaryDecider {
    /**
     * ContentViewstatestartpull-downRefresh
     *
     * @param content ContentView
     * @return true triggerpull-downRefresh
     */
    boolean canRefresh(View content);
    /**
     * ContentViewstatestartpull-upLoad
     *
     * @param content ContentView
     * @return true triggerLoad
     */
    boolean canLoadMore(View content);
}
