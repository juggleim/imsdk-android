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

    public static URI createWebSocketUri(String server) {
        String webSocketUrl = WEB_SOCKET_PREFIX + server + WEB_SOCKET_SUFFIX;
        return URI.create(webSocketUrl);
    }

    public JWebSocket(String appKey, String token, URI serverUri, Context context) {
        super(serverUri);
        mAppKey = appKey;
        mToken = token;
        mContext = context;
        mPbData = new PBData();
    }

    public void disconnect(Boolean receivePush) {
        sendDisconnectMsg(receivePush);
    }

    public void setConnectionListener(IWebSocketConnectListener listener) {
        mConnectListener = listener;
    }

    public interface IWebSocketConnectListener {
        void onConnectComplete(int errorCode, String userId);
        void onWebSocketFail();
        void onWebSocketClose();
    }

    @Override
    public void onOpen(ServerHandshake handshakedata) {
        LoggerUtils.i("JWebSocket, onOpen");
        sendConnectMsg();
    }

    @Override
    public void onMessage(String message) {
        LoggerUtils.i("JWebSocket, onMessage");
    }

    @Override
    public void onMessage(ByteBuffer bytes) {
        LoggerUtils.i("JWebSocket, onMessage bytes");
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
        LoggerUtils.i("JWebSocket, onClose");
    }

    @Override
    public void onError(Exception ex) {
        LoggerUtils.i("JWebSocket, onError");
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

    private void sendDisconnectMsg(boolean receivePush) {
        byte[] bytes = mPbData.disconnectData(receivePush);
        send(bytes);
    }

    private void handleConnectAckMsg(PBRcvObj.ConnectAck ack) {
        LoggerUtils.i("connect userId is " + ack.userId);
        if (mConnectListener != null) {
            mConnectListener.onConnectComplete(ack.code, ack.userId);
        }
    }

    private String mAppKey;
    private String mToken;
    private PBData mPbData;
    private Context mContext;
    private IWebSocketConnectListener mConnectListener;
    private static final String WEB_SOCKET_PREFIX = "ws://";
    private static final String WEB_SOCKET_SUFFIX = "/im";


}
