package com.juggle.im.model;

public abstract class MessageContent {
    public enum MessageFlag {
        NONE(0),
        // Whether this is a command message.
        IS_CMD(1),
        // Whether this counts toward unread count.
        IS_COUNTABLE(2),
        // Whether this is a status message.
        IS_STATUS(4),
        // Whether this message is saved.
        IS_SAVE(8),
        IS_MODIFIED(16),
        IS_MERGED(32),
        IS_MUTE(64),
        IS_BROADCAST(128),
        // Whether this is an encrypted message.
        IS_E2EE(2048);

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

    public String getSearchContent() {
        return "";
    }
}
