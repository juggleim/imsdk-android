package com.juggle.im.internal.model;

public class ChatroomAttributeItem {
    public enum ChatroomAttrOptType {
        DEFAULT(0),
        UPDATE(1),
        DELETE(2);

        ChatroomAttrOptType(int value) {
            this.mValue = value;
        }
        public int getValue() {
            return mValue;
        }
        public static ChatroomAttrOptType setValue(int value) {
            for (ChatroomAttrOptType t : ChatroomAttrOptType.values()) {
                if (value == t.mValue) {
                    return t;
                }
            }
            return DEFAULT;
        }
        private final int mValue;
    }

    public long getTimestamp() {
        return mTimestamp;
    }

    public void setTimestamp(long timestamp) {
        mTimestamp = timestamp;
    }

    public String getKey() {
        return mKey;
    }

    public void setKey(String key) {
        mKey = key;
    }

    public String getValue() {
        return mValue;
    }

    public void setValue(String value) {
        mValue = value;
    }

    public String getUserId() {
        return mUserId;
    }

    public void setUserId(String userId) {
        mUserId = userId;
    }

    public int getCode() {
        return mCode;
    }

    public void setCode(int code) {
        mCode = code;
    }

    public ChatroomAttrOptType getType() {
        return mType;
    }

    public void setType(ChatroomAttrOptType type) {
        mType = type;
    }

    private String mKey;
    private String mValue;
    private String mUserId;
    private int mCode;
    private long mTimestamp;
    private ChatroomAttrOptType mType;
}
