package com.jet.im.internal.core.network;

import com.jet.im.internal.model.ConcreteConversationInfo;

import java.util.List;

public abstract class SyncConversationsCallback implements IWebSocketCallback {
    public abstract void onSuccess(List<ConcreteConversationInfo> conversationInfoList, List<ConcreteConversationInfo> deleteConversationInfoList, boolean isFinished);
    public abstract void onError(int errorCode);
}
