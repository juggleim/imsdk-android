package com.jet.im.model;

import java.util.List;

/**
 * @author Ye_Guli
 * @create 2024-07-04 17:33
 */
public class MessageQueryOptions {
    private boolean mOpenSearch;
    private String mSearchContent;
    private List<String> mSenderUserIds;
    private List<String> mContentTypes;
    private List<Conversation> mConversations;
    private List<Message.MessageState> mStates;
    private Message.MessageDirection mDirection;

    public MessageQueryOptions() {
    }

    public MessageQueryOptions(Builder builder) {
        this.mOpenSearch = builder.mOpenSearch;
        this.mSearchContent = builder.mSearchContent;
        this.mSenderUserIds = builder.mSenderUserIds;
        this.mContentTypes = builder.mContentTypes;
        this.mConversations = builder.mConversations;
        this.mStates = builder.mStates;
        this.mDirection = builder.mDirection;
    }

    public boolean isOpenSearch() {
        return mOpenSearch;
    }

    public void setOpenSearch(boolean openSearch) {
        this.mOpenSearch = openSearch;
    }

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

    public Message.MessageDirection getDirection() {
        return mDirection;
    }

    public void setDirection(Message.MessageDirection direction) {
        this.mDirection = direction;
    }

    public static class Builder {
        private boolean mOpenSearch;
        private String mSearchContent;
        private List<String> mSenderUserIds;
        private List<String> mContentTypes;
        private List<Conversation> mConversations;
        private List<Message.MessageState> mStates;
        private Message.MessageDirection mDirection;

        public Builder() {
        }

        public Builder setOpenSearch(boolean openSearch) {
            this.mOpenSearch = openSearch;
            return this;
        }

        public Builder setSearchContent(String searchContent) {
            this.mSearchContent = searchContent;
            return this;
        }

        public Builder setSenderUserIds(List<String> senderUserIds) {
            this.mSenderUserIds = senderUserIds;
            return this;
        }

        public Builder setContentTypes(List<String> contentTypes) {
            this.mContentTypes = contentTypes;
            return this;
        }

        public Builder setConversations(List<Conversation> conversations) {
            this.mConversations = conversations;
            return this;
        }

        public Builder setStates(List<Message.MessageState> states) {
            this.mStates = states;
            return this;
        }

        public Builder setDirection(Message.MessageDirection direction) {
            this.mDirection = direction;
            return this;
        }

        public MessageQueryOptions build() {
            return new MessageQueryOptions(this);
        }
    }
}