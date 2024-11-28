package com.juggle.im.internal.connect.fsm;

import java.util.HashMap;
import java.util.Map;

public class ConnEvent {
    public final static int USER_CONNECT = 101;
    public final static int USER_DISCONNECT = 102;

    public final static int CONNECT_DONE = 201;
    public final static int CONNECT_FAILURE = 202;

    public final static int RECONNECT_TIMER_FIRE = 301;
    public final static int NETWORK_AVAILABLE = 302;
    public final static int ENTER_FOREGROUND = 303;
    public final static int ENTER_BACKGROUND = 304;
    public final static int WEBSOCKET_FAIL = 305;
    public final static int REMOTE_DISCONNECT = 306;
    public final static int CONNECTING_TIMEOUT = 307;

    public static String nameOfEvent(int event) {
        return sEventNameMap.get(event);
    }

    private static final Map<Integer, String> sEventNameMap = new HashMap<Integer, String>() {{
        put(USER_CONNECT, "user connect");
        put(USER_DISCONNECT, "user disconnect");
        put(CONNECT_DONE, "connect done");
        put(CONNECT_FAILURE, "connect failure");
        put(RECONNECT_TIMER_FIRE, "reconnect timer fire");
        put(NETWORK_AVAILABLE, "network available");
        put(ENTER_FOREGROUND, "enter foreground");
        put(ENTER_BACKGROUND, "enter background");
        put(WEBSOCKET_FAIL, "websocket fail");
        put(REMOTE_DISCONNECT, "remote disconnect");
        put(CONNECTING_TIMEOUT, "connecting timeout");
    }};

}
