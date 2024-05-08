package com.jet.im.kit.interfaces;

import androidx.annotation.Nullable;

import java.util.List;

/**
 * Interface definition for a callback to be invoked when a result of a list of user is loaded.
 */
public interface UserListResultHandler {
    /**
     * Called when a result of a list of user is loaded.
     *
     * @param userList The list of user that was loaded.
     * @param e The exception when the request fails.
     * @see UserInfo
     */
    void onResult(@Nullable List<? extends UserInfo> userList, @Nullable Exception e);
}
