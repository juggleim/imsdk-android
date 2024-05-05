package com.jet.im.interfaces;

import com.jet.im.JetIMConst;
import com.jet.im.model.Conversation;
import com.jet.im.model.ConversationInfo;

import java.util.List;

public interface IConversationManager {

    interface ISimpleCallback {
        void onSuccess();

        void onError(int errorCode);
    }

    List<ConversationInfo> getConversationInfoList();

    List<ConversationInfo> getConversationInfoList(int[] conversationTypes,
                                                   int count,
                                                   long timestamp,
                                                   JetIMConst.PullDirection direction);

    List<ConversationInfo> getConversationInfoList(int count,
                                                   long timestamp,
                                                   JetIMConst.PullDirection direction);

    ConversationInfo getConversationInfo(Conversation conversation);

    void deleteConversationInfo(Conversation conversation);

    void setDraft(Conversation conversation, String draft);

    void clearDraft(Conversation conversation);

    void setMute(Conversation conversation,
                 boolean isMute,
                 ISimpleCallback callback);

    void setTop(Conversation conversation, boolean isTop);

    List<ConversationInfo> getTopConversationInfoList(int count,
                                                      long timestamp,
                                                      JetIMConst.PullDirection direction);

    int getTotalUnreadCount();

    void clearUnreadCount(Conversation conversation);

    void clearTotalUnreadCount();

    void addListener(String key, IConversationListener listener);

    void removeListener(String key);

    void addSyncListener(String key, IConversationSyncListener listener);

    void removeSyncListener(String key);

    interface IConversationListener {
        void onConversationInfoAdd(List<ConversationInfo> conversationInfoList);

        void onConversationInfoUpdate(List<ConversationInfo> conversationInfoList);

        void onConversationInfoDelete(List<ConversationInfo> conversationInfoList);

        void onTotalUnreadMessageCountUpdate(int count);
    }

    interface IConversationSyncListener {
        void onConversationSyncComplete();
    }
}
