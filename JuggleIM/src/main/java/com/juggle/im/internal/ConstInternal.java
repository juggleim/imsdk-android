package com.juggle.im.internal;

public class ConstInternal {
    public static final String SDK_VERSION = "1.8.11.1";
    public static final String PLATFORM = "Android";
    public static final String LOG_UPLOAD_HEADER_APP_KEY = "x-appkey";
    public static final String LOG_UPLOAD_HEADER_TOKEN = "x-token";
    public static final int THUMBNAIL_WIDTH = 240;
    public static final int THUMBNAIL_HEIGHT = 240;
    public static final int THUMBNAIL_QUALITY = 30;

    public static class ErrorCode {
        public static final int NONE = 0;
        //未传 AppKey
        public static final int APP_KEY_EMPTY = 11001;
        //未传 Token
        public static final int TOKEN_EMPTY = 11002;
        //AppKey 不存在
        public static final int APP_KEY_INVALID = 11003;
        //Token 不合法
        public static final int TOKEN_ILLEGAL = 11004;
        //Token 未授权
        public static final int TOKEN_UNAUTHORIZED = 11005;
        //Token 已过期
        public static final int TOKEN_EXPIRED = 11006;
        //App 已封禁
        public static final int APP_PROHIBITED = 11009;
        //用户被封禁
        public static final int USER_PROHIBITED = 11010;
        //用户被踢下线
        public static final int USER_KICKED_BY_OTHER_CLIENT = 11011;
        //用户注销下线
        public static final int USER_LOG_OUT = 11012;

        //群组不存在
        public static final int GROUP_NOT_EXIST = 13001;
        //不是群成员
        public static final int NOT_GROUP_MEMBER = 13002;

        public static final int WEB_SOCKET_FAILURE = 21001;
        public static final int NAVI_FAILURE = 21002;
        public static final int INVALID_PARAM = 21003;
        public static final int OPERATION_TIMEOUT = 21004;
        public static final int CONNECTION_UNAVAILABLE = 21005;
        public static final int SERVER_SET_ERROR = 21006;
        public static final int CONNECTION_ALREADY_EXIST = 21007;

        public static final int MESSAGE_NOT_EXIST = 22001;
        public static final int MESSAGE_ALREADY_RECALLED = 22002;
    }
}
