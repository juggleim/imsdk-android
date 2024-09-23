package com.juggle.chat.common.widgets.refresh.util;

import android.util.Log;
import android.view.View;

import com.juggle.chat.common.widgets.refresh.api.RefreshKernel;
import com.juggle.chat.common.widgets.refresh.listener.CoordinatorLayoutListener;


/** Design 兼容包缺省尝试 Created by scwang on 2018/1/29. */
public class DesignUtil {
    private static final String TAG = "DesignUtil";

    public static void checkCoordinatorLayout(
            View content, RefreshKernel kernel, final CoordinatorLayoutListener listener) {
        try { // try 不能删除，不然会出现兼容性问题
            // sdk 不做任何处理，不引用 CoordinatorLayout，则不支持;
            //            if (content instanceof CoordinatorLayout) {
            //                kernel.getRefreshLayout().setEnableNestedScroll(false);
            //                ViewGroup layout = (ViewGroup) content;
            //                for (int i = layout.getChildCount() - 1; i >= 0; i--) {
            //                    View view = layout.getChildAt(i);
            //                    if (view instanceof AppBarLayout) {
            //                        ((AppBarLayout) view).addOnOffsetChangedListener(new
            // AppBarLayout.OnOffsetChangedListener() {
            //                            @Override
            //                            public void onOffsetChanged(AppBarLayout appBarLayout, int
            // verticalOffset) {
            //                                listener.onCoordinatorUpdate(
            //                                        verticalOffset >= 0,
            //                                        (appBarLayout.getTotalScrollRange() +
            // verticalOffset) <= 0);
            //                            }
            //                        });
            //                    }
            //                }
            //            }
        } catch (Throwable e) {
            Log.e(TAG, "checkCoordinatorLayout", e);
        }
    }
}
