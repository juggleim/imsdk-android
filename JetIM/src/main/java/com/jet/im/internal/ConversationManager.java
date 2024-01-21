package com.jet.im.internal;

import com.jet.im.internal.core.JetIMCore;
import com.jet.im.interfaces.IConversationManager;
import com.jet.im.internal.core.network.SyncConversationsCallback;
import com.jet.im.internal.model.ConcreteConversationInfo;

import java.util.List;

public class ConversationManager implements IConversationManager {

    public ConversationManager(JetIMCore core) {
        this.mCore = core;
    }

    public void syncConversations(ICompleteCallback callback) {
        //todo db
        mCore.getWebSocket().syncConversations(0, CONVERSATION_SYNC_COUNT, mCore.getUserId(), new SyncConversationsCallback() {
            @Override
            public void onSuccess(List<ConcreteConversationInfo> conversationInfoList, boolean isFinished) {

            }

            @Override
            public void onError(int errorCode) {

            }
        });
    }

    interface ICompleteCallback {
        void onComplete();
    }

    private JetIMCore mCore;
    private static final int CONVERSATION_SYNC_COUNT = 100;
}
