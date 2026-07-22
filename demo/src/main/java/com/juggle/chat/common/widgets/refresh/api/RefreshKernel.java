package com.juggle.chat.common.widgets.refresh.api;

import android.animation.ValueAnimator;

import androidx.annotation.NonNull;

import com.juggle.chat.common.widgets.refresh.constant.RefreshState;


/** RefreshLayoutFeatureAPI Feature Header  Footer API Created by scwang on 2017/5/26. */
@SuppressWarnings({"unused", "UnusedReturnValue", "SameParameterValue"})
public interface RefreshKernel {

    @NonNull
    RefreshLayout getRefreshLayout();

    @NonNull
    RefreshContent getRefreshContent();

    RefreshKernel setState(@NonNull RefreshState state);

    // <editor-fold desc="View displacement Spinner">

    /**
     * startRefresh
     *
     * @param open
     * @return RefreshKernel
     */
    RefreshKernel startTwoLevel(boolean open);

    /**
     * endRefresh
     *
     * @return RefreshKernel
     */
    RefreshKernel finishTwoLevel();

    /**
     * Viewposition moveSpinner
     *
     * @param spinner position (px)
     * @param isDragging true fingerdrag false reboundanimation
     * @return RefreshKernel
     */
    RefreshKernel moveSpinner(int spinner, boolean isDragging);

    /**
     * animationView position moveSpinner
     *
     * @param endSpinner endposition (px)
     * @return ValueAnimator animation null
     */
    ValueAnimator animSpinner(int endSpinner);
    // </editor-fold>

    // <editor-fold desc="Request events">

    /**
     * pull-down Header  Footer
     *
     * @param internal Header Footer  this
     * @param backgroundColor color
     * @return RefreshKernel
     */
    RefreshKernel requestDrawBackgroundFor(@NonNull RefreshComponent internal, int backgroundColor);
    /**
     *
     *
     * @param internal Header Footer  this
     * @param request
     * @return RefreshKernel
     */
    RefreshKernel requestNeedTouchEventFor(@NonNull RefreshComponent internal, boolean request);
    /**
     * SetDefaultContentScrollSet
     *
     * @param internal Header Footer  this
     * @param translation
     * @return RefreshKernel
     */
    RefreshKernel requestDefaultTranslationContentFor(
            @NonNull RefreshComponent internal, boolean translation);
    /**
     * rebuild headerHeight  footerHeight ,  height height WRAP_CONTENT
     *
     * @param internal Header Footer  this
     * @return RefreshKernel
     */
    RefreshKernel requestRemeasureHeightFor(@NonNull RefreshComponent internal);
    /**
     * Setrebound
     *
     * @param duration rebound
     * @return RefreshKernel
     */
    RefreshKernel requestFloorDuration(int duration);
    /**
     * SetBottomheight
     *
     * @return RefreshKernel
     */
    RefreshKernel requestFloorBottomPullUpToCloseRate(float rate);
    // </editor-fold>
}
