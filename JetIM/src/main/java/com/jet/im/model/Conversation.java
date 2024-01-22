package com.jet.im.model;

public class Conversation {
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
