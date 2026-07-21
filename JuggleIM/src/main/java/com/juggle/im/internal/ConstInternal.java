package com.juggle.im.internal;

public class ConstInternal {
    public static final String SDK_VERSION = "1.9.1";
    public static final String PLATFORM = "Android";
    public static final String LOG_UPLOAD_HEADER_APP_KEY = "x-appkey";
    public static final String LOG_UPLOAD_HEADER_TOKEN = "x-token";
    public static final int THUMBNAIL_WIDTH = 240;
    public static final int THUMBNAIL_HEIGHT = 240;
    public static final int THUMBNAIL_QUALITY = 30;

    public static class ErrorCode {
        public static final int NONE = 0;
        //AppKey was not provided
        public static final int APP_KEY_EMPTY = 11001;
        //Token was not provided
        public static final int TOKEN_EMPTY = 11002;
        //AppKey does not exist
        public static final int APP_KEY_INVALID = 11003;
        //Token is invalid
        public static final int TOKEN_ILLEGAL = 11004;
        //Token is unauthorized
        public static final int TOKEN_UNAUTHORIZED = 11005;
        //Token has expired
        public static final int TOKEN_EXPIRED = 11006;
        //App is banned
        public static final int APP_PROHIBITED = 11009;
        //User is banned
        public static final int USER_PROHIBITED = 11010;
        //User was kicked offline
        public static final int USER_KICKED_BY_OTHER_CLIENT = 11011;
        //User logged out and went offline
        public static final int USER_LOG_OUT = 11012;

        //Not friends
        public static final int NOT_FRIEND = 12009;
        //No operation permission
        public static final int NO_OPERATION_PERMISSION = 12010;
        //Message does not exist
        public static final int REMOTE_MESSAGE_NOT_EXIST = 12011;
        //Duplicate favorite message
        public static final int ADD_DUPLICATE_FAVORITE_MESSAGE = 12012;
        //Encrypted chat hash does not match
        public static final int PUB_KEYS_HASH_MISMATCH = 12013;

        //Group does not exist
        public static final int GROUP_NOT_EXIST = 13001;
        //Not a group member
        public static final int NOT_GROUP_MEMBER = 13002;

        //The conversation has an unfinished call
        public static final int CALL_CONVERSATION_BOUND = 16011;
        //Connection forbidden
        public static final int CONNECT_FORBIDDEN = 21000;
        public static final int WEB_SOCKET_FAILURE = 21001;
        public static final int NAVI_FAILURE = 21002;
        public static final int INVALID_PARAM = 21003;
        public static final int OPERATION_TIMEOUT = 21004;
        public static final int CONNECTION_UNAVAILABLE = 21005;
        public static final int SERVER_SET_ERROR = 21006;
        public static final int CONNECTION_ALREADY_EXIST = 21007;

        public static final int MESSAGE_NOT_EXIST = 22001;
        public static final int MESSAGE_ALREADY_RECALLED = 22002;

        //Peer public key not found
        public static final int OTHER_SIDE_E2EE_INVALID = 22006;
        //Local public key has not been generated
        public static final int LOCAL_PUBLIC_KEY_INVALID = 22007;

        //Failed to join the LiveKit room
        public static final int JOIN_LIVEKIT_FAIL = 25001;
    }
}
