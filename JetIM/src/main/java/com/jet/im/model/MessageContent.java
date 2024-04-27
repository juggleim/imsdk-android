package com.jet.im.model;

public abstract class MessageContent {
    public enum MessageFlag {
        /// 是否命令消息
        IS_CMD(1),
        // 是否计未读数
        IS_COUNTABLE(2),
        // 是否状态消息
        IS_STATUS(4),
        // 是否存入历史消息
        IS_SAVE(8),
        IS_MODIFIED(16),
        IS_MERGED(32),
        IS_MUTE(64);
        public int getValue() {
            return mValue;
        }
        MessageFlag(int value) {
            this.mValue = value;
        }

        private final int mValue;
    }
    public MessageContent() {
        mContentType = "jg:unknown";
    }

    public String getContentType() {
        return mContentType;
    }
    public abstract byte[] encode();
    public abstract void decode(byte[] data);
    public String conversationDigest() {
        return "";
    }
    public int getFlags() {
        return MessageFlag.IS_COUNTABLE.getValue() | MessageFlag.IS_SAVE.getValue();
    }

    protected String mContentType;

    public String getSearchContent(){
        return "";
    }

    public MessageMentionInfo getMentionInfo() {
        return mMentionInfo;
    }

    public void setMentionInfo(MessageMentionInfo mentionInfo) {
        mMentionInfo = mentionInfo;
    }

    private MessageMentionInfo mMentionInfo;
}
