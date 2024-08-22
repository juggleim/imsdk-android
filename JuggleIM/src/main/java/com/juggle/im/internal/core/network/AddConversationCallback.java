package com.juggle.im.internal.core.network;

import com.juggle.im.internal.model.ConcreteConversationInfo;

public abstract class AddConversationCallback implements IWebSocketCallback {
    public abstract void onSuccess(long timestamp, ConcreteConversationInfo conversationInfo);

    public abstract void onError(int errorCode);
}