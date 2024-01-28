package com.jet.im.interfaces;

import com.jet.im.JetIMConst;
import com.jet.im.model.Conversation;
import com.jet.im.model.ConversationInfo;

import java.util.List;

public interface IConversationManager {

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
}
