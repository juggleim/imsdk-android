package com.juggle.im;

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

    //非好友关系
    public static final int NOT_FRIEND = 12009;
    //没有操作权限
    public static final int NO_OPERATION_PERMISSION = 12010;
    //消息不存在
    public static final int REMOTE_MESSAGE_NOT_EXIST = 12011;
    //收藏重复消息
    public static final int ADD_DUPLICATE_FAVORITE_MESSAGE = 12012;

    //群组不存在
    public static final int GROUP_NOT_EXIST = 13001;
    //不是群成员
    public static final int NOT_GROUP_MEMBER = 13002;

    //聊天室默认错误
    public static final int CHATROOM_UNKNOWN_ERROR = 14000;
    //非聊天室成员
    public static final int NOT_CHATROOM_MEMBER = 14001;
    //聊天室属性已满（最多 100 个）
    public static final int CHATROOM_ATTRIBUTES_COUNT_EXCEED = 14002;
    //无权限操作聊天室属性（非当前用户设置的 key）
    public static final int CHATROOM_KEY_UNAUTHORIZED = 14003;
    //聊天室属性不存在
    public static final int CHATROOM_ATTRIBUTE_NOT_EXIST = 14004;
    //聊天室不存在
    public static final int CHATROOM_NOT_EXIST = 14005;
    //聊天室已销毁
    public static final int CHATROOM_DESTROYED = 14006;

    public static final int INVALID_PARAM = 21003;
    public static final int OPERATION_TIMEOUT = 21004;
    public static final int CONNECTION_UNAVAILABLE = 21005;
    public static final int SERVER_SET_ERROR = 21006;
    public static final int CONNECTION_ALREADY_EXIST = 21007;

    public static final int MESSAGE_NOT_EXIST = 22001;
    public static final int MESSAGE_ALREADY_RECALLED = 22002;
    public static final int MESSAGE_UPLOAD_ERROR = 22003;
    //不是媒体消息
    public static final int MESSAGE_DOWNLOAD_ERROR_NOT_MEDIA_MESSAGE = 23001;
    //媒体消息url 为空
    public static final int MESSAGE_DOWNLOAD_ERROR_URL_EMPTY = 23002;
    //appKey 或 userid 为 null
    public static final int MESSAGE_DOWNLOAD_ERROR_APP_KEY_OR_USERID_EMPTY = 23004;
    public static final int MESSAGE_DOWNLOAD_ERROR_SAVE_PATH_EMPTY = 23005;
    public static final int MESSAGE_DOWNLOAD_ERROR = 23006;
    public static final int FILE_SAVED_FAILED = 23007;
    //批量设置聊天室属性失败
    public static final int CHATROOM_BATCH_SET_ATTRIBUTE_FAIL = 24001;
}
