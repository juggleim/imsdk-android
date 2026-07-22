package com.juggle.chat.common.widgets.refresh.simple;

import android.graphics.PointF;
import android.view.View;

import com.juggle.chat.common.widgets.refresh.listener.ScrollBoundaryDecider;
import com.juggle.chat.common.widgets.refresh.util.SmartUtil;

/** Scroll Created by scwang on 2017/7/8. */
public class SimpleBoundaryDecider implements ScrollBoundaryDecider {

    // <editor-fold desc="Internal">
    public PointF mActionEvent;
    public ScrollBoundaryDecider boundary;
    public boolean mEnableLoadMoreWhenContentNotFull = true;
    // </editor-fold>

    // <editor-fold desc="ScrollBoundaryDecider">
    @Override
    public boolean canRefresh(View content) {
        if (boundary != null) {
            return boundary.canRefresh(content);
        }
        // mActionEvent == null  canRefresh Search
        return SmartUtil.canRefresh(content, mActionEvent);
    }

    @Override
    public boolean canLoadMore(View content) {
        if (boundary != null) {
            return boundary.canLoadMore(content);
        }
        // mActionEvent == null  canLoadMore Search
        return SmartUtil.canLoadMore(content, mActionEvent, mEnableLoadMoreWhenContentNotFull);
    }
    // </editor-fold>
}
