package com.jet.im.interfaces;

import com.jet.im.model.Conversation;
import com.jet.im.model.ConversationInfo;

import java.util.List;

public interface IConversationManager {

    List<ConversationInfo> getConversationInfoList();

    ConversationInfo getConversationInfo(Conversation conversation);
}
