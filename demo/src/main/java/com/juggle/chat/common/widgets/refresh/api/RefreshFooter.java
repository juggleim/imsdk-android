package com.juggle.chat.common.widgets.refresh.api;

import static androidx.annotation.RestrictTo.Scope.LIBRARY;
import static androidx.annotation.RestrictTo.Scope.LIBRARY_GROUP;
import static androidx.annotation.RestrictTo.Scope.SUBCLASSES;

import androidx.annotation.RestrictTo;

/** RefreshBottom Created by scwang on 2017/5/26. */
public interface RefreshFooter extends RefreshComponent {

    /**
     * 【】SetLoadcomplete，triggerLoadFeature
     *
     * @param noMoreData
     * @return true LoadcompletestateShow false Not supported
     */
    @RestrictTo({LIBRARY, LIBRARY_GROUP, SUBCLASSES})
    boolean setNoMoreData(boolean noMoreData);
}
