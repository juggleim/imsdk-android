package com.juggle.im.internal.core.db;

class ProfileSql {
    static final String SQL_CREATE_TABLE = "CREATE TABLE IF NOT EXISTS profile ("
            + "key VARCHAR (64) PRIMARY KEY,"
            + "value VARCHAR (64)"
            + ")";
    static final String SQL_GET_VALUE = "SELECT value FROM profile WHERE key = ?";
    static final String SQL_SET_VALUE = "INSERT OR REPLACE INTO profile (key, value) values (?, ?)";
    static final String COLUMN_VALUE = "value";
    static final String CONVERSATION_TIME = "conversation_time";
    static final String SEND_TIME = "send_time";
    static final String RECEIVE_TIME = "receive_time";

}
