package com.jet.im.internal.core.network;

import android.content.Context;
import android.os.Build;

import androidx.annotation.NonNull;

import com.jet.im.JetIMConst;
import com.jet.im.internal.ConstInternal;
import com.jet.im.internal.model.ConcreteMessage;
import com.jet.im.internal.util.JUtility;
import com.jet.im.model.Conversation;
import com.jet.im.model.MessageContent;
import com.jet.im.utils.LoggerUtils;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

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
        mMsgCallbackMap = new ConcurrentHashMap<>();
    }

    public void disconnect(Boolean receivePush) {
        sendDisconnectMsg(receivePush);
    }

    public void setConnectionListener(IWebSocketConnectListener listener) {
        mConnectListener = listener;
    }

    public void setMessageListener(IWebSocketMessageListener listener) {
        mMessageListener = listener;
    }

    public void sendIMMessage(MessageContent content,
                              Conversation conversation,
                              String clientUid,
                              SendMessageCallback callback) {
        Integer key = mMsgIndex;
        byte[] bytes = mPbData.sendMessageData(content.getContentType(),
                content.encode(),
                content.getFlags(),
                clientUid,
                mMsgIndex++,
                conversation.getConversationType(),
                conversation.getConversationId());
        mMsgCallbackMap.put(key, callback);
        sendWhenOpen(bytes);
    }

    public void syncConversations(long startTime,
                                  int count,
                                  String userId,
                                  SyncConversationsCallback callback) {
        Integer key = mMsgIndex;
        byte[] bytes = mPbData.syncConversationsData(startTime, count, userId, mMsgIndex++);
        mMsgCallbackMap.put(key, callback);
        sendWhenOpen(bytes);
    }

    public void syncMessages(long receiveTime,
                             long sendTime,
                             String userId) {
        byte[] bytes = mPbData.syncMessagesData(receiveTime, sendTime, userId, mMsgIndex++);
        sendWhenOpen(bytes);
    }

    public void queryHisMsg(Conversation conversation, long startTime, int count, JetIMConst.PullDirection direction, QryHisMsgCallback callback) {
        Integer key = mMsgIndex;
        byte[] bytes = mPbData.queryHisMsgData(conversation, startTime, count, direction, mMsgIndex++);
        mMsgCallbackMap.put(key, callback);
        sendWhenOpen(bytes);
    }

    public void ping() {
        byte[] bytes = mPbData.pingData();
        sendWhenOpen(bytes);
    }

    public interface IWebSocketConnectListener {
        void onConnectComplete(int errorCode, String userId);
        void onDisconnect(int errorCode);
        void onWebSocketFail();
        void onWebSocketClose();
    }

    public interface IWebSocketMessageListener {
        void onMessageReceive(ConcreteMessage message);
        void onMessageReceive(List<ConcreteMessage> messages, boolean isFinished);
        void onSyncNotify(long syncTime);
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
            case PBRcvObj.PBRcvType.parseError:
                break;
            case PBRcvObj.PBRcvType.connectAck:
                handleConnectAckMsg(obj.mConnectAck);
                break;
            case PBRcvObj.PBRcvType.publishMsgAck:
                handlePublishAckMsg(obj.mPublishMsgAck);
                break;
            case PBRcvObj.PBRcvType.qryHisMessagesAck:
                handleQryHisMsgAck(obj.mQryHisMsgAck);
                break;
            case PBRcvObj.PBRcvType.syncConversationsAck:
                handleSyncConversationAck(obj.mSyncConvAck);
                break;
            case PBRcvObj.PBRcvType.publishMsg:
                handleReceiveMessage(obj.mPublishMsgBody);
                break;
            case PBRcvObj.PBRcvType.publishMsgNtf:
                handlePublishMsgNtf(obj.mPublishMsgNtf);
                break;
            case PBRcvObj.PBRcvType.syncMessagesAck:
                handleSyncMsgAck(obj.mQryHisMsgAck);
                break;
            case PBRcvObj.PBRcvType.pong:
                handlePong();
                break;
            case PBRcvObj.PBRcvType.disconnectMsg:
                handleDisconnectMsg(obj.mDisconnectMsg);
                break;
        }
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        LoggerUtils.i("JWebSocket, onClose, code is " + code + " reason is " + reason);
        if (mConnectListener != null) {
            mConnectListener.onWebSocketClose();
        }
    }

    @Override
    public void onError(Exception ex) {
        LoggerUtils.i("JWebSocket, onError, msg is " +ex.getMessage());
        if (mConnectListener != null) {
            mConnectListener.onWebSocketFail();
        }
    }

    public void setToken(String token) {
        mToken = token;
    }

    public void setAppKey(String appKey) {
        mAppKey = appKey;
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
        sendWhenOpen(bytes);
    }

    private void sendDisconnectMsg(boolean receivePush) {
        byte[] bytes = mPbData.disconnectData(receivePush);
        sendWhenOpen(bytes);
    }

    private void sendPublishAck(int index) {
        byte[] bytes = mPbData.publishAckData(index);
        sendWhenOpen(bytes);
    }

    private void handleConnectAckMsg(@NonNull PBRcvObj.ConnectAck ack) {
        LoggerUtils.i("connect userId is " + ack.userId);
        if (mConnectListener != null) {
            mConnectListener.onConnectComplete(ack.code, ack.userId);
        }
    }

    private void handlePublishAckMsg(PBRcvObj.PublishMsgAck ack) {
        LoggerUtils.d("handlePublishAckMsg, msgId is " + ack.msgId + ", code is " + ack.code);
        IWebSocketCallback callback = mMsgCallbackMap.remove(ack.index);
        if (callback instanceof SendMessageCallback) {
            SendMessageCallback sCallback = (SendMessageCallback) callback;
            if (ack.code != 0) {
                sCallback.onError(ack.code, sCallback.getClientMsgNo());
            } else {
                sCallback.onSuccess(sCallback.getClientMsgNo(), ack.msgId, ack.timestamp, ack.msgIndex);
            }
        }
    }

    private void handleQryHisMsgAck(PBRcvObj.QryHisMsgAck ack) {
        LoggerUtils.d("handleQryHisMsgAck");
        IWebSocketCallback callback = mMsgCallbackMap.remove(ack.index);
        if (callback instanceof QryHisMsgCallback) {
            QryHisMsgCallback qCallback = (QryHisMsgCallback) callback;
            if (ack.code != 0) {
                qCallback.onError(ack.code);
            } else {
                qCallback.onSuccess(ack.msgList, ack.isFinished);
            }
        }
    }

    private void handleSyncConversationAck(PBRcvObj.SyncConvAck ack) {
        IWebSocketCallback callback = mMsgCallbackMap.remove(ack.index);
        if (callback instanceof SyncConversationsCallback) {
            SyncConversationsCallback syncConversationsCallback = (SyncConversationsCallback) callback;
            if (ack.code != 0) {
                syncConversationsCallback.onError(ack.code);
            } else {
                syncConversationsCallback.onSuccess(ack.convList, ack.isFinished);
            }
        }
    }

    private void handleSyncMsgAck(PBRcvObj.QryHisMsgAck ack) {
        if (mMessageListener != null) {
            mMessageListener.onMessageReceive(ack.msgList, ack.isFinished);
        }
    }

    private void handleReceiveMessage(PBRcvObj.PublishMsgBody body) {
        if (mMessageListener != null) {
            mMessageListener.onMessageReceive(body.rcvMessage);
        }
        if (body.qos == 1) {
            sendPublishAck(body.index);
        }
    }

    private void handlePublishMsgNtf(PBRcvObj.PublishMsgNtf ntf) {
        if (mMessageListener != null) {
            mMessageListener.onSyncNotify(ntf.syncTime);
        }
    }

    private void handlePong() {
        LoggerUtils.d("pong");
    }

    private void handleDisconnectMsg(PBRcvObj.DisconnectMsg msg) {
        if (mConnectListener != null) {
            mConnectListener.onDisconnect(msg.code);
        }
    }

    private void sendWhenOpen(byte[] bytes) {
        if (isOpen()) {
            send(bytes);
        }
    }

    private String mAppKey;
    private String mToken;
    private final PBData mPbData;
    private final Context mContext;
    private IWebSocketConnectListener mConnectListener;
    private IWebSocketMessageListener mMessageListener;
    private Integer mMsgIndex = 0;
    private final ConcurrentHashMap<Integer, IWebSocketCallback>mMsgCallbackMap;
    private static final String WEB_SOCKET_PREFIX = "ws://";
    private static final String WEB_SOCKET_SUFFIX = "/im";


}
