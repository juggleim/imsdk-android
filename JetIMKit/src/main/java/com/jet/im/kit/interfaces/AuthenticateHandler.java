package com.jet.im.kit.interfaces;

/**
 * Interface definition for a callback to be invoked when UIKit tries to authenticate the current user.
 *
 * since 3.0.0
 */
public interface AuthenticateHandler {
    /**
     * Calls when the current user is authenticated in each component of UIKit.
     * On the contrary, if the authentication is failed {@link #onAuthenticationFailed()} will be called.
     *
     * since 3.0.0
     */
    void onAuthenticated();

    /**
     * Calls when current user authentication fails in each component of UIKit.
     * On the contrary, if the authentication is made successfully {@link #onAuthenticated()} will be called.
     *
     * since 3.0.0
     */
    void onAuthenticationFailed();
}
