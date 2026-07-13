package com.juggle.im.model;

import java.util.List;

/**
 * @author Ye_Guli
 * @create 2024-07-04 17:33
 */
public class MessageQueryOptions {
    private final String mSearchContent;//Specified search content.
    private final List<String> mSenderUserIds;//Specified message sender IDs.
    private final List<String> mContentTypes;//Specified message types.
    private final List<Conversation> mConversations;//Specified conversations.
    private final List<Message.MessageState> mStates;//Specified message states.
    private final Message.MessageDirection mDirection;//Specified message direction.
    private final List<Conversation.ConversationType> mConversationTypes;//Specified conversation types.

    public MessageQueryOptions(Builder builder) {
        this.mSearchContent = builder.mSearchContent;
        this.mSenderUserIds = builder.mSenderUserIds;
        this.mContentTypes = builder.mContentTypes;
        this.mConversations = builder.mConversations;
        this.mStates = builder.mStates;
        this.mDirection = builder.mDirection;
        this.mConversationTypes = builder.mConversationTypes;
    }

    public String getSearchContent() {
        return mSearchContent;
    }

    public List<String> getSenderUserIds() {
        return mSenderUserIds;
    }

    public List<String> getContentTypes() {
        return mContentTypes;
    }

    public List<Conversation> getConversations() {
        return mConversations;
    }

    public List<Message.MessageState> getStates() {
        return mStates;
    }

    public Message.MessageDirection getDirection() {
        return mDirection;
    }

    public List<Conversation.ConversationType> getConversationTypes() {
        return mConversationTypes;
    }

    public static class Builder {
        private String mSearchContent;
        private List<String> mSenderUserIds;
        private List<String> mContentTypes;
        private List<Conversation> mConversations;
        private List<Message.MessageState> mStates;
        private Message.MessageDirection mDirection;
        private List<Conversation.ConversationType> mConversationTypes;

        public Builder() {
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

        public Builder setConversationTypes(List<Conversation.ConversationType> conversationTypes) {
            this.mConversationTypes = conversationTypes;
            return this;
        }

        public MessageQueryOptions build() {
            return new MessageQueryOptions(this);
        }
    }
}
