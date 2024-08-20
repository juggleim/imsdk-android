package com.juggle.im.internal.model;

import com.juggle.im.model.Conversation;

import java.util.List;

/**
 * @author Ye_Guli
 * @create 2024-06-26 15:24
 */
public class MergeInfo {
    private Conversation mConversation;
    private List<ConcreteMessage> mMessages;
    private String mContainerMsgId;

    public Conversation getConversation() {
        return mConversation;
    }

    public void setConversation(Conversation conversation) {
        this.mConversation = conversation;
    }

    public List<ConcreteMessage> getMessages() {
        return mMessages;
    }

    public void setMessages(List<ConcreteMessage> messages) {
        this.mMessages = messages;
    }

    public String getContainerMsgId() {
        return mContainerMsgId;
    }

    public void setContainerMsgId(String containerMsgId) {
        this.mContainerMsgId = containerMsgId;
    }
}