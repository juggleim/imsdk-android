package com.jet.im.kit.interfaces;

import androidx.annotation.Nullable;

import com.sendbird.android.exception.SendbirdException;

/**
 * Interface definition that delivers the results of the request.
 */
public interface OnCompleteHandler {
    /**
     * Called when request is finished.
     * The presence of a value of e delivered to the parameter is a case where the request fails.
     *
     * @param e The object of exception.
     * since 3.0.0
     */
    void onComplete(@Nullable Exception e);
}
