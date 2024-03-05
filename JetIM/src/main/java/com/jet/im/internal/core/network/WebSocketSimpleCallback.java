package com.jet.im.internal.core.network;

public abstract class WebSocketSimpleCallback implements IWebSocketCallback {
    public abstract void onSuccess();
    public abstract void onError(int errorCode);
}
