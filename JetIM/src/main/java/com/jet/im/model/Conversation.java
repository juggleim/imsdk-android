package com.jet.im.model;

import androidx.annotation.NonNull;

import java.util.Objects;

public class Conversation {
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Conversation that = (Conversation) o;
        return Objects.equals(mConversationId, that.mConversationId) && mConversationType == that.mConversationType;
    }

    @Override
    public int hashCode() {
        return Objects.hash(mConversationId, mConversationType);
    }

    @NonNull
    @Override
    public String toString() {
        return "Conversation{" +
                "mConversationId='" + mConversationId + '\'' +
                ", mConversationType=" + mConversationType +
                '}';
    }

    public Conversation(ConversationType type, String conversationId) {
        this.mConversationType = type;
        this.mConversationId = conversationId;
    }
    public enum ConversationType {
        UNKNOWN(0),
        /// 单聊
        PRIVATE(1),
        /// 群组
        GROUP(2),
        /// 聊天室
        CHATROOM(3),
        /// 系统会话
        SYSTEM(4);

        ConversationType(int value) {
            this.mValue = value;
        }
        public int getValue() {
            return mValue;
        }
        public static ConversationType setValue(int value) {
            for (ConversationType t : ConversationType.values()) {
                if (value == t.mValue) {
                    return t;
                }
            }
            return UNKNOWN;
        }
        private final int mValue;
    }

    public String getConversationId() {
        return mConversationId;
    }
    public ConversationType getConversationType() {
        return mConversationType;
    }

    private final String mConversationId;
    private final ConversationType mConversationType;
}
