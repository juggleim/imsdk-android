package com.jet.im.internal.core.network;

import android.os.Build;
import android.os.Handler;

import androidx.annotation.NonNull;

import com.jet.im.JErrorCode;
import com.jet.im.JetIMConst;
import com.jet.im.internal.ConstInternal;
import com.jet.im.internal.model.ConcreteMessage;
import com.jet.im.internal.model.MergeInfo;
import com.jet.im.internal.model.upload.UploadFileType;
import com.jet.im.internal.util.JLogger;
import com.jet.im.model.Conversation;
import com.jet.im.model.MediaMessageContent;
import com.jet.im.model.MessageContent;
import com.jet.im.model.MessageMentionInfo;
import com.jet.im.push.PushChannel;

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
        mCompeteStatusList = new ArrayList<>();
    }

    public void connect(String appKey, String token, String deviceId, String packageName, String networkType, String carrier, PushChannel pushChannel, String pushToken, List<String> servers) {
        JLogger.i("WS-Connect", "appKey is " + appKey + ", token is " + token + ", servers is " + servers);
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
                mCompeteStatusList.add(WebSocketStatus.IDLE);
                executorService.execute(wsc::connect);
            }
        });
    }

    public void disconnect(Boolean receivePush) {
        JLogger.i("WS-Disconnect", "receivePush is " + receivePush);
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
                              MergeInfo mergeInfo,
                              MessageMentionInfo mentionInfo,
                              ConcreteMessage referMsg,
                              boolean isBroadcast,
                              String userId,
                              SendMessageCallback callback) {
        Integer key = mCmdIndex;
        byte[] encodeBytes;
        if (content instanceof MediaMessageContent) {
            MediaMessageContent mediaContent = (MediaMessageContent) content;
            String local = mediaContent.getLocalPath();
            mediaContent.setLocalPath("");
            encodeBytes = mediaContent.encode();
            mediaContent.setLocalPath(local);
        } else {
            encodeBytes = content.encode();
        }

        byte[] bytes = mPbData.sendMessageData(content.getContentType(),
                encodeBytes,
                content.getFlags(),
                clientUid,
                mergeInfo,
                isBroadcast,
                userId,
                mCmdIndex++,
                conversation.getConversationType(),
                conversation.getConversationId(),
                mentionInfo,
                referMsg);
        mWebSocketCommandManager.putCommand(key, callback);
        JLogger.i("WS-Send", "send message");
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
        JLogger.i("WS-Send", "recallMessage, messageId is " + messageId);
        sendWhenOpen(bytes);
    }

    public void syncConversations(long startTime,
                                  int count,
                                  String userId,
                                  SyncConversationsCallback callback) {
        Integer key = mCmdIndex;
        byte[] bytes = mPbData.syncConversationsData(startTime, count, userId, mCmdIndex++);
        mWebSocketCommandManager.putCommand(key, callback);
        JLogger.i("WS-Send", "syncConversations, startTime is " + startTime + ", count is " + count);
        sendWhenOpen(bytes);
    }

    public void syncMessages(long receiveTime,
                             long sendTime,
                             String userId) {
        byte[] bytes = mPbData.syncMessagesData(receiveTime, sendTime, userId, mCmdIndex++);
        JLogger.i("WS-Send", "syncMessages, receiveTime is " + receiveTime + ", sendTime is " + sendTime);
        sendWhenOpen(bytes);
    }

    public void sendReadReceipt(Conversation conversation,
                                List<String> messageIds,
                                WebSocketTimestampCallback callback) {
        Integer key = mCmdIndex;
        byte[] bytes = mPbData.sendReadReceiptData(conversation, messageIds, mCmdIndex++);
        mWebSocketCommandManager.putCommand(key, callback);
        JLogger.i("WS-Send", "sendReadReceipt");
        sendWhenOpen(bytes);
    }

    public void getGroupMessageReadDetail(Conversation conversation,
                                          String messageId,
                                          QryReadDetailCallback callback) {
        Integer key = mCmdIndex;
        byte[] bytes = mPbData.getGroupMessageReadDetail(conversation, messageId, mCmdIndex++);
        mWebSocketCommandManager.putCommand(key, callback);
        JLogger.i("WS-Send", "getGroupMessageReadDetail, messageId is " + messageId);
        sendWhenOpen(bytes);
    }

    public void deleteConversationInfo(Conversation conversation,
                                       String userId,
                                       WebSocketTimestampCallback callback) {
        Integer key = mCmdIndex;
        byte[] bytes = mPbData.deleteConversationData(conversation, userId, mCmdIndex++);
        mWebSocketCommandManager.putCommand(key, callback);
        JLogger.i("WS-Send", "getGroupMessageReadDetail, conversation is " + conversation);
        sendWhenOpen(bytes);
    }

    public void clearUnreadCount(Conversation conversation,
                                 String userId,
                                 long msgIndex,
                                 WebSocketTimestampCallback callback) {
        Integer key = mCmdIndex;
        byte[] bytes = mPbData.clearUnreadCountData(conversation, userId, msgIndex, mCmdIndex++);
        mWebSocketCommandManager.putCommand(key, callback);
        JLogger.i("WS-Send", "clearUnreadCount, conversation is " + conversation + ", msgIndex is " + msgIndex);
        sendWhenOpen(bytes);
    }

    public void clearTotalUnreadCount(String userId, long time, WebSocketTimestampCallback callback) {
        Integer key = mCmdIndex;
        byte[] bytes = mPbData.clearTotalUnreadCountData(userId, time, mCmdIndex++);
        mWebSocketCommandManager.putCommand(key, callback);
        JLogger.i("WS-Send", "clearTotalUnreadCount, time is " + time);
        sendWhenOpen(bytes);
    }

    public void addConversationInfo(Conversation conversation, String userId, AddConversationCallback callback) {
        Integer key = mCmdIndex;
        byte[] bytes = mPbData.addConversationInfo(conversation, userId, mCmdIndex++);
        mWebSocketCommandManager.putCommand(key, callback);
        JLogger.i("WS-Send", "addConversationInfo, conversation is " + conversation);
        sendWhenOpen(bytes);
    }

    public void queryHisMsg(Conversation conversation, long startTime, int count, JetIMConst.PullDirection direction, QryHisMsgCallback callback) {
        Integer key = mCmdIndex;
        byte[] bytes = mPbData.queryHisMsgData(conversation, startTime, count, direction, mCmdIndex++);
        mWebSocketCommandManager.putCommand(key, callback);
        JLogger.i("WS-Send", "queryHisMsg, conversation is " + conversation + ", startTime is " + startTime + ", count is " + count + ", direction is " + direction);
        sendWhenOpen(bytes);
    }

    public void queryHisMsgByIds(Conversation conversation, List<String> messageIds, QryHisMsgCallback callback) {
        Integer key = mCmdIndex;
        byte[] bytes = mPbData.queryHisMsgDataByIds(conversation, messageIds, mCmdIndex++);
        mWebSocketCommandManager.putCommand(key, callback);
        JLogger.i("WS-Send", "queryHisMsgByIds, conversation is " + conversation);
        sendWhenOpen(bytes);
    }

    public void setMute(Conversation conversation, boolean isMute, String userId, WebSocketTimestampCallback callback) {
        Integer key = mCmdIndex;
        byte[] bytes = mPbData.disturbData(conversation, userId, isMute, mCmdIndex++);
        mWebSocketCommandManager.putCommand(key, callback);
        JLogger.i("WS-Send", "setMute, conversation is " + conversation + ", isMute is " + isMute);
        sendWhenOpen(bytes);
    }

    public void setTop(Conversation conversation, boolean isTop, String userId, WebSocketTimestampCallback callback) {
        Integer key = mCmdIndex;
        byte[] bytes = mPbData.topConversationData(conversation, userId, isTop, mCmdIndex++);
        mWebSocketCommandManager.putCommand(key, callback);
        JLogger.i("WS-Send", "setTop, conversation is " + conversation + ", isTop is " + isTop);
        sendWhenOpen(bytes);
    }

    public void getMergedMessageList(String containerMsgId,
                                     long timestamp,
                                     int count,
                                     JetIMConst.PullDirection direction,
                                     QryHisMsgCallback callback) {
        Integer key = mCmdIndex;
        byte[] bytes = mPbData.getMergedMessageList(containerMsgId, timestamp, count, direction, mCmdIndex++);
        mWebSocketCommandManager.putCommand(key, callback);
        JLogger.i("WS-Send", "getMergedMessageList, containerMsgId is " + containerMsgId + ", timestamp is " + timestamp + ", count is " + count + ", direction is " + direction);
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
        JLogger.i("WS-Send", "getMentionMessageList, conversation is " + conversation + ", time is " + time + ", count is " + count + ", direction is " + direction);
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
        JLogger.i("WS-Send", "registerPushToken, channel is " + channel.getName() + ", token is " + token);
        sendWhenOpen(bytes);
    }

    public void clearHistoryMessage(Conversation conversation, long time, WebSocketTimestampCallback callback) {
        Integer key = mCmdIndex;
        byte[] bytes = mPbData.clearHistoryMessage(conversation, time, 0, mCmdIndex++);
        mWebSocketCommandManager.putCommand(key, callback);
        JLogger.i("WS-Send", "clearHistoryMessage, conversation is " + conversation + ", time is " + time);
        sendWhenOpen(bytes);
    }

    public void deleteMessage(Conversation conversation, List<ConcreteMessage> msgList, WebSocketTimestampCallback callback) {
        Integer key = mCmdIndex;
        byte[] bytes = mPbData.deleteMessage(conversation, msgList, mCmdIndex++);
        mWebSocketCommandManager.putCommand(key, callback);
        JLogger.i("WS-Send", "deleteMessage, conversation is " + conversation);
        sendWhenOpen(bytes);
    }

    public void getUploadFileCred(String userId, UploadFileType fileType, String ext, QryUploadFileCredCallback callback) {
        Integer key = mCmdIndex;
        byte[] bytes = mPbData.getUploadFileCred(userId, fileType, ext, mCmdIndex++);
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
        JLogger.v("WS-Send", "ping");
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
        } else if (callback instanceof QryUploadFileCredCallback) {
            QryUploadFileCredCallback sCallback = (QryUploadFileCredCallback) callback;
            sCallback.onError(errorCode);
        } else if (callback instanceof AddConversationCallback) {
            AddConversationCallback sCallback = (AddConversationCallback) callback;
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
        boolean onMessageReceive(ConcreteMessage message);

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
            for (int i = 0; i < mCompeteWSCList.size(); i++) {
                JWebSocketClient wsc = mCompeteWSCList.get(i);
                if (wsc == client) {
                    JLogger.i("WS-Connect", "onOpen");
                    mIsCompeteFinish = true;
                    mCompeteStatusList.set(i, WebSocketStatus.SUCCESS);
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
    }

    @Override
    public void onMessage(JWebSocketClient client, ByteBuffer bytes) {
        if (client != mWebSocketClient) {
            return;
        }
        mHeartbeatManager.updateLastMessageReceivedTime();
        PBRcvObj obj = mPbData.rcvObjWithBytes(bytes);
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
            case PBRcvObj.PBRcvType.qryFileCredAck:
                handleUploadFileCredCallback(obj.mQryFileCredAck);
                break;
            case PBRcvObj.PBRcvType.addConversationAck:
                handleAddConversationAck(obj.mConversationInfoAck);
                break;
            default:
                JLogger.i("WS-Receive", "default, type is " + obj.getRcvType());
                break;
        }
    }

    @Override
    public void onClose(JWebSocketClient client, int code, String reason, boolean remote) {
        mSendHandler.post(() -> {
            if (mIsCompeteFinish) {
                if (client != mWebSocketClient) {
                    return;
                }
                JLogger.i("WS-Connect", "onClose, code is " + code + ", reason is " + reason + ", isRemote " + remote);
                resetWebSocketClient();
                if (remote && mConnectListener != null) {
                    mConnectListener.onWebSocketClose();
                }
            } else {
                for (int i = 0; i < mCompeteWSCList.size(); i++) {
                    JWebSocketClient wsc = mCompeteWSCList.get(i);
                    if (wsc == client) {
                        mCompeteStatusList.set(i, WebSocketStatus.FAILURE);
                        break;
                    }
                }
                boolean allFailed = true;
                for (WebSocketStatus status : mCompeteStatusList) {
                    if (WebSocketStatus.FAILURE != status) {
                        allFailed = false;
                        break;
                    }
                }
                if (allFailed && mConnectListener != null) {
                    JLogger.i("WS-Connect", "onClose, code is " + code + ", reason is " + reason + ", isRemote " + remote);
                    resetWebSocketClient();
                    mConnectListener.onWebSocketClose();
                }
            }
        });
    }

    @Override
    public void onError(JWebSocketClient client, Exception ex) {
        if (client != mWebSocketClient) {
            return;
        }
        JLogger.e("WS-Connect", "onError, msg is " + ex.getMessage());
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
        JLogger.v("WS-Send", "publish ack");
        byte[] bytes = mPbData.publishAckData(index);
        sendWhenOpen(bytes);
    }

    private void handleConnectAckMsg(@NonNull PBRcvObj.ConnectAck ack) {
        JLogger.i("WS-Receive", "handleConnectAckMsg, connect userId is " + ack.userId);
        if (mConnectListener != null) {
            mConnectListener.onConnectComplete(ack.code, ack.userId, ack.session, ack.extra);
        }
    }

    private void handlePublishAckMsg(PBRcvObj.PublishMsgAck ack) {
        JLogger.i("WS-Receive", "handlePublishAckMsg, msgId is " + ack.msgId + ", code is " + ack.code);
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
        JLogger.i("WS-Receive", "handleQryHisMsgAck");
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
        JLogger.i("WS-Receive", "handleSyncConversationAck");
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
        JLogger.i("WS-Receive", "handleSyncMsgAck");
        if (mMessageListener != null) {
            mMessageListener.onMessageReceive(ack.msgList, ack.isFinished);
        }
    }

    private void handleReceiveMessage(PBRcvObj.PublishMsgBody body) {
        JLogger.i("WS-Receive", "handleReceiveMessage");
        boolean needAck = false;
        if (mMessageListener != null) {
            needAck = mMessageListener.onMessageReceive(body.rcvMessage);
        }
        if (body.qos == 1 && needAck) {
            sendPublishAck(body.index);
        }
    }

    private void handlePublishMsgNtf(PBRcvObj.PublishMsgNtf ntf) {
        JLogger.i("WS-Receive", "handlePublishMsgNtf");
        if (mMessageListener != null) {
            mMessageListener.onSyncNotify(ntf.syncTime);
        }
    }

    private void handlePong() {
        JLogger.v("WS-Receive", "handlePong");
    }

    private void handleDisconnectMsg(PBRcvObj.DisconnectMsg msg) {
        JLogger.i("WS-Receive", "handleDisconnectMsg");
        mSendHandler.post(this::resetWebSocketClient);
        if (mConnectListener != null) {
            mConnectListener.onDisconnect(msg.code, msg.extra);
        }
    }

    private void handleSimpleQryAck(PBRcvObj.SimpleQryAck ack) {
        JLogger.i("WS-Receive", "handleSimpleQryAck, code is " + ack.code);
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
        JLogger.i("WS-Receive", "handleSimpleQryAckWithTimeCallback, code is " + ack.code);
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

    private void handleTimestampCallback(PBRcvObj.TimestampQryAck ack) {
        JLogger.i("WS-Receive", "handleTimestampAck, code is " + ack.code);
        IWebSocketCallback c = mWebSocketCommandManager.removeCommand(ack.index);
        if (c == null) return;
        if (c instanceof WebSocketTimestampCallback) {
            WebSocketTimestampCallback callback = (WebSocketTimestampCallback) c;
            if (ack.code != 0) {
                callback.onError(ack.code);
            } else {
                callback.onSuccess(ack.operationTime);
            }
        }
    }

    private void handleQryReadDetailAck(PBRcvObj.QryReadDetailAck ack) {
        JLogger.i("WS-Receive", "handleQryReadDetailAck, code is " + ack.code);
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

    private void handleUploadFileCredCallback(PBRcvObj.QryFileCredAck ack) {
        JLogger.i("WS-Receive", "handleUploadFileCredCallback, code is " + ack.code);
        IWebSocketCallback c = mWebSocketCommandManager.removeCommand(ack.index);
        if (c == null) return;
        if (c instanceof QryUploadFileCredCallback) {
            QryUploadFileCredCallback callback = (QryUploadFileCredCallback) c;
            if (ack.code != 0) {
                callback.onError(ack.code);
            } else {
                callback.onSuccess(ack.ossType, ack.qiNiuCred, ack.preSignCred);
            }
        }
    }

    private void handleAddConversationAck(PBRcvObj.ConversationInfoAck ack) {
        JLogger.i("WS-Receive", "handleAddConversationAck, code is " + ack.code);
        IWebSocketCallback c = mWebSocketCommandManager.removeCommand(ack.index);
        if (c == null) return;
        if (c instanceof AddConversationCallback) {
            AddConversationCallback callback = (AddConversationCallback) c;
            if (ack.code != 0) {
                callback.onError(ack.code);
            } else {
                callback.onSuccess(ack.timestamp, ack.conversationInfo);
            }
        }
    }

    private void sendWhenOpen(byte[] bytes) {
        mSendHandler.post(() -> {
            if (mWebSocketClient != null && mWebSocketClient.isOpen()) {
                mWebSocketClient.send(bytes);
                return;
            }
            JLogger.e("WS-Send", mWebSocketClient == null ? "mWebSocketClient is null" : "mWebSocketClient is not open");
            pushRemainCmdAndCallbackError();
            mConnectListener.onWebSocketClose();
        });
    }

    private void resetWebSocketClient() {
        mWebSocketClient = null;
        mCompeteWSCList.clear();
        mCompeteStatusList.clear();
        mIsCompeteFinish = false;
    }

    private URI createWebSocketUri(String server) {
        String webSocketUrl;
        if (server.contains(PROTOCOL_HEAD)) {
            webSocketUrl = server + WEB_SOCKET_SUFFIX;
        } else {
            webSocketUrl = WS_HEAD_PREFIX + server + WEB_SOCKET_SUFFIX;
        }
        return URI.create(webSocketUrl);
    }

    private enum WebSocketStatus {IDLE, FAILURE, SUCCESS}

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
    private final List<WebSocketStatus> mCompeteStatusList;
    private final Handler mSendHandler;
    private static final String PROTOCOL_HEAD = "://";
    private static final String WS_HEAD_PREFIX = "ws://";
    private static final String WSS_HEAD_PREFIX = "wss://";
    private static final String WEB_SOCKET_SUFFIX = "/im";
    private static final int MAX_CONCURRENT_COUNT = 5;
}
