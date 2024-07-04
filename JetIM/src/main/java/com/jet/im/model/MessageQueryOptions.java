package com.jet.im.model;

import java.util.List;

/**
 * @author Ye_Guli
 * @create 2024-07-04 17:33
 */
public class MessageQueryOptions {
    private String mSearchContent;
    private List<String> mSenderUserIds;
    private List<String> mContentTypes;
    private List<Conversation> mConversations;
    private List<Message.MessageState> mStates;
    private List<Message.MessageDirection> mDirections;

    public String getSearchContent() {
        return mSearchContent;
    }

    public void setSearchContent(String searchContent) {
        this.mSearchContent = searchContent;
    }

    public List<String> getSenderUserIds() {
        return mSenderUserIds;
    }

    public void setSenderUserIds(List<String> senderUserIds) {
        this.mSenderUserIds = senderUserIds;
    }

    public List<String> getContentTypes() {
        return mContentTypes;
    }

    public void setContentTypes(List<String> contentTypes) {
        this.mContentTypes = contentTypes;
    }

    public List<Conversation> getConversations() {
        return mConversations;
    }

    public void setConversations(List<Conversation> conversations) {
        this.mConversations = conversations;
    }

    public List<Message.MessageState> getStates() {
        return mStates;
    }

    public void setStates(List<Message.MessageState> states) {
        this.mStates = states;
    }

    public List<Message.MessageDirection> getDirections() {
        return mDirections;
    }

    public void setDirections(List<Message.MessageDirection> directions) {
        this.mDirections = directions;
    }
}