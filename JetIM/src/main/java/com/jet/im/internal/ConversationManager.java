package com.jet.im.internal;

import com.jet.im.internal.core.JetIMCore;
import com.jet.im.interfaces.IConversationManager;
import com.jet.im.internal.core.network.SyncConversationsCallback;
import com.jet.im.internal.model.ConcreteConversationInfo;
import com.jet.im.model.Conversation;
import com.jet.im.model.ConversationInfo;
import com.jet.im.utils.LoggerUtils;

import java.util.List;

public class ConversationManager implements IConversationManager {

    public ConversationManager(JetIMCore core) {
        this.mCore = core;
    }

    void syncConversations(ICompleteCallback callback) {
        mCore.getWebSocket().syncConversations(mCore.getConversationSyncTime(), CONVERSATION_SYNC_COUNT, mCore.getUserId(), new SyncConversationsCallback() {
            @Override
            public void onSuccess(List<ConcreteConversationInfo> conversationInfoList, boolean isFinished) {
                if (conversationInfoList.size() > 0) {
                    ConcreteConversationInfo last = conversationInfoList.get(conversationInfoList.size() - 1);
                    if (last.getUpdateTime() > 0) {
                        mCore.setConversationSyncTime(last.getUpdateTime());
                    }
                    mCore.getDbManager().insertConversations(conversationInfoList);
                }
                if (!isFinished) {
                    syncConversations(callback);
                } else {
                    if (callback != null) {
                        callback.onComplete();
                    }
                }
            }

            @Override
            public void onError(int errorCode) {
                LoggerUtils.e("sync conversation fail, code is " + errorCode);
                if (callback != null) {
                    callback.onComplete();
                }
            }
        });
    }

    @Override
    public ConversationInfo getConversationInfo(Conversation conversation) {
        return mCore.getDbManager().getConversationInfo(conversation);
    }

    interface ICompleteCallback {
        void onComplete();
    }

    private final JetIMCore mCore;
    private static final int CONVERSATION_SYNC_COUNT = 100;
}
