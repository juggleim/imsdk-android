package com.jet.im.kit.interfaces;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * Interface definition for a information of user.
 */
public interface UserInfo {
    /**
     * Provides the identifier of the user.
     *
     * @return the identifier of the user.
     */
    @NonNull
    String getUserId();

    /**
     * Provides the nickname of the user.
     *
     * @return the nickname of the user.
     */
    @Nullable
    String getNickname();

    /**
     * Provides the profile url of the user.
     *
     * @return the profile url of the user.
     */
    @Nullable
    String getProfileUrl();
}
