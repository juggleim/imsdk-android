package com.jet.im.interfaces;

import com.jet.im.model.Conversation;
import com.jet.im.model.ConversationInfo;

public interface IConversationManager {

    public ConversationInfo getConversationInfo(Conversation conversation);
}
