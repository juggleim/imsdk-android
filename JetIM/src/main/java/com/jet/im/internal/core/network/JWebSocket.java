package com.jet.im.internal.core.network;

import android.content.Context;
import android.os.Build;

import com.jet.im.internal.ConstInternal;
import com.jet.im.internal.util.JUtility;
import com.jet.im.utils.LoggerUtils;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.nio.ByteBuffer;

public class JWebSocket extends WebSocketClient {


    public JWebSocket(String appKey, String token, URI serverUri, Context context) {
        super(serverUri);
        mAppKey = appKey;
        mToken = token;
        mContext = context;
        mPbData = new PBData();
    }

    public static URI createWebSocketUri(String server) {
        String webSocketUrl = WEB_SOCKET_PREFIX + server + WEB_SOCKET_SUFFIX;
        return URI.create(webSocketUrl);
    }

    @Override
    public void onOpen(ServerHandshake handshakedata) {
        LoggerUtils.i("lifei, onOpen");
        sendConnectMsg();
    }

    @Override
    public void onMessage(String message) {
        LoggerUtils.i("lifei, onMessage");
    }

    @Override
    public void onMessage(ByteBuffer bytes) {
        LoggerUtils.i("lifei, onMessage bytes");
        PBRcvObj obj = mPbData.rcvObjWithBytes(bytes);
        switch (obj.getRcvType()) {
//            case PBRcvObj.PBRcvType.parseError:
//                break;

            case PBRcvObj.PBRcvType.connectAck:
                handleConnectAckMsg(obj.connectAck);
                break;

        }
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

    private void sendConnectMsg() {
        byte[] bytes = mPbData.connectData(mAppKey,
                mToken,
                JUtility.getDeviceId(mContext),
                ConstInternal.PLATFORM,
                Build.BRAND,
                Build.MODEL,
                Build.VERSION.RELEASE,
                "pushToken",
                JUtility.getNetworkType(mContext),
                JUtility.getCarrier(mContext),
                "");
        send(bytes);
    }

    private void handleConnectAckMsg(PBRcvObj.ConnectAck ack) {
        LoggerUtils.i("connect userId is " + ack.userId);
    }

    private String mAppKey;
    private String mToken;
    private PBData mPbData;



    private Context mContext;
    private static final String WEB_SOCKET_PREFIX = "ws://";
    private static final String WEB_SOCKET_SUFFIX = "/im";


}
