package com.jet.im.internal;

public class ConstInternal {
    public static final String WEB_SOCKET_URL = "120.48.178.248:9002";
    public static final String NAVI_URL = "http://120.48.178.248:8083";
    public static final String PLATFORM = "Android";

    static class ErrorCode {
        static final int NONE = 0;
        //未传 AppKey
        static final int APP_KEY_EMPTY = 11001;
        //未传 Token
        static final int TOKEN_EMPTY = 11002;
        //AppKey 不存在
        static final int APP_KEY_INVALID = 11003;
        //Token 不合法
        static final int TOKEN_ILLEGAL = 11004;
        //Token 未授权
        static final int TOKEN_UNAUTHORIZED = 11005;
        //Token 已过期
        static final int TOKEN_EXPIRED = 11006;
        //App 已封禁
        static final int APP_PROHIBITED = 11009;
        //用户被封禁
        static final int USER_PROHIBITED = 11010;
        //用户被踢下线
        static final int USER_KICKED_BY_OTHER_CLIENT = 11011;
        //用户注销下线
        static final int USER_LOG_OUT = 11012;

        //群组不存在
        static final int GROUP_NOT_EXIST = 13001;
        //不是群成员
        static final int NOT_GROUP_MEMBER = 13002;

        static final int WEB_SOCKET_FAILURE = 21001;
        static final int NAVI_FAILURE = 21002;
        static final int INVALID_PARAM = 21003;

        static final int MESSAGE_NOT_EXIST = 22001;
        static final int MESSAGE_ALREADY_RECALLED = 22002;
    }
}
