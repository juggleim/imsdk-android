package com.juggle.chat.common.widgets.refresh.listener;

import com.juggle.chat.common.widgets.refresh.api.RefreshFooter;
import com.juggle.chat.common.widgets.refresh.api.RefreshHeader;

/** FeatureListener Created by scwang on 2017/5/26. */
public interface OnMultiListener extends OnRefreshLoadMoreListener, OnStateChangedListener {
    /**
     * fingerdragpull-down（，isDraggingonPulling、onReleasing）
     *
     * @param header
     * @param isDragging true fingerdrag false reboundanimation
     * @param percent pull-down  = offset/footerHeight (0 - percent - (footerHeight+maxDragHeight) /
     *     footerHeight )
     * @param offset pull-down 0 - offset - (footerHeight+maxDragHeight)
     * @param headerHeight height HeaderHeight or FooterHeight
     * @param maxDragHeight dragheight
     */
    void onHeaderMoving(
            RefreshHeader header,
            boolean isDragging,
            float percent,
            int offset,
            int headerHeight,
            int maxDragHeight);

    void onHeaderReleased(RefreshHeader header, int headerHeight, int maxDragHeight);

    void onHeaderStartAnimator(RefreshHeader header, int headerHeight, int maxDragHeight);

    void onHeaderFinish(RefreshHeader header, boolean success);

    /**
     * fingerdragpull-up（，isDraggingonPulling、onReleasing）
     *
     * @param footer
     * @param isDragging true fingerdrag false reboundanimation
     * @param percent pull-down  = offset/footerHeight (0 - percent - (footerHeight+maxDragHeight) /
     *     footerHeight )
     * @param offset pull-down 0 - offset - (footerHeight+maxDragHeight)
     * @param footerHeight height HeaderHeight or FooterHeight
     * @param maxDragHeight dragheight
     */
    void onFooterMoving(
            RefreshFooter footer,
            boolean isDragging,
            float percent,
            int offset,
            int footerHeight,
            int maxDragHeight);

    void onFooterReleased(RefreshFooter footer, int footerHeight, int maxDragHeight);

    void onFooterStartAnimator(RefreshFooter footer, int footerHeight, int maxDragHeight);

    void onFooterFinish(RefreshFooter footer, boolean success);
}
