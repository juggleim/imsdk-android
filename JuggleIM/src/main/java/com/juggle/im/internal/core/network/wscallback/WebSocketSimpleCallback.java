package com.juggle.im.internal.core.network.wscallback;

public abstract class WebSocketSimpleCallback implements IWebSocketCallback {
    public abstract void onSuccess();
    public abstract void onError(int errorCode);
}
