package com.jet.im.internal.core.network;

import com.jet.im.internal.model.ConcreteConversationInfo;

public abstract class AddConversationCallback implements IWebSocketCallback {
    public abstract void onSuccess(long timestamp, ConcreteConversationInfo conversationInfo);

    public abstract void onError(int errorCode);
}