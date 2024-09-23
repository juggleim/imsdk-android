package com.jet.im.kit.interfaces;

import androidx.annotation.Nullable;

import com.juggle.im.model.Message;

import java.util.List;

/**
 * Interface definition that delivers the results of the request.
 */
public interface MessageHandler {
    /**
     * Called when request is finished.
     * The presence of a value of e delivered to the parameter is a case where the request fails.
     *
     * @param e The object of exception.
     *          since 3.0.0
     */
    void onResult(List<Message> list, @Nullable Exception e);
}
