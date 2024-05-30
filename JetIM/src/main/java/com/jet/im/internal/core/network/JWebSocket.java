package com.jet.im.internal.core.network;

import android.os.Build;
import android.os.Handler;

import androidx.annotation.NonNull;

import com.jet.im.JErrorCode;
import com.jet.im.JetIMConst;
import com.jet.im.internal.ConstInternal;
import com.jet.im.internal.model.ConcreteMessage;
import com.jet.im.model.Conversation;
import com.jet.im.model.MessageContent;
import com.jet.im.push.PushChannel;
import com.jet.im.internal.util.JLogger;

import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class JWebSocket implements WebSocketCommandManager.CommandTimeoutListener, JWebSocketClient.IWebSocketClientListener {
    public JWebSocket(Handler sendHandler) {
        mSendHandler = sendHandler;
        mPbData = new PBData();
        mHeartbeatManager = new HeartbeatManager(this);
        mWebSocketCommandManager = new WebSocketCommandManager(this);
        mWebSocketCommandManager.start(false);
        mCompeteWSCList = new ArrayList<>();
    }

    public void connect(String appKey, String token, String deviceId, String packageName, String networkType, String carrier, PushChannel pushChannel, String pushToken, List<String> servers) {
        mSendHandler.post(() -> {
            mAppKey = appKey;
            mToken = token;
            mDeviceId = deviceId;
            mPackageName = packageName;
            mPushChannel = pushChannel;
            mPushToken = pushToken;
            mNetworkType = networkType;
            mCarrier = carrier;

            resetWebSocketClient();
            ExecutorService executorService = Executors.newFixedThreadPool(MAX_CONCURRENT_COUNT);

            for (String server : servers) {
                URI uri = createWebSocketUri(server);
                JWebSocketClient wsc = new JWebSocketClient(uri, JWebSocket.this);
                mCompeteWSCList.add(wsc);
                executorService.execute(wsc::connect);
            }
        });
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
                              List<ConcreteMessage> mergedMsgList,
                              boolean isBroadcast,
                              String userId,
                              SendMessageCallback callback) {
        Integer key = mCmdIndex;
        byte[] bytes = mPbData.sendMessageData(content.getContentType(),
                content.encode(),
                content.getFlags(),
                clientUid,
                mergedMsgList,
                isBroadcast,
                userId,
                mCmdIndex++,
                conversation.getConversationType(),
                conversation.getConversationId(),
                content.getMentionInfo());
        mWebSocketCommandManager.putCommand(key, callback);
        sendWhenOpen(bytes);
    }

    public void recallMessage(String messageId,
                              Conversation conversation,
                              long timestamp,
                              Map<String, String> extras,
                              WebSocketTimestampCallback callback) {
        Integer key = mCmdIndex;
        byte[] bytes = mPbData.recallMessageData(messageId, conversation, timestamp, extras, mCmdIndex++);
        mWebSocketCommandManager.putCommand(key, callback);
        sendWhenOpen(bytes);
    }

    public void syncConversations(long startTime,
                                  int count,
                                  String userId,
                                  SyncConversationsCallback callback) {
        Integer key = mCmdIndex;
        byte[] bytes = mPbData.syncConversationsData(startTime, count, userId, mCmdIndex++);
        mWebSocketCommandManager.putCommand(key, callback);
        sendWhenOpen(bytes);
    }

    public void syncMessages(long receiveTime,
                             long sendTime,
                             String userId) {
        byte[] bytes = mPbData.syncMessagesData(receiveTime, sendTime, userId, mCmdIndex++);
        sendWhenOpen(bytes);
    }

    public void sendReadReceipt(Conversation conversation,
                                List<String> messageIds,
                                WebSocketSimpleCallback callback) {
        Integer key = mCmdIndex;
        byte[] bytes = mPbData.sendReadReceiptData(conversation, messageIds, mCmdIndex++);
        mWebSocketCommandManager.putCommand(key, callback);
        sendWhenOpen(bytes);
    }

    public void getGroupMessageReadDetail(Conversation conversation,
                                          String messageId,
                                          QryReadDetailCallback callback) {
        Integer key = mCmdIndex;
        byte[] bytes = mPbData.getGroupMessageReadDetail(conversation, messageId, mCmdIndex++);
        mWebSocketCommandManager.putCommand(key, callback);
        sendWhenOpen(bytes);
    }

    public void deleteConversationInfo(Conversation conversation,
                                       String userId,
                                       WebSocketSimpleCallback callback) {
        Integer key = mCmdIndex;
        byte[] bytes = mPbData.deleteConversationData(conversation, userId, mCmdIndex++);
        mWebSocketCommandManager.putCommand(key, callback);
        sendWhenOpen(bytes);
    }

    public void clearUnreadCount(Conversation conversation,
                                 String userId,
                                 long msgIndex,
                                 WebSocketSimpleCallback callback) {
        Integer key = mCmdIndex;
        byte[] bytes = mPbData.clearUnreadCountData(conversation, userId, msgIndex, mCmdIndex++);
        mWebSocketCommandManager.putCommand(key, callback);
        sendWhenOpen(bytes);
    }

    public void clearTotalUnreadCount(String userId, long time, WebSocketSimpleCallback callback) {
        Integer key = mCmdIndex;
        byte[] bytes = mPbData.clearTotalUnreadCountData(userId, time, mCmdIndex++);
        mWebSocketCommandManager.putCommand(key, callback);
        sendWhenOpen(bytes);
    }

    public void queryHisMsg(Conversation conversation, long startTime, int count, JetIMConst.PullDirection direction, QryHisMsgCallback callback) {
        Integer key = mCmdIndex;
        byte[] bytes = mPbData.queryHisMsgData(conversation, startTime, count, direction, mCmdIndex++);
        mWebSocketCommandManager.putCommand(key, callback);
        sendWhenOpen(bytes);
    }

    public void queryHisMsgByIds(Conversation conversation, List<String> messageIds, QryHisMsgCallback callback) {
        Integer key = mCmdIndex;
        byte[] bytes = mPbData.queryHisMsgDataByIds(conversation, messageIds, mCmdIndex++);
        mWebSocketCommandManager.putCommand(key, callback);
        sendWhenOpen(bytes);
    }

    public void setMute(Conversation conversation, boolean isMute, String userId, WebSocketSimpleCallback callback) {
        Integer key = mCmdIndex;
        byte[] bytes = mPbData.disturbData(conversation, userId, isMute, mCmdIndex++);
        mWebSocketCommandManager.putCommand(key, callback);
        sendWhenOpen(bytes);
    }

    public void setTop(Conversation conversation, boolean isTop, String userId, WebSocketTimestampCallback callback) {
        Integer key = mCmdIndex;
        byte[] bytes = mPbData.topConversationData(conversation, userId, isTop, mCmdIndex++);
        mWebSocketCommandManager.putCommand(key, callback);
        sendWhenOpen(bytes);
    }

    public void getMergedMessageList(String messageId,
                                     long timestamp,
                                     int count,
                                     JetIMConst.PullDirection direction,
                                     QryHisMsgCallback callback) {
        Integer key = mCmdIndex;
        byte[] bytes = mPbData.getMergedMessageList(messageId, timestamp, count, direction, mCmdIndex++);
        mWebSocketCommandManager.putCommand(key, callback);
        sendWhenOpen(bytes);
    }

    public void getMentionMessageList(Conversation conversation,
                                      long time,
                                      int count,
                                      JetIMConst.PullDirection direction,
                                      QryHisMsgCallback callback) {
        Integer key = mCmdIndex;
        byte[] bytes = mPbData.getMentionMessages(conversation, time, count, direction, mCmdIndex++);
        mWebSocketCommandManager.putCommand(key, callback);
        sendWhenOpen(bytes);
    }

    public void registerPushToken(PushChannel channel, String token, String deviceId, String packageName, String userId, WebSocketSimpleCallback callback) {
        Integer key = mCmdIndex;
        byte[] bytes = mPbData.registerPushToken(channel,
                token,
                deviceId,
                packageName,
                userId,
                mCmdIndex++);
        mWebSocketCommandManager.putCommand(key, callback);
        sendWhenOpen(bytes);
    }

    public void clearHistoryMessage(Conversation conversation, long time, WebSocketSimpleCallback callback) {
        Integer key = mCmdIndex;
        byte[] bytes = mPbData.clearHistoryMessage(conversation, time, 0, mCmdIndex++);
        mWebSocketCommandManager.putCommand(key, callback);
        sendWhenOpen(bytes);
    }

    public void deleteMessage(Conversation conversation, List<ConcreteMessage> msgList, WebSocketSimpleCallback callback) {
        Integer key = mCmdIndex;
        byte[] bytes = mPbData.deleteMessage(conversation, msgList, mCmdIndex++);
        mWebSocketCommandManager.putCommand(key, callback);
        sendWhenOpen(bytes);
    }

    public void startHeartbeat() {
        mHeartbeatManager.start(false);
    }

    public void stopHeartbeat() {
        mHeartbeatManager.stop();
    }

    public void handleHeartbeatTimeout() {
        if (mConnectListener != null) {
            mConnectListener.onTimeOut();
        }
    }

    public synchronized void pushRemainCmdAndCallbackError() {
        ArrayList<IWebSocketCallback> errorList = mWebSocketCommandManager.clearCommand();
        for (int i = 0; i < errorList.size(); i++) {
            onCommandError(errorList.get(i), JErrorCode.CONNECTION_UNAVAILABLE);
        }
    }

    public void ping() {
        byte[] bytes = mPbData.pingData();
        sendWhenOpen(bytes);
    }

    @Override
    public void onCommandTimeOut(IWebSocketCallback callback) {
        onCommandError(callback, JErrorCode.OPERATION_TIMEOUT);
    }

    private void onCommandError(IWebSocketCallback callback, int errorCode) {
        if (callback == null) return;
        if (callback instanceof SendMessageCallback) {
            SendMessageCallback sCallback = (SendMessageCallback) callback;
            sCallback.onError(errorCode, sCallback.getClientMsgNo());
        } else if (callback instanceof QryHisMsgCallback) {
            QryHisMsgCallback sCallback = (QryHisMsgCallback) callback;
            sCallback.onError(errorCode);
        } else if (callback instanceof SyncConversationsCallback) {
            SyncConversationsCallback sCallback = (SyncConversationsCallback) callback;
            sCallback.onError(errorCode);
        } else if (callback instanceof QryReadDetailCallback) {
            QryReadDetailCallback sCallback = (QryReadDetailCallback) callback;
            sCallback.onError(errorCode);
        } else if (callback instanceof WebSocketSimpleCallback) {
            WebSocketSimpleCallback sCallback = (WebSocketSimpleCallback) callback;
            sCallback.onError(errorCode);
        } else if (callback instanceof WebSocketTimestampCallback) {
            WebSocketTimestampCallback sCallback = (WebSocketTimestampCallback) callback;
            sCallback.onError(errorCode);
        }
    }

    public interface IWebSocketConnectListener {
        void onConnectComplete(int errorCode, String userId, String session, String extra);

        void onDisconnect(int errorCode, String extra);

        void onWebSocketFail();

        void onWebSocketClose();

        void onTimeOut();
    }

    public interface IWebSocketMessageListener {
        void onMessageReceive(ConcreteMessage message);

        void onMessageReceive(List<ConcreteMessage> messages, boolean isFinished);

        void onSyncNotify(long syncTime);
    }

    @Override
    public void onOpen(JWebSocketClient client, ServerHandshake handshakedata) {
        mSendHandler.post(() -> {
            if (mIsCompeteFinish) {
                client.close();
                return;
            }
            for (JWebSocketClient wsc : mCompeteWSCList) {
                if (wsc == client) {
                    JLogger.i("JWebSocket, onOpen");
                    mIsCompeteFinish = true;
                    mWebSocketClient = client;
                    sendConnectMsg();
                    break;
                }
            }
        });
    }

    @Override
    public void onMessage(JWebSocketClient client, String message) {
        if (client != mWebSocketClient) {
            return;
        }
        mHeartbeatManager.updateLastMessageReceivedTime();
        JLogger.i("JWebSocket, onMessage");
    }

    @Override
    public void onMessage(JWebSocketClient client, ByteBuffer bytes) {
        if (client != mWebSocketClient) {
            return;
        }
        mHeartbeatManager.updateLastMessageReceivedTime();
        PBRcvObj obj = mPbData.rcvObjWithBytes(bytes);
        JLogger.i("JWebSocket, onMessage bytes, type is " + obj.getRcvType());
        switch (obj.getRcvType()) {
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
            case PBRcvObj.PBRcvType.qryReadDetailAck:
                handleQryReadDetailAck(obj.mQryReadDetailAck);
                break;
            case PBRcvObj.PBRcvType.simpleQryAck:
                handleSimpleQryAck(obj.mSimpleQryAck);
                break;
            case PBRcvObj.PBRcvType.simpleQryAckCallbackTimestamp:
                handleSimpleQryAckWithTimeCallback(obj.mSimpleQryAck);
                break;
            default:
                break;
        }
    }

    @Override
    public void onClose(JWebSocketClient client, int code, String reason, boolean remote) {
        if (client != mWebSocketClient) {
            return;
        }
        JLogger.i("JWebSocket, onClose, code is " + code + ", reason is " + reason + ", isRemote " + remote);
        mSendHandler.post(this::resetWebSocketClient);
        if (remote && mConnectListener != null) {
            mConnectListener.onWebSocketClose();
        }
    }

    @Override
    public void onError(JWebSocketClient client, Exception ex) {
        if (client != mWebSocketClient) {
            return;
        }
        JLogger.i("JWebSocket, onError, msg is " + ex.getMessage());
        mSendHandler.post(this::resetWebSocketClient);
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
                mDeviceId,
                ConstInternal.PLATFORM,
                Build.BRAND,
                Build.MODEL,
                Build.VERSION.RELEASE,
                mPackageName,
                mPushChannel,
                mPushToken,
                mNetworkType,
                mCarrier,
                "");
        sendWhenOpen(bytes);
    }

    private void sendDisconnectMsg(boolean receivePush) {
        byte[] bytes = mPbData.disconnectData(receivePush);
        sendWhenOpen(bytes);
        mSendHandler.post(this::resetWebSocketClient);
    }

    private void sendPublishAck(int index) {
        byte[] bytes = mPbData.publishAckData(index);
        sendWhenOpen(bytes);
    }

    private void handleConnectAckMsg(@NonNull PBRcvObj.ConnectAck ack) {
        JLogger.i("connect userId is " + ack.userId);
        if (mConnectListener != null) {
            mConnectListener.onConnectComplete(ack.code, ack.userId, ack.session, ack.extra);
        }
    }

    private void handlePublishAckMsg(PBRcvObj.PublishMsgAck ack) {
        JLogger.d("handlePublishAckMsg, msgId is " + ack.msgId + ", code is " + ack.code);
        IWebSocketCallback c = mWebSocketCommandManager.removeCommand(ack.index);
        if (c == null) return;
        if (c instanceof SendMessageCallback) {
            SendMessageCallback callback = (SendMessageCallback) c;
            if (ack.code != 0) {
                callback.onError(ack.code, callback.getClientMsgNo());
            } else {
                callback.onSuccess(callback.getClientMsgNo(), ack.msgId, ack.timestamp, ack.seqNo);
            }
        }
    }

    private void handleQryHisMsgAck(PBRcvObj.QryHisMsgAck ack) {
        JLogger.d("handleQryHisMsgAck");
        IWebSocketCallback c = mWebSocketCommandManager.removeCommand(ack.index);
        if (c == null) return;
        if (c instanceof QryHisMsgCallback) {
            QryHisMsgCallback callback = (QryHisMsgCallback) c;
            if (ack.code != 0) {
                callback.onError(ack.code);
            } else {
                callback.onSuccess(ack.msgList, ack.isFinished);
            }
        }
    }

    private void handleSyncConversationAck(PBRcvObj.SyncConvAck ack) {
        JLogger.d("handleSyncConversationAck");
        IWebSocketCallback c = mWebSocketCommandManager.removeCommand(ack.index);
        if (c == null) return;
        if (c instanceof SyncConversationsCallback) {
            SyncConversationsCallback callback = (SyncConversationsCallback) c;
            if (ack.code != 0) {
                callback.onError(ack.code);
            } else {
                callback.onSuccess(ack.convList, ack.deletedConvList, ack.isFinished);
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
        JLogger.d("pong, mMsgCallbackMap count is " + mWebSocketCommandManager.size());
    }

    private void handleDisconnectMsg(PBRcvObj.DisconnectMsg msg) {
        mSendHandler.post(this::resetWebSocketClient);
        if (mConnectListener != null) {
            mConnectListener.onDisconnect(msg.code, msg.extra);
        }
    }

    private void handleSimpleQryAck(PBRcvObj.SimpleQryAck ack) {
        JLogger.d("handleSimpleQryAck, code is " + ack.code);
        IWebSocketCallback c = mWebSocketCommandManager.removeCommand(ack.index);
        if (c == null) return;
        if (c instanceof WebSocketSimpleCallback) {
            WebSocketSimpleCallback callback = (WebSocketSimpleCallback) c;
            if (ack.code != 0) {
                callback.onError(ack.code);
            } else {
                callback.onSuccess();
            }
        }
    }

    private void handleSimpleQryAckWithTimeCallback(PBRcvObj.SimpleQryAck ack) {
        JLogger.d("handleSimpleQryAckWithTimeCallback, code is " + ack.code);
        IWebSocketCallback c = mWebSocketCommandManager.removeCommand(ack.index);
        if (c == null) return;
        if (c instanceof WebSocketTimestampCallback) {
            WebSocketTimestampCallback callback = (WebSocketTimestampCallback) c;
            if (ack.code != 0) {
                callback.onError(ack.code);
            } else {
                callback.onSuccess(ack.timestamp);
            }
        }
    }

    private void handleQryReadDetailAck(PBRcvObj.QryReadDetailAck ack) {
        JLogger.d("handleQryReadDetailAck, code is " + ack.code);
        IWebSocketCallback c = mWebSocketCommandManager.removeCommand(ack.index);
        if (c == null) return;
        if (c instanceof QryReadDetailCallback) {
            QryReadDetailCallback callback = (QryReadDetailCallback) c;
            if (ack.code != 0) {
                callback.onError(ack.code);
            } else {
                callback.onSuccess(ack.readMembers, ack.unreadMembers);
            }
        }
    }

    private void sendWhenOpen(byte[] bytes) {
        mSendHandler.post(() -> {
            if (mWebSocketClient != null) {
                mWebSocketClient.send(bytes);
            }
        });
    }

    private void resetWebSocketClient() {
        mWebSocketClient = null;
        mCompeteWSCList.clear();
        mIsCompeteFinish = false;
    }

    private URI createWebSocketUri(String server) {
        String webSocketUrl = WEB_SOCKET_PREFIX + server + WEB_SOCKET_SUFFIX;
        return URI.create(webSocketUrl);
    }

    private String mAppKey;
    private String mToken;
    private String mDeviceId;
    private String mPackageName;
    private String mNetworkType;
    private String mCarrier;
    private PushChannel mPushChannel;
    private String mPushToken;
    private final PBData mPbData;

    private final WebSocketCommandManager mWebSocketCommandManager;
    private final HeartbeatManager mHeartbeatManager;
    private IWebSocketConnectListener mConnectListener;
    private IWebSocketMessageListener mMessageListener;
    private Integer mCmdIndex = 0;
    private JWebSocketClient mWebSocketClient;
    private boolean mIsCompeteFinish;
    private final List<JWebSocketClient> mCompeteWSCList;
    private final Handler mSendHandler;
    private static final String WEB_SOCKET_PREFIX = "ws://";
    private static final String WEB_SOCKET_SUFFIX = "/im";
    private static final int MAX_CONCURRENT_COUNT = 5;
}
