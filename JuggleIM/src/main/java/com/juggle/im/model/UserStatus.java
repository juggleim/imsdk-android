package com.juggle.im.model;

public class UserStatus {
    public enum UserStatusType {
        UNKNOWN(0),
        ONLINE(1),
        OFFLINE(2);

        UserStatusType(int value) {
            this.mValue = value;
        }

        public int getValue() {
            return mValue;
        }

        public static UserStatusType setValue(int value) {
            for (UserStatusType t : UserStatusType.values()) {
                if (value == t.mValue) {
                    return t;
                }
            }
            return UNKNOWN;
        }

        private final int mValue;
    }

    public UserStatusType getStatusType() {
        return mStatusType;
    }

    public void setStatusType(UserStatusType statusType) {
        mStatusType = statusType;
    }

    public String getUserId() {
        return mUserId;
    }

    public void setUserId(String userId) {
        mUserId = userId;
    }

    private String mUserId;
    private UserStatusType mStatusType;
}
