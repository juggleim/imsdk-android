package com.juggle.im.internal.core.network;

import android.os.Build;
import android.os.Handler;

import androidx.annotation.NonNull;

import com.juggle.im.JErrorCode;
import com.juggle.im.JIMConst;
import com.juggle.im.call.CallConst;
import com.juggle.im.call.internal.model.RtcRoom;
import com.juggle.im.call.model.CallMember;
import com.juggle.im.internal.ConstInternal;
import com.juggle.im.internal.model.ChatroomAttributeItem;
import com.juggle.im.internal.model.ConcreteMessage;
import com.juggle.im.internal.model.MergeInfo;
import com.juggle.im.internal.model.upload.UploadFileType;
import com.juggle.im.internal.util.JLogger;
import com.juggle.im.model.Conversation;
import com.juggle.im.model.MediaMessageContent;
import com.juggle.im.model.MessageContent;
import com.juggle.im.model.MessageMentionInfo;
import com.juggle.im.model.PushData;
import com.juggle.im.model.TimePeriod;
import com.juggle.im.model.UserInfo;
import com.juggle.im.model.messages.ImageMessage;
import com.juggle.im.model.messages.UnknownMessage;
import com.juggle.im.model.messages.VideoMessage;
import com.juggle.im.push.PushChannel;

import org.java_websocket.exceptions.WebsocketNotConnectedException;
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

    public void connect(String appKey, String token, String deviceId, String packageName, String networkType, String carrier, PushChannel pushChannel, String pushToken, String language, List<String> servers) {
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
            mLanguage = language;

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

    public void setChatroomListener(IWebSocketChatroomListener listener) {
        mChatroomListener = listener;
    }

    public void setCallListener(IWebSocketCallListener listener) {
        mCallListener = listener;
    }

    public void sendIMMessage(MessageContent content,
                              Conversation conversation,
                              String clientUid,
                              MergeInfo mergeInfo,
                              MessageMentionInfo mentionInfo,
                              ConcreteMessage referMsg,
                              PushData pushData,
                              boolean isBroadcast,
                              String userId,
                              SendMessageCallback callback) {
        Integer key = mCmdIndex;
        byte[] encodeBytes = encodeContentData(content);
        String contentType;
        if (content instanceof UnknownMessage) {
            UnknownMessage unknown = (UnknownMessage) content;
            contentType = unknown.getMessageType();
        } else {
            contentType = content.getContentType();
        }

        byte[] bytes = mPbData.sendMessageData(contentType,
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
                referMsg,
                pushData);
        mWebSocketCommandManager.putCommand(key, callback);
        JLogger.i("WS-Send", "send message");
        sendWhenOpen(bytes);
    }

    private byte[] encodeContentData(MessageContent content) {
        byte[] encodeBytes;

        if (content instanceof ImageMessage) {
            ImageMessage imageMessage = (ImageMessage) content;
            String local = imageMessage.getLocalPath();
            String thumbnailLocal = imageMessage.getThumbnailLocalPath();
            imageMessage.setLocalPath("");
            imageMessage.setThumbnailLocalPath("");
            encodeBytes = imageMessage.encode();
            imageMessage.setLocalPath(local);
            imageMessage.setThumbnailLocalPath(thumbnailLocal);
        } else if (content instanceof VideoMessage) {
            VideoMessage videoMessage = (VideoMessage) content;
            String local = videoMessage.getLocalPath();
            String snapshotLocal = videoMessage.getSnapshotLocalPath();
            videoMessage.setLocalPath("");
            videoMessage.setSnapshotLocalPath("");
            encodeBytes = videoMessage.encode();
            videoMessage.setLocalPath(local);
            videoMessage.setSnapshotLocalPath(snapshotLocal);
        } else if (content instanceof MediaMessageContent) {
            MediaMessageContent mediaContent = (MediaMessageContent) content;
            String local = mediaContent.getLocalPath();
            mediaContent.setLocalPath("");
            encodeBytes = mediaContent.encode();
            mediaContent.setLocalPath(local);
        } else {
            encodeBytes = content.encode();
        }
        return encodeBytes;
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

    public void updateMessage(String messageId, MessageContent content, Conversation conversation, long timestamp, long msgSeqNo, WebSocketTimestampCallback callback) {
        Integer key = mCmdIndex;
        byte[] contentBytes = encodeContentData(content);
        String contentType;
        if (content instanceof UnknownMessage) {
            UnknownMessage unknown = (UnknownMessage) content;
            contentType = unknown.getMessageType();
        } else {
            contentType = content.getContentType();
        }
        byte[] bytes = mPbData.updateMessageData(messageId, contentType, contentBytes, conversation, timestamp, msgSeqNo, mCmdIndex++);
        mWebSocketCommandManager.putCommand(key, callback);
        JLogger.i("WS-Send", "update message, messageId is " + messageId);
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
                             String userId,
                             QryHisMsgCallback callback) {
        Integer key = mCmdIndex;
        byte[] bytes = mPbData.syncMessagesData(receiveTime, sendTime, userId, mCmdIndex++);
        mWebSocketCommandManager.putCommand(key, callback);
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
                                 String msgId,
                                 long timestamp,
                                 WebSocketTimestampCallback callback) {
        Integer key = mCmdIndex;
        byte[] bytes = mPbData.clearUnreadCountData(conversation, userId, msgIndex, msgId, timestamp, mCmdIndex++);
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

    public void queryHisMsg(Conversation conversation, long startTime, int count, JIMConst.PullDirection direction, List<String> contentTypes, QryHisMsgCallback callback) {
        Integer key = mCmdIndex;
        byte[] bytes = mPbData.queryHisMsgData(conversation, startTime, count, direction, contentTypes, mCmdIndex++);
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

    public void setUnread(Conversation conversation, String userId, WebSocketTimestampCallback callback) {
        Integer key = mCmdIndex;
        byte[] bytes = mPbData.markUnread(conversation, userId, mCmdIndex++);
        mWebSocketCommandManager.putCommand(key, callback);
        JLogger.i("WS-Send", "setUnread, conversation is " + conversation);
        sendWhenOpen(bytes);
    }

    public void getMergedMessageList(String containerMsgId,
                                     long timestamp,
                                     int count,
                                     JIMConst.PullDirection direction,
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
                                      JIMConst.PullDirection direction,
                                      long lastReadIndex,
                                      QryHisMsgCallback callback) {
        Integer key = mCmdIndex;
        byte[] bytes = mPbData.getMentionMessages(conversation, time, count, direction, lastReadIndex, mCmdIndex++);
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

    public void clearHistoryMessage(Conversation conversation, long time, boolean forAllUsers, WebSocketTimestampCallback callback) {
        Integer key = mCmdIndex;
        int scope = forAllUsers ? 1 : 0;
        byte[] bytes = mPbData.clearHistoryMessage(conversation, time, scope, mCmdIndex++);
        mWebSocketCommandManager.putCommand(key, callback);
        JLogger.i("WS-Send", "clearHistoryMessage, conversation is " + conversation + ", time is " + time);
        sendWhenOpen(bytes);
    }

    public void deleteMessage(Conversation conversation, List<ConcreteMessage> msgList, boolean forAllUsers, WebSocketTimestampCallback callback) {
        Integer key = mCmdIndex;
        byte[] bytes = mPbData.deleteMessage(conversation, msgList, forAllUsers, mCmdIndex++);
        mWebSocketCommandManager.putCommand(key, callback);
        JLogger.i("WS-Send", "deleteMessage, conversation is " + conversation);
        sendWhenOpen(bytes);
    }

    public void getUploadFileCred(String userId, UploadFileType fileType, String ext, QryUploadFileCredCallback callback) {
        Integer key = mCmdIndex;
        byte[] bytes = mPbData.getUploadFileCred(userId, fileType, ext, mCmdIndex++);
        JLogger.i("WS-Send", "getUploadFileCred, file type is " + fileType);
        mWebSocketCommandManager.putCommand(key, callback);
        sendWhenOpen(bytes);
    }

    public void setGlobalMute(boolean isMute, String userId, String timezone, List<TimePeriod> periods, WebSocketTimestampCallback callback) {
        Integer key = mCmdIndex;
        byte[] bytes = mPbData.setGlobalMute(isMute, userId, timezone, periods, mCmdIndex++);
        JLogger.i("WS-Send", "setGlobalMute, isMute is " + isMute);
        mWebSocketCommandManager.putCommand(key, callback);
        sendWhenOpen(bytes);
    }

    public void getGlobalMute(String userId, GetGlobalMuteCallback callback) {
        Integer key = mCmdIndex;
        byte[] bytes = mPbData.getGlobalMute(userId, mCmdIndex++);
        JLogger.i("WS-Send", "getGlobalMute");
        mWebSocketCommandManager.putCommand(key, callback);
        sendWhenOpen(bytes);
    }

    public void getFirstUnreadMessage(Conversation conversation, QryHisMsgCallback callback) {
        Integer key = mCmdIndex;
        byte[] bytes = mPbData.qryFirstUnreadMessage(conversation, mCmdIndex++);
        JLogger.i("WS-Send", "getFirstUnreadMessage");
        mWebSocketCommandManager.putCommand(key, callback);
        sendWhenOpen(bytes);
    }

    public void pushSwitch(boolean enablePush, String userId) {
        byte[] bytes = mPbData.pushSwitch(enablePush, userId, mCmdIndex++);
        JLogger.i("WS-Send", "push switch, enable is " + enablePush);
        sendWhenOpen(bytes);
    }

    public void uploadLogStatus(int result, String userId, String messageId, String url) {
        byte[] bytes = mPbData.uploadLogStatus(result, userId, messageId, url, mCmdIndex++);
        JLogger.i("WS-Send", "upload log status, result is " + result);
        sendWhenOpen(bytes);
    }

    public void joinChatroom(String chatroomId, boolean isAutoCreate, WebSocketTimestampCallback callback) {
        Integer key = mCmdIndex;
        byte[] bytes = mPbData.joinChatroom(chatroomId, isAutoCreate, mCmdIndex++);
        JLogger.i("WS-Send", "joinChatroom");
        mWebSocketCommandManager.putCommand(key, callback);
        sendWhenOpen(bytes);
    }

    public void quitChatroom(String chatroomId, WebSocketTimestampCallback callback) {
        Integer key = mCmdIndex;
        byte[] bytes = mPbData.quitChatroom(chatroomId, mCmdIndex++);
        JLogger.i("WS-Send", "quitChatroom");
        mWebSocketCommandManager.putCommand(key, callback);
        sendWhenOpen(bytes);
    }

    public void setAttributes(String chatroomId, Map<String, String> attributes, UpdateChatroomAttrCallback callback) {
        Integer key = mCmdIndex;
        byte[] bytes = mPbData.setAttributes(chatroomId, attributes, mCmdIndex++);
        JLogger.i("WS-Send", "setAttributes");
        mWebSocketCommandManager.putCommand(key, callback);
        sendWhenOpen(bytes);
    }

    public void removeAttributes(String chatroomId, List<String> keys, UpdateChatroomAttrCallback callback) {
        Integer key = mCmdIndex;
        byte[] bytes = mPbData.removeAttributes(chatroomId, keys, mCmdIndex++);
        JLogger.i("WS-Send", "removeAttributes");
        mWebSocketCommandManager.putCommand(key, callback);
        sendWhenOpen(bytes);
    }

    public void syncChatroomMessages(String chatroomId, String userId, long syncTime, int prevMessageCount, QryHisMsgCallback callback) {
        Integer key = mCmdIndex;
        byte[] bytes = mPbData.syncChatroomMessages(chatroomId, userId, syncTime, prevMessageCount, mCmdIndex++);
        mWebSocketCommandManager.putCommand(key, callback);
        JLogger.i("WS-Send", "syncChatroomMessages, id is " + chatroomId + ", time is " + syncTime + ", count is " + prevMessageCount);
        sendWhenOpen(bytes);
    }

    public void syncChatroomAttributes(String chatroomId, String userId, long syncTime) {
        Integer key = mCmdIndex;
        byte[] bytes = mPbData.syncChatroomAttributes(chatroomId, userId, syncTime, mCmdIndex++);
        JLogger.i("WS-Send", "syncChatroomAttributes");
        ChatroomWebSocketCallback callback = new ChatroomWebSocketCallback();
        callback.mChatroomId = chatroomId;
        mWebSocketCommandManager.putCommand(key, callback);
        sendWhenOpen(bytes);
    }

    public void setLanguage(String language, String userId, WebSocketSimpleCallback callback) {
        Integer key = mCmdIndex;
        byte[] bytes = mPbData.setLanguage(language, userId, mCmdIndex++);
        JLogger.i("WS-Send", "set language: " + language);
        mWebSocketCommandManager.putCommand(key, callback);
        sendWhenOpen(bytes);
    }

    public void getLanguage(String userId, WebSocketDataCallback<String> callback) {
        Integer key = mCmdIndex;
        byte[] bytes = mPbData.getLanguage(userId, mCmdIndex++);
        JLogger.i("WS-Send", "get language");
        mWebSocketCommandManager.putCommand(key, callback);
        sendWhenOpen(bytes);
    }

    public void addMessageReaction(String messageId, Conversation conversation, String reactionId, String userId, WebSocketTimestampCallback callback) {
        Integer key = mCmdIndex;
        byte[] bytes = mPbData.addMsgSet(messageId, conversation, reactionId, userId, mCmdIndex++);
        JLogger.i("WS-Send", "add message reaction, messageId is " + messageId + ", reactionId is " + reactionId);
        mWebSocketCommandManager.putCommand(key, callback);
        sendWhenOpen(bytes);
    }

    public void removeMessageReaction(String messageId, Conversation conversation, String reactionId, String userId, WebSocketTimestampCallback callback) {
        Integer key = mCmdIndex;
        byte[] bytes = mPbData.removeMsgSet(messageId, conversation, reactionId, userId, mCmdIndex++);
        JLogger.i("WS-Send", "remove message reaction, messageId is " + messageId + ", reactionId is " + reactionId);
        mWebSocketCommandManager.putCommand(key, callback);
        sendWhenOpen(bytes);
    }

    public void getMessagesReaction(List<String> messageIdList, Conversation conversation, MessageReactionListCallback callback) {
        Integer key = mCmdIndex;
        byte[] bytes = mPbData.queryMsgExSet(messageIdList, conversation, mCmdIndex++);
        JLogger.i("WS-Send", "get messages reaction, count is " + messageIdList.size());
        mWebSocketCommandManager.putCommand(key, callback);
        sendWhenOpen(bytes);
    }

    public void addConversationsToTag(List<Conversation> conversations, String tagId, String userId, WebSocketSimpleCallback callback) {
        Integer key = mCmdIndex;
        byte[] bytes = mPbData.addConversationsToTag(conversations, tagId, userId, mCmdIndex++);
        JLogger.i("WS-Send", "add conversations to tag, tagId is " + tagId + ", count is " + conversations.size());
        mWebSocketCommandManager.putCommand(key, callback);
        sendWhenOpen(bytes);
    }

    public void removeConversationsFromTag(List<Conversation> conversations, String tagId, String userId, WebSocketSimpleCallback callback) {
        Integer key = mCmdIndex;
        byte[] bytes = mPbData.removeConversationsFromTag(conversations, tagId, userId, mCmdIndex++);
        JLogger.i("WS-Send", "remove conversations from tag, tagId is " + tagId + ", count is " + conversations.size());
        mWebSocketCommandManager.putCommand(key, callback);
        sendWhenOpen(bytes);
    }

    public void callInvite(String callId, boolean isMultiCall, CallConst.CallMediaType mediaType, List<String> userIdList, int engineType, CallAuthCallback callback) {
        Integer key = mCmdIndex;
        byte[] bytes = mPbData.callInvite(callId, isMultiCall, mediaType, userIdList, engineType, mCmdIndex++);
        JLogger.i("WS-Send", "call invite, callId is " + callId + ", isMultiCall is " + isMultiCall);
        mWebSocketCommandManager.putCommand(key, callback);
        sendWhenOpen(bytes);
    }

    public void callHangup(String callId, WebSocketSimpleCallback callback) {
        Integer key = mCmdIndex;
        byte[] bytes = mPbData.callHangup(callId, mCmdIndex++);
        JLogger.i("WS-Send", "call hangup, callId is " + callId);
        mWebSocketCommandManager.putCommand(key, callback);
        sendWhenOpen(bytes);
    }

    public void callAccept(String callId, CallAuthCallback callback) {
        Integer key = mCmdIndex;
        byte[] bytes = mPbData.callAccept(callId, mCmdIndex++);
        JLogger.i("WS-Send", "call accept, callId is " + callId);
        mWebSocketCommandManager.putCommand(key, callback);
        sendWhenOpen(bytes);
    }

    public void callConnected(String callId, WebSocketSimpleCallback callback) {
        Integer key = mCmdIndex;
        byte[] bytes = mPbData.callConnected(callId, mCmdIndex++);
        JLogger.i("WS-Send", "call connected, callId is " + callId);
        mWebSocketCommandManager.putCommand(key, callback);
        sendWhenOpen(bytes);
    }

    public void queryCallRooms(String userId, RtcRoomListCallback callback) {
        Integer key = mCmdIndex;
        byte[] bytes = mPbData.queryCallRooms(userId, mCmdIndex++);
        JLogger.i("WS-Send", "query call rooms");
        mWebSocketCommandManager.putCommand(key, callback);
        sendWhenOpen(bytes);
    }

    public void queryCallRoom(String roomId, RtcRoomListCallback callback) {
        Integer key = mCmdIndex;
        byte[] bytes = mPbData.queryCallRoom(roomId, mCmdIndex++);
        JLogger.i("WS-Send", "query call room");
        mWebSocketCommandManager.putCommand(key, callback);
        sendWhenOpen(bytes);
    }

    public void rtcPing(String callId) {
        JLogger.v("WS-Send", "rtc ping");
        byte[] bytes = mPbData.rtcPingData(callId, mCmdIndex++);
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
        } else if (callback instanceof GetGlobalMuteCallback) {
            GetGlobalMuteCallback sCallback = (GetGlobalMuteCallback) callback;
            sCallback.onError(errorCode);
        } else if (callback instanceof UpdateChatroomAttrCallback) {
            UpdateChatroomAttrCallback sCallback = (UpdateChatroomAttrCallback) callback;
            sCallback.onComplete(errorCode, null);
        } else if (callback instanceof CallAuthCallback) {
            CallAuthCallback sCallback = (CallAuthCallback) callback;
            sCallback.onError(errorCode);
        } else if (callback instanceof RtcRoomListCallback) {
            RtcRoomListCallback sCallback = (RtcRoomListCallback) callback;
            sCallback.onError(errorCode);
        } else if (callback instanceof MessageReactionListCallback) {
            MessageReactionListCallback sCallback = (MessageReactionListCallback) callback;
            sCallback.onError(errorCode);
        } else if (callback instanceof WebSocketDataCallback) {
            WebSocketDataCallback sCallback = (WebSocketDataCallback) callback;
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
        void onSyncNotify(long syncTime);
        void onChatroomSyncNotify(String chatroomId, long syncTime);
        void onMessageSend(String messageId, long timestamp, long seqNo, String clientUid, String contentType, MessageContent content, int count);
    }

    public interface IWebSocketChatroomListener {
        void onSyncChatroomAttrNotify(String chatroomId, long syncTime);
        void onAttributesSync(String chatroomId, List<ChatroomAttributeItem> items, int code);
        void onChatroomDestroy(String chatroomId);
        void onChatroomQuit(String chatroomId);
        void onChatroomKick(String chatroomId);
    }

    public interface IWebSocketCallListener {
        void onCallInvite(RtcRoom room, UserInfo inviter, List<UserInfo> targetUsers);
        // 用户主动挂断
        void onCallHangup(RtcRoom room, UserInfo user);
        // 用户掉线或者被踢出通话
        void onCallQuit(RtcRoom room, List<CallMember> members);
        void onCallAccept(RtcRoom room, UserInfo user);
        void onRoomDestroy(RtcRoom room);
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
            case PBRcvObj.PBRcvType.globalMuteAck:
                handleGlobalMuteAck(obj.mGlobalMuteAck);
                break;
            case PBRcvObj.PBRcvType.qryFirstUnreadMsgAck:
                handleFirstUnreadMsgAck(obj.mQryHisMsgAck);
                break;
            case PBRcvObj.PBRcvType.publishChatroomMsgNtf:
                handlePublishChatroomMsgNtf(obj.mPublishMsgNtf);
                break;
            case PBRcvObj.PBRcvType.publishChatroomAttrNtf:
                handlePublishChatroomAttrNtf(obj.mPublishMsgNtf);
                break;
            case PBRcvObj.PBRcvType.syncChatroomMsgAck:
                handleSyncChatroomMsgAck(obj.mQryHisMsgAck);
                break;
            case PBRcvObj.PBRcvType.setChatroomAttrAck:
            case PBRcvObj.PBRcvType.removeChatroomAttrAck:
                handleChatroomAttrAck(obj.mChatroomAttrsAck);
                break;
            case PBRcvObj.PBRcvType.syncChatroomAttrsAck:
                handleSyncChatroomAttrAck(obj.mChatroomAttrsAck);
                break;
            case PBRcvObj.PBRcvType.chatroomDestroyNtf:
                handleChatroomDestroyNtf(obj.mPublishMsgNtf);
                break;
            case PBRcvObj.PBRcvType.chatroomEventNtf:
                handleChatroomEventNtf(obj.mPublishMsgNtf);
                break;
            case PBRcvObj.PBRcvType.rtcRoomEventNtf:
                handleRtcRoomEventNtf(obj.mRtcRoomEventNtf);
                break;
            case PBRcvObj.PBRcvType.rtcInviteEventNtf:
                handleRtcInviteEventNtf(obj.mRtcInviteEventNtf);
                break;
            case PBRcvObj.PBRcvType.callAuthAck:
                handleRtcInviteAck(obj.mStringAck);
                break;
            case PBRcvObj.PBRcvType.qryCallRoomsAck:
                handleRtcQryCallRoomsAck(obj.mRtcQryCallRoomsAck);
                break;
            case PBRcvObj.PBRcvType.qryCallRoomAck:
                //复用 mRtcQryCallRoomsAck
                handleRtcQryCallRoomsAck(obj.mRtcQryCallRoomsAck);
                break;
            case PBRcvObj.PBRcvType.getUserInfoAck:
                handleGetUserInfoAck(obj.mStringAck);
                break;
            case PBRcvObj.PBRcvType.qryMsgExtAck:
                handleQryMsgExtAck(obj.mQryMsgExtAck);
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
        ex.printStackTrace();
        JLogger.e("WS-Connect", "onError, msg is " + ex.getMessage());
        //不处理，避免业务层的异常导致重连
//        mSendHandler.post(this::resetWebSocketClient);
//        if (mConnectListener != null) {
//            mConnectListener.onWebSocketFail();
//        }
    }

    public void setToken(String token) {
        mToken = token;
    }

    public void setAppKey(String appKey) {
        mAppKey = appKey;
    }

    private void sendConnectMsg() {
        mPbData.resetDataConverter();
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
                "",
                mLanguage);
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
        if (c == null) {
            if (ack.code == 0) {
                mMessageListener.onMessageSend(ack.msgId, ack.timestamp, ack.seqNo, ack.clientUid, ack.contentType, ack.content, ack.groupMemberCount);
            }
            return;
        }
        if (c instanceof SendMessageCallback) {
            SendMessageCallback callback = (SendMessageCallback) c;
            if (ack.code != 0) {
                callback.onError(ack.code, callback.getClientMsgNo());
            } else {
                callback.onSuccess(callback.getClientMsgNo(), ack.msgId, ack.timestamp, ack.seqNo, ack.contentType, ack.content, ack.groupMemberCount);
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

    private void handleSyncChatroomMsgAck(PBRcvObj.QryHisMsgAck ack) {
        JLogger.i("WS-Receive", "handleSyncChatroomMsgAck");
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

    private void handlePublishChatroomMsgNtf(PBRcvObj.PublishMsgNtf ntf) {
        JLogger.i("WS-Receive", "handlePublishChatroomMsgNtf");
        if (mMessageListener != null) {
            mMessageListener.onChatroomSyncNotify(ntf.chatroomId, ntf.syncTime);
        }
    }

    private void handlePublishChatroomAttrNtf(PBRcvObj.PublishMsgNtf ntf) {
        JLogger.i("WS-Receive", "handlePublishChatroomAttrNtf");
        if (mChatroomListener != null) {
            mChatroomListener.onSyncChatroomAttrNotify(ntf.chatroomId, ntf.syncTime);
        }
    }

    private void handleChatroomAttrAck(PBRcvObj.ChatroomAttrsAck ack) {
        JLogger.i("WS-Receive", "handleChatroomAttrAck");
        IWebSocketCallback c = mWebSocketCommandManager.removeCommand(ack.index);
        if (c == null) return;
        if (c instanceof UpdateChatroomAttrCallback) {
            UpdateChatroomAttrCallback callback = (UpdateChatroomAttrCallback) c;
            callback.onComplete(ack.code, ack.items);
        }
    }

    private void handleSyncChatroomAttrAck(PBRcvObj.ChatroomAttrsAck ack) {
        JLogger.i("WS-Receive", "handleSyncChatroomAttrAck");
        IWebSocketCallback c = mWebSocketCommandManager.removeCommand(ack.index);
        if (c == null) return;
        if (c instanceof ChatroomWebSocketCallback) {
            ChatroomWebSocketCallback callback = (ChatroomWebSocketCallback) c;
            if (mChatroomListener != null) {
                mChatroomListener.onAttributesSync(callback.mChatroomId, ack.items, ack.code);
            }
        }
    }

    private void handleChatroomDestroyNtf(PBRcvObj.PublishMsgNtf ntf) {
        JLogger.i("WS-Receive", "handleChatroomDestroyNtf");
        if (mChatroomListener != null) {
            mChatroomListener.onChatroomDestroy(ntf.chatroomId);
        }
    }

    private void handleChatroomEventNtf(PBRcvObj.PublishMsgNtf ntf) {
        JLogger.i("WS-Receive", "handleChatroomEventNtf");
        if (ntf.type == PBRcvObj.PBChatroomEventType.QUIT
        || ntf.type == PBRcvObj.PBChatroomEventType.FALLOUT) {
            if (mChatroomListener != null) {
                mChatroomListener.onChatroomQuit(ntf.chatroomId);
            }
        } else if (ntf.type == PBRcvObj.PBChatroomEventType.KICK) {
            if (mChatroomListener != null) {
                mChatroomListener.onChatroomKick(ntf.chatroomId);
            }
        }
    }

    private void handleRtcRoomEventNtf(PBRcvObj.RtcRoomEventNtf ntf) {
        JLogger.i("Call-RmEvent", "type is " + ntf.eventType);
        switch (ntf.eventType) {
            case QUIT:
                if (mCallListener != null) {
                    mCallListener.onCallQuit(ntf.room, ntf.members);
                }
                break;

            case DESTROY:
                if (mCallListener != null) {
                    mCallListener.onRoomDestroy(ntf.room);
                }
                break;

            //todo

            default:
                break;
        }
    }

    private void handleRtcInviteEventNtf(PBRcvObj.RtcInviteEventNtf ntf) {
        JLogger.i("WS-Receive", "handleRtcInviteEventNtf");
        switch (ntf.type) {
            case INVITE:
                if (mCallListener != null) {
                    mCallListener.onCallInvite(ntf.room, ntf.user, ntf.targetUsers);
                }
                break;

            case HANGUP:
                if (mCallListener != null) {
                    mCallListener.onCallHangup(ntf.room, ntf.user);
                }
                break;

            case ACCEPT:
                if (mCallListener != null) {
                    mCallListener.onCallAccept(ntf.room, ntf.user);
                }
                break;

            default:
                break;
        }
    }

    private void handleRtcInviteAck(PBRcvObj.StringAck ack) {
        JLogger.i("WS-Receive", "handleRtcInviteAck");
        IWebSocketCallback c = mWebSocketCommandManager.removeCommand(ack.index);
        if (c == null) {
            return;
        }
        if (c instanceof CallAuthCallback) {
            CallAuthCallback callback = (CallAuthCallback) c;
            if (ack.code != 0) {
                callback.onError(ack.code);
            } else {
                callback.onSuccess(ack.str);
            }
        }
    }

    private void handleRtcQryCallRoomsAck(PBRcvObj.RtcQryCallRoomsAck ack) {
        JLogger.i("WS-Receive", "handleRtcQryCallRoomsAck");
        IWebSocketCallback c = mWebSocketCommandManager.removeCommand(ack.index);
        if (c == null) {
            return;
        }
        if (c instanceof RtcRoomListCallback) {
            RtcRoomListCallback callback = (RtcRoomListCallback) c;
            if (ack.code != 0) {
                callback.onError(ack.code);
            } else {
                callback.onSuccess(ack.rooms);
            }
        }
    }

    private void handleGetUserInfoAck(PBRcvObj.StringAck ack) {
        JLogger.i("WS-Receive", "handleGetUserInfoAck");
        IWebSocketCallback c = mWebSocketCommandManager.removeCommand(ack.index);
        if (c == null) {
            return;
        }
        if (c instanceof WebSocketDataCallback) {
            @SuppressWarnings("unchecked")
            WebSocketDataCallback<String> callback = (WebSocketDataCallback<String>) c;
            if (ack.code != 0) {
                callback.onError(ack.code);
            } else {
                callback.onSuccess(ack.str);
            }
        }
    }

    private void handleQryMsgExtAck(PBRcvObj.QryMsgExtAck ack) {
        JLogger.i("WS-Receive", "handleQryMsgExtAck");
        IWebSocketCallback c = mWebSocketCommandManager.removeCommand(ack.index);
        if (c == null) {
            return;
        }
        if (c instanceof MessageReactionListCallback) {
            MessageReactionListCallback callback = (MessageReactionListCallback) c;
            if (ack.code != 0) {
                callback.onError(ack.code);
            } else {
                callback.onSuccess(ack.reactionList);
            }
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

    private void handleGlobalMuteAck(PBRcvObj.GlobalMuteAck ack) {
        JLogger.i("WS-Receive", "handleGlobalMuteAck, code is " + ack.code);
        IWebSocketCallback c = mWebSocketCommandManager.removeCommand(ack.index);
        if (c == null) return;
        if (c instanceof GetGlobalMuteCallback) {
            GetGlobalMuteCallback callback = (GetGlobalMuteCallback) c;
            if (ack.code != 0) {
                callback.onError(ack.code);
            } else {
                callback.onSuccess(ack.isMute, ack.timezone, ack.periods);
            }
        }
    }

    private void handleFirstUnreadMsgAck(PBRcvObj.QryHisMsgAck ack) {
        JLogger.i("WS-Receive", "handleFirstUnreadMsgAck, code is " + ack.code);
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

    private void sendWhenOpen(byte[] bytes) {
        mSendHandler.post(() -> {
            if (mWebSocketClient != null && mWebSocketClient.isOpen()) {
                try {
                    mWebSocketClient.send(bytes);
                } catch (WebsocketNotConnectedException e) {
                    e.printStackTrace();
                    webSocketSendFail();
                }
                return;
            }
            JLogger.e("WS-Send", mWebSocketClient == null ? "mWebSocketClient is null" : "mWebSocketClient is not open");
            //可能是还没连接成功，或者根本就没连接
            //webSocketSendFail();
            pushRemainCmdAndCallbackError();
        });
    }

    private void webSocketSendFail() {
        pushRemainCmdAndCallbackError();
        mConnectListener.onWebSocketClose();
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
    private String mLanguage;
    private final PBData mPbData;
    private final WebSocketCommandManager mWebSocketCommandManager;
    private final HeartbeatManager mHeartbeatManager;
    private IWebSocketConnectListener mConnectListener;
    private IWebSocketMessageListener mMessageListener;
    private IWebSocketChatroomListener mChatroomListener;
    private IWebSocketCallListener mCallListener;
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