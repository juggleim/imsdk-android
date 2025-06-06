package com.juggle.im.internal.core.network;

public abstract class WebSocketDataCallback<T> implements IWebSocketCallback {
    public abstract void onSuccess(T data);
    public abstract void onError(int errorCode);
}
