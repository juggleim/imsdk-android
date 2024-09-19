package com.jet.im.kit.interfaces;

import androidx.annotation.Nullable;

import com.sendbird.android.exception.SendbirdException;

import java.util.List;

/**
 * Interface definition for a callback to be invoked when getting a result.
 *
 * since 1.2.0
 */
public interface OnListResultHandler<T> {
    /**
     * Called when a result has been completed.
     *
     * @param result The object of result.
     */
    void onResult(@Nullable List<T> result, @Nullable SendbirdException e);
}
