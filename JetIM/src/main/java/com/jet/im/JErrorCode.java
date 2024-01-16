package com.jet.im;

public class JErrorCode {
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
}