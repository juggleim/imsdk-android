package com.jet.im.kit.adapter;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.sendbird.android.handler.InitResultHandler;
import com.jet.im.kit.SendbirdUIKit;
import com.jet.im.kit.interfaces.UserInfo;

/**
 * Adapters provides a binding from a {@link com.sendbird.android.SendbirdChat} set to a connection
 * within a {@link SendbirdUIKit#init(SendbirdUIKitAdapter, Context)}.
 */
public interface SendbirdUIKitAdapter {
    /**
     * Provides the identifier of an app.
     *
     * @return the identifier of an app.
     */
    @NonNull
    String getAppId();

    /**
     * Provides the access token to SendBird server.
     *
     * @return the access token.
     */
    @Nullable
    String getAccessToken();


    /**
     * Provides the {@link UserInfo} to access SendBird server.
     *
     * @return the current user.
     */
    @NonNull
    UserInfo getUserInfo();

    /**
     * Provides the {@link InitResultHandler} to initialize SendBird Chat SDK.
     *
     * @return the handler of SendBird Chat SDK initialization.
     * since 2.2.0
     */
    @NonNull
    InitResultHandler getInitResultHandler();
}
