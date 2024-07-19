package com.jet.im.kit.interfaces;

/**
 * Interface definition for a callback to be invoked when UIKit tries to authenticate the current user.
 *
 * since 3.0.0
 */
public interface ConnectHandler {

    void onConnected(Exception e);


}
