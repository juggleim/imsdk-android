package com.jet.im.kit.interfaces;

import android.view.View;

import androidx.annotation.NonNull;

import com.sendbird.android.message.BaseMessage;
import com.jet.im.kit.model.Action;

/**
 * Interface definition for a callback to be invoked when a item is invoked with an event.
 *
 * since 3.5.0
 */
public interface OnNotificationTemplateActionHandler {
    /**
     * If an Action is registered in a specific view, it is called when a click event occurs.
     *
     * @param view the view that was clicked.
     * @param action the registered Action data
     * @param message the clicked message
     * since 3.5.0
     */
    void onHandleAction(@NonNull View view, @NonNull Action action, @NonNull BaseMessage message);
}
