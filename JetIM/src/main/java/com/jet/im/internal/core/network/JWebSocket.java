package com.jet.im.internal.core.network;

import com.jet.im.utils.LoggerUtils;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.util.List;

public class JWebSocket extends WebSocketClient {


    public JWebSocket(String appKey, String token, URI serverUri) {
        super(serverUri);
        mAppKey = appKey;
        mToken = token;
    }

    public static URI createWebSocketUri(String server) {
        String webSocketUrl = WEB_SOCKET_PREFIX + server + WEB_SOCKET_SUFFIX;
        return URI.create(webSocketUrl);
    }

    @Override
    public void onOpen(ServerHandshake handshakedata) {
        LoggerUtils.i("lifei, onOpen");
    }

    @Override
    public void onMessage(String message) {
        LoggerUtils.i("lifei, onMessage");
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        LoggerUtils.i("lifei, onClose");
    }

    @Override
    public void onError(Exception ex) {
        LoggerUtils.i("lifei, onError");
    }

    public void setToken(String token) {
        mToken = token;
    }
    private String mAppKey;
    private String mToken;
    private static final String WEB_SOCKET_PREFIX = "ws://";
    private static final String WEB_SOCKET_SUFFIX = "/im";


}
