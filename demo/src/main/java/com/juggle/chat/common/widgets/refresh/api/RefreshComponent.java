package com.juggle.chat.common.widgets.refresh.api;

import static androidx.annotation.RestrictTo.Scope.LIBRARY;
import static androidx.annotation.RestrictTo.Scope.LIBRARY_GROUP;
import static androidx.annotation.RestrictTo.Scope.SUBCLASSES;

import android.view.View;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.RestrictTo;

import com.juggle.chat.common.widgets.refresh.constant.SpinnerStyle;
import com.juggle.chat.common.widgets.refresh.listener.OnStateChangedListener;


/** Refreshinternalcomponent Created by scwang on 2017/5/26. */
public interface RefreshComponent extends OnStateChangedListener {
    /**
     * GetView
     *
     * @return View
     */
    @NonNull
    View getView();

    /**
     * Get {@link SpinnerStyle} MustReturn
     *
     * @return
     */
    @NonNull
    SpinnerStyle getSpinnerStyle();

    /**
     * 【】Setcolor
     *
     * @param colors Xml srlPrimaryColor srlAccentColor
     */
    @RestrictTo({LIBRARY, LIBRARY_GROUP, SUBCLASSES})
    void setPrimaryColors(@ColorInt int... colors);

    /**
     * 【】complete （height（：setHeader），, RefreshLayout#onMeasure）
     *
     * @param kernel RefreshKernel
     * @param height HeaderHeight or FooterHeight
     * @param maxDragHeight dragheight
     */
    @RestrictTo({LIBRARY, LIBRARY_GROUP, SUBCLASSES})
    void onInitialized(@NonNull RefreshKernel kernel, int height, int maxDragHeight);
    /**
     * 【】fingerdragpull-down（，isDraggingonPulling、onReleasing）
     *
     * @param isDragging true fingerdrag false reboundanimation
     * @param percent pull-down  = offset/footerHeight (0 - percent - (footerHeight+maxDragHeight) /
     *     footerHeight )
     * @param offset pull-down 0 - offset - (footerHeight+maxDragHeight)
     * @param height height HeaderHeight or FooterHeight (offset  height  percent  1)
     * @param maxDragHeight dragheight offset  height parameter  maxDragHeight
     */
    @RestrictTo({LIBRARY, LIBRARY_GROUP, SUBCLASSES})
    void onMoving(boolean isDragging, float percent, int offset, int height, int maxDragHeight);

    /**
     * 【】release（，triggerLoad）
     *
     * @param refreshLayout RefreshLayout
     * @param height height HeaderHeight or FooterHeight
     * @param maxDragHeight dragheight
     */
    @RestrictTo({LIBRARY, LIBRARY_GROUP, SUBCLASSES})
    void onReleased(@NonNull RefreshLayout refreshLayout, int height, int maxDragHeight);

    /**
     * 【】startanimation
     *
     * @param refreshLayout RefreshLayout
     * @param height HeaderHeight or FooterHeight
     * @param maxDragHeight dragheight
     */
    @RestrictTo({LIBRARY, LIBRARY_GROUP, SUBCLASSES})
    void onStartAnimator(@NonNull RefreshLayout refreshLayout, int height, int maxDragHeight);

    /**
     * 【】animationend
     *
     * @param refreshLayout RefreshLayout
     * @param success SuccessRefreshLoad
     * @return completeanimationtime Return Integer.MAX_VALUE Cancelcomplete，state
     */
    @RestrictTo({LIBRARY, LIBRARY_GROUP, SUBCLASSES})
    int onFinish(@NonNull RefreshLayout refreshLayout, boolean success);

    /**
     * 【】directiondrag
     *
     * @param percentX pull-down，finger（0 - percentX - 1）
     * @param offsetX pull-down，finger（0 - offsetX - LayoutWidth）
     * @param offsetMax
     */
    @RestrictTo({LIBRARY, LIBRARY_GROUP, SUBCLASSES})
    void onHorizontalDrag(float percentX, int offsetX, int offsetMax);

    /**
     * directiondrag（onHorizontalDrag）
     *
     * @return dragtime，Not supportedReturnfalse
     */
    boolean isSupportHorizontalDrag();
}
