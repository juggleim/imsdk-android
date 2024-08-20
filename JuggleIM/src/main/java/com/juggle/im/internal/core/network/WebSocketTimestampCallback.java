package com.juggle.im.internal.core.network;

public abstract class WebSocketTimestampCallback implements IWebSocketCallback{
    public abstract void onSuccess(long timestamp);
    public abstract void onError(int errorCode);
}
