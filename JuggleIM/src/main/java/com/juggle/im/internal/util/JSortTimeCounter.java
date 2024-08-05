package com.juggle.im.internal.util;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * @author Ye_Guli
 * @create 2024-08-02 18:21
 */
public class JSortTimeCounter {
    private static final String SP_NAME = "j_im_core_stc";
    private static final Object lock = new Object();

    private final String mSortTimeKey;
    private final SharedPreferences mSharedPreferences;

    private volatile long mCurrentSortTime;

    public JSortTimeCounter(Context context, String appKey, String userId) {
        mSortTimeKey = String.format("key_%s_%s", appKey, userId);
        mSharedPreferences = context.getSharedPreferences(SP_NAME, Context.MODE_PRIVATE);
        mCurrentSortTime = mSharedPreferences.getLong(mSortTimeKey, 0L);
    }

    public long getNextSortTime() {
        synchronized (lock) {
            mCurrentSortTime++;
            mSharedPreferences.edit().putLong(mSortTimeKey, mCurrentSortTime).apply();
            return mCurrentSortTime;
        }
    }
}