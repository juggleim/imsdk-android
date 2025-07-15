package com.juggle.im.internal.core.network.wscallback;

public abstract class CallAuthCallback implements IWebSocketCallback {
    public abstract void onSuccess(String zegoToken);
    public abstract void onError(int errorCode);
}
