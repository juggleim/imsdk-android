package com.jet.im.internal.core.network;

import com.jet.im.internal.model.ConcreteConversationInfo;

public abstract class AddConversationsCallback implements IWebSocketCallback {
    public abstract void onSuccess(ConcreteConversationInfo conversationInfo);

    public abstract void onError(int errorCode);
}