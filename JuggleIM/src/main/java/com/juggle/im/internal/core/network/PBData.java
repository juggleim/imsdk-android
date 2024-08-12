package com.juggle.im.internal.core.network;

import android.text.TextUtils;

import androidx.annotation.NonNull;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import com.juggle.im.JIMConst;
import com.juggle.im.internal.ContentTypeCenter;
import com.juggle.im.internal.model.ConcreteConversationInfo;
import com.juggle.im.internal.model.ConcreteMessage;
import com.juggle.im.internal.model.MergeInfo;
import com.juggle.im.internal.model.upload.UploadFileType;
import com.juggle.im.internal.model.upload.UploadOssType;
import com.juggle.im.internal.model.upload.UploadPreSignCred;
import com.juggle.im.internal.model.upload.UploadQiNiuCred;
import com.juggle.im.internal.util.JLogger;
import com.juggle.im.model.Conversation;
import com.juggle.im.model.ConversationMentionInfo;
import com.juggle.im.model.GroupInfo;
import com.juggle.im.model.GroupMessageReadInfo;
import com.juggle.im.model.Message;
import com.juggle.im.model.MessageContent;
import com.juggle.im.model.MessageMentionInfo;
import com.juggle.im.model.TimePeriod;
import com.juggle.im.model.UserInfo;
import com.juggle.im.model.messages.MergeMessage;
import com.juggle.im.push.PushChannel;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import app_messages.Appmessages;
import app_messages.Connect;
import app_messages.Pushtoken;

class PBData {
    byte[] connectData(String appKey,
                       String token,
                       String deviceId,
                       String platform,
                       String deviceCompany,
                       String deviceModel,
                       String osVersion,
                       String packageName,
                       PushChannel pushChannel,
                       String pushToken,
                       String networkId,
                       String ispNum,
                       String clientIp) {
        Connect.ConnectMsgBody.Builder builder = Connect.ConnectMsgBody.newBuilder();
        builder.setProtoId(PROTO_ID)
                .setSdkVersion(SDK_VERSION)
                .setAppkey(appKey)
                .setToken(token)
                .setDeviceId(deviceId)
                .setPlatform(platform)
                .setDeviceCompany(deviceCompany)
                .setDeviceModel(deviceModel)
                .setDeviceOsVersion(osVersion)
                .setNetworkId(networkId)
                .setIspNum(ispNum)
                .setClientIp(clientIp)
                .setPackageName(packageName);
        if (!TextUtils.isEmpty(pushToken)) {
            builder.setPushToken(pushToken);
        }
        if (pushChannel != null) {
            switch (pushChannel) {
                case HUAWEI:
                    builder.setPushChannel("Huawei");
                    break;
                case XIAOMI:
                    builder.setPushChannel("Xiaomi");
                    break;
            }
        }
        Connect.ConnectMsgBody body = builder.build();
        Connect.ImWebsocketMsg msg = Connect.ImWebsocketMsg.newBuilder()
                .setVersion(PROTOCOL_VERSION)
                .setCmd(CmdType.connect)
                .setQos(Qos.yes)
                .setConnectMsgBody(body)
                .build();
        return msg.toByteArray();
    }

    byte[] disconnectData(boolean receivePush) {
        int code = receivePush ? 0 : 1;
        Connect.DisconnectMsgBody body = Connect.DisconnectMsgBody.newBuilder()
                .setCode(code)
                .setTimestamp(System.currentTimeMillis())
                .build();
        Connect.ImWebsocketMsg msg = Connect.ImWebsocketMsg.newBuilder()
                .setVersion(PROTOCOL_VERSION)
                .setCmd(CmdType.disconnect)
                .setQos(Qos.no)
                .setDisconnectMsgBody(body)
                .build();
        return msg.toByteArray();
    }

    byte[] sendMessageData(String contentType,
                           byte[] msgData,
                           int flags,
                           String clientUid,
                           MergeInfo mergeInfo,
                           boolean isBroadcast,
                           String userId,
                           int index,
                           Conversation.ConversationType conversationType,
                           String conversationId,
                           MessageMentionInfo mentionInfo,
                           ConcreteMessage referMsg) {
        ByteString byteString = ByteString.copyFrom(msgData);
        Appmessages.UpMsg.Builder upMsgBuilder = Appmessages.UpMsg.newBuilder();
        upMsgBuilder.setMsgType(contentType)
                .setMsgContent(byteString)
                .setFlags(flags)
                .setClientUid(clientUid);
        if (mergeInfo != null && TextUtils.isEmpty(mergeInfo.getContainerMsgId()) && mergeInfo.getMessages() != null) {
            flags |= MessageContent.MessageFlag.IS_MERGED.getValue();
            upMsgBuilder.setFlags(flags);

            int channelType = mergeInfo.getConversation().getConversationType().getValue();
            String targetId = mergeInfo.getConversation().getConversationId();
            Appmessages.MergedMsgs.Builder mergedMsgsBuilder = Appmessages.MergedMsgs.newBuilder();
            mergedMsgsBuilder.setChannelTypeValue(channelType)
                    .setUserId(userId)
                    .setTargetId(targetId);
            for (ConcreteMessage msg : mergeInfo.getMessages()) {
                Appmessages.SimpleMsg simpleMsg = Appmessages.SimpleMsg.newBuilder()
                        .setMsgId(msg.getMessageId())
                        .setMsgTime(msg.getTimestamp())
                        .setMsgReadIndex(msg.getSeqNo())
                        .build();
                mergedMsgsBuilder.addMsgs(simpleMsg);
            }
            upMsgBuilder.setMergedMsgs(mergedMsgsBuilder.build());
        }
        if (isBroadcast) {
            flags |= MessageContent.MessageFlag.IS_BROADCAST.getValue();
            upMsgBuilder.setFlags(flags);
        }
        if (mentionInfo != null) {
            Appmessages.MentionInfo.Builder pbMentionBuilder = Appmessages.MentionInfo.newBuilder();
            pbMentionBuilder.setMentionTypeValue(mentionInfo.getType().getValue());
            if (mentionInfo.getTargetUsers() != null) {
                for (UserInfo userInfo : mentionInfo.getTargetUsers()) {
                    Appmessages.UserInfo pbUser = Appmessages.UserInfo.newBuilder()
                            .setUserId(userInfo.getUserId())
                            .build();
                    pbMentionBuilder.addTargetUsers(pbUser);
                }
            }
            upMsgBuilder.setMentionInfo(pbMentionBuilder);
        }
        if (referMsg != null) {
            Appmessages.DownMsg downMsg = downMsgWithMessage(referMsg);
            upMsgBuilder.setReferMsg(downMsg);
        }
        Appmessages.UpMsg upMsg = upMsgBuilder.build();

        String topic = "";
        switch (conversationType) {
            case PRIVATE:
                topic = P_MSG;
                break;
            case GROUP:
                topic = G_MSG;
                break;
            case CHATROOM:
                topic = C_MSG;
                break;
            case SYSTEM:
                //todo 系统消息还没做
                break;
        }

        Connect.PublishMsgBody publishMsgBody = Connect.PublishMsgBody.newBuilder()
                .setIndex(index)
                .setTopic(topic)
                .setTargetId(conversationId)
                .setData(upMsg.toByteString())
                .build();

        mMsgCmdMap.put(index, topic);

        Connect.ImWebsocketMsg msg = createImWebsocketMsgWithPublishMsg(publishMsgBody);
        return msg.toByteArray();
    }

    byte[] recallMessageData(String messageId, Conversation conversation, long timestamp, Map<String, String> extras, int index) {
        Appmessages.RecallMsgReq.Builder builder = Appmessages.RecallMsgReq.newBuilder()
                .setMsgId(messageId)
                .setTargetId(conversation.getConversationId())
                .setChannelTypeValue(conversation.getConversationType().getValue())
                .setMsgTime(timestamp);
        if (extras != null) {
            for (Map.Entry<String, String> entry : extras.entrySet()) {
                if (entry.getKey() == null || entry.getValue() == null) continue;
                Appmessages.KvItem kvItem = Appmessages.KvItem.newBuilder()
                        .setKey(entry.getKey())
                        .setValue(entry.getValue())
                        .build();
                builder.addExts(kvItem);
            }
        }
        Appmessages.RecallMsgReq req = builder.build();

        Connect.QueryMsgBody body = Connect.QueryMsgBody.newBuilder()
                .setIndex(index)
                .setTopic(RECALL_MSG)
                .setTargetId(conversation.getConversationId())
                .setData(req.toByteString())
                .build();

        mMsgCmdMap.put(index, RECALL_MSG);

        Connect.ImWebsocketMsg msg = createImWebsocketMsgWithQueryMsg(body);
        return msg.toByteArray();
    }

    byte[] syncConversationsData(long startTime, int count, String userId, int index) {
        Appmessages.SyncConversationsReq req = Appmessages.SyncConversationsReq.newBuilder()
                .setStartTime(startTime)
                .setCount(count)
                .build();

        Connect.QueryMsgBody body = Connect.QueryMsgBody.newBuilder()
                .setIndex(index)
                .setTopic(SYNC_CONV)
                .setTargetId(userId)
                .setData(req.toByteString())
                .build();

        mMsgCmdMap.put(index, body.getTopic());
        Connect.ImWebsocketMsg m = createImWebsocketMsgWithQueryMsg(body);
        return m.toByteArray();
    }

    byte[] deleteConversationData(Conversation conversation, String userId, int index) {
        Appmessages.Conversation c = pbConversationFromConversation(conversation).build();
        Appmessages.ConversationsReq req = Appmessages.ConversationsReq.newBuilder()
                .addConversations(c)
                .build();

        Connect.QueryMsgBody body = Connect.QueryMsgBody.newBuilder()
                .setIndex(index)
                .setTopic(DEL_CONV)
                .setTargetId(userId)
                .setData(req.toByteString())
                .build();

        mMsgCmdMap.put(index, body.getTopic());
        Connect.ImWebsocketMsg m = createImWebsocketMsgWithQueryMsg(body);
        return m.toByteArray();
    }

    byte[] clearUnreadCountData(Conversation conversation, String userId, long msgIndex, int index) {
        Appmessages.Conversation.Builder builder = pbConversationFromConversation(conversation);
        builder.setLatestReadIndex(msgIndex);
        Appmessages.Conversation c = builder.build();
        Appmessages.ClearUnreadReq req = Appmessages.ClearUnreadReq.newBuilder()
                .addConversations(c)
                .build();

        Connect.QueryMsgBody body = Connect.QueryMsgBody.newBuilder()
                .setIndex(index)
                .setTopic(CLEAR_UNREAD)
                .setTargetId(userId)
                .setData(req.toByteString())
                .build();

        mMsgCmdMap.put(index, body.getTopic());
        Connect.ImWebsocketMsg m = createImWebsocketMsgWithQueryMsg(body);
        return m.toByteArray();
    }

    byte[] clearTotalUnreadCountData(String userId, long time, int index) {
        Appmessages.QryTotalUnreadCountReq req = Appmessages.QryTotalUnreadCountReq.newBuilder()
                .setTime(time)
                .build();

        Connect.QueryMsgBody body = Connect.QueryMsgBody.newBuilder()
                .setIndex(index)
                .setTopic(CLEAR_TOTAL_UNREAD)
                .setTargetId(userId)
                .setData(req.toByteString())
                .build();

        mMsgCmdMap.put(index, body.getTopic());
        Connect.ImWebsocketMsg m = createImWebsocketMsgWithQueryMsg(body);
        return m.toByteArray();
    }

    public byte[] addConversationInfo(Conversation conversation, String userId, Integer index) {
        Appmessages.Conversation req = pbConversationFromConversation(conversation).build();

        Connect.QueryMsgBody body = Connect.QueryMsgBody.newBuilder()
                .setIndex(index)
                .setTopic(ADD_CONVERSATION)
                .setTargetId(userId)
                .setData(req.toByteString())
                .build();

        mMsgCmdMap.put(index, body.getTopic());
        Connect.ImWebsocketMsg m = createImWebsocketMsgWithQueryMsg(body);
        return m.toByteArray();
    }

    byte[] syncMessagesData(long receiveTime, long sendTime, String userId, int index) {
        Appmessages.SyncMsgReq req = Appmessages.SyncMsgReq.newBuilder()
                .setSyncTime(receiveTime)
                .setContainsSendBox(true)
                .setSendBoxSyncTime(sendTime)
                .build();

        Connect.QueryMsgBody body = Connect.QueryMsgBody.newBuilder()
                .setIndex(index)
                .setTopic(SYNC_MSG)
                .setTargetId(userId)
                .setData(req.toByteString())
                .build();
        mMsgCmdMap.put(index, body.getTopic());
        Connect.ImWebsocketMsg m = createImWebsocketMsgWithQueryMsg(body);
        return m.toByteArray();
    }

    byte[] sendReadReceiptData(Conversation conversation,
                               List<String> messageIds,
                               int index) {
        Appmessages.MarkReadReq.Builder builder = Appmessages.MarkReadReq.newBuilder();
        builder.setChannelTypeValue(conversation.getConversationType().getValue());
        builder.setTargetId(conversation.getConversationId());
        for (String messageId : messageIds) {
            Appmessages.SimpleMsg simpleMsg = Appmessages.SimpleMsg.newBuilder().setMsgId(messageId).build();
            builder.addMsgs(simpleMsg);
        }
        Appmessages.MarkReadReq req = builder.build();

        Connect.QueryMsgBody body = Connect.QueryMsgBody.newBuilder()
                .setIndex(index)
                .setTopic(MARK_READ)
                .setTargetId(conversation.getConversationId())
                .setData(req.toByteString())
                .build();
        mMsgCmdMap.put(index, body.getTopic());
        Connect.ImWebsocketMsg m = createImWebsocketMsgWithQueryMsg(body);
        return m.toByteArray();
    }

    byte[] getGroupMessageReadDetail(Conversation conversation,
                                     String messageId,
                                     int index) {
        Appmessages.QryReadDetailReq req = Appmessages.QryReadDetailReq.newBuilder()
                .setTargetId(conversation.getConversationId())
                .setChannelTypeValue(conversation.getConversationType().getValue())
                .setMsgId(messageId)
                .build();
        Connect.QueryMsgBody body = Connect.QueryMsgBody.newBuilder()
                .setIndex(index)
                .setTopic(QRY_READ_DETAIL)
                .setTargetId(conversation.getConversationId())
                .setData(req.toByteString())
                .build();
        mMsgCmdMap.put(index, body.getTopic());
        Connect.ImWebsocketMsg m = createImWebsocketMsgWithQueryMsg(body);
        return m.toByteArray();
    }

    byte[] queryHisMsgData(Conversation conversation, long startTime, int count, JIMConst.PullDirection direction, int index) {
        int order = direction == JIMConst.PullDirection.OLDER ? 0 : 1;
        Appmessages.QryHisMsgsReq req = Appmessages.QryHisMsgsReq.newBuilder()
                .setTargetId(conversation.getConversationId())
                .setChannelTypeValue(conversation.getConversationType().getValue())
                .setStartTime(startTime)
                .setCount(count)
                .setOrder(order)
                .build();
        Connect.QueryMsgBody body = Connect.QueryMsgBody.newBuilder()
                .setIndex(index)
                .setTopic(QRY_HIS_MSG)
                .setTargetId(conversation.getConversationId())
                .setData(req.toByteString())
                .build();
        mMsgCmdMap.put(index, body.getTopic());
        Connect.ImWebsocketMsg m = createImWebsocketMsgWithQueryMsg(body);
        return m.toByteArray();
    }

    byte[] queryHisMsgDataByIds(Conversation conversation, List<String> messageIds, int index) {
        Appmessages.QryHisMsgByIdsReq.Builder builder = Appmessages.QryHisMsgByIdsReq.newBuilder();
        builder.setTargetId(conversation.getConversationId())
                .setChannelTypeValue(conversation.getConversationType().getValue());
        for (String messageId : messageIds) {
            builder.addMsgIds(messageId);
        }
        Appmessages.QryHisMsgByIdsReq req = builder.build();
        Connect.QueryMsgBody body = Connect.QueryMsgBody.newBuilder()
                .setIndex(index)
                .setTopic(QRY_HISMSG_BY_IDS)
                .setTargetId(conversation.getConversationId())
                .setData(req.toByteString())
                .build();
        mMsgCmdMap.put(index, body.getTopic());
        Connect.ImWebsocketMsg m = createImWebsocketMsgWithQueryMsg(body);
        return m.toByteArray();
    }

    byte[] disturbData(Conversation conversation, String userId, boolean isMute, int index) {
        Appmessages.UndisturbConverItem item = Appmessages.UndisturbConverItem.newBuilder()
                .setTargetId(conversation.getConversationId())
                .setChannelTypeValue(conversation.getConversationType().getValue())
                .setUndisturbType(isMute ? 1 : 0)
                .build();
        Appmessages.UndisturbConversReq req = Appmessages.UndisturbConversReq.newBuilder()
                .addItems(item)
                .build();
        Connect.QueryMsgBody body = Connect.QueryMsgBody.newBuilder()
                .setIndex(index)
                .setTopic(UNDISTURB_CONVERS)
                .setTargetId(userId)
                .setData(req.toByteString())
                .build();
        mMsgCmdMap.put(index, body.getTopic());
        Connect.ImWebsocketMsg m = createImWebsocketMsgWithQueryMsg(body);
        return m.toByteArray();
    }

    byte[] topConversationData(Conversation conversation,
                               String userId,
                               boolean isTop,
                               int index) {
        Appmessages.Conversation pbConversation = Appmessages.Conversation.newBuilder()
                .setChannelTypeValue(conversation.getConversationType().getValue())
                .setTargetId(conversation.getConversationId())
                .setIsTop(isTop ? 1 : 0)
                .build();
        Appmessages.ConversationsReq req = Appmessages.ConversationsReq.newBuilder()
                .addConversations(pbConversation)
                .build();
        Connect.QueryMsgBody body = Connect.QueryMsgBody.newBuilder()
                .setIndex(index)
                .setTopic(TOP_CONVERS)
                .setTargetId(userId)
                .setData(req.toByteString())
                .build();
        mMsgCmdMap.put(index, body.getTopic());
        Connect.ImWebsocketMsg m = createImWebsocketMsgWithQueryMsg(body);
        return m.toByteArray();
    }

    byte[] getMergedMessageList(String containerMsgId,
                                long timestamp,
                                int count,
                                JIMConst.PullDirection direction,
                                int index) {
        int order = direction == JIMConst.PullDirection.OLDER ? 0 : 1;
        Appmessages.QryMergedMsgsReq req = Appmessages.QryMergedMsgsReq.newBuilder()
                .setStartTime(timestamp)
                .setCount(count)
                .setOrder(order)
                .build();
        Connect.QueryMsgBody body = Connect.QueryMsgBody.newBuilder()
                .setIndex(index)
                .setTopic(QRY_MERGED_MSGS)
                .setTargetId(containerMsgId)
                .setData(req.toByteString())
                .build();
        mMsgCmdMap.put(index, body.getTopic());
        Connect.ImWebsocketMsg m = createImWebsocketMsgWithQueryMsg(body);
        return m.toByteArray();
    }

    byte[] registerPushToken(PushChannel channel,
                             String token,
                             String deviceId,
                             String packageName,
                             String userId,
                             int index) {
        Pushtoken.RegPushTokenReq req = Pushtoken.RegPushTokenReq.newBuilder()
                .setDeviceId(deviceId)
                .setPlatformValue(PLATFORM_ANDROID)
                .setPushChannelValue(channel.getCode())
                .setPushToken(token)
                .setPackageName(packageName)
                .build();
        Connect.QueryMsgBody body = Connect.QueryMsgBody.newBuilder()
                .setIndex(index)
                .setTopic(REG_PUSH_TOKEN)
                .setTargetId(userId)
                .setData(req.toByteString())
                .build();
        mMsgCmdMap.put(index, body.getTopic());
        Connect.ImWebsocketMsg m = createImWebsocketMsgWithQueryMsg(body);
        return m.toByteArray();
    }

    byte[] getMentionMessages(Conversation conversation,
                              long timestamp,
                              int count,
                              JIMConst.PullDirection direction,
                              int index) {
        int order = direction == JIMConst.PullDirection.OLDER ? 0 : 1;
        Appmessages.QryMentionMsgsReq req = Appmessages.QryMentionMsgsReq.newBuilder()
                .setTargetId(conversation.getConversationId())
                .setChannelTypeValue(conversation.getConversationType().getValue())
                .setStartTime(timestamp)
                .setCount(count)
                .setOrder(order)
                .build();
        Connect.QueryMsgBody body = Connect.QueryMsgBody.newBuilder()
                .setIndex(index)
                .setTopic(QRY_MENTION_MSGS)
                .setTargetId(conversation.getConversationId())
                .setData(req.toByteString())
                .build();
        mMsgCmdMap.put(index, body.getTopic());
        Connect.ImWebsocketMsg m = createImWebsocketMsgWithQueryMsg(body);
        return m.toByteArray();
    }

    byte[] clearHistoryMessage(Conversation conversation, long time, int scope, int index) {
        Appmessages.CleanHisMsgReq req = Appmessages.CleanHisMsgReq.newBuilder()
                .setTargetId(conversation.getConversationId())
                .setChannelTypeValue(conversation.getConversationType().getValue())
                .setCleanMsgTime(time)
                .setCleanScope(scope)
                .build();

        Connect.QueryMsgBody body = Connect.QueryMsgBody.newBuilder()
                .setIndex(index)
                .setTopic(CLEAR_HIS_MSG)
                .setTargetId(conversation.getConversationId())
                .setData(req.toByteString())
                .build();

        mMsgCmdMap.put(index, body.getTopic());
        Connect.ImWebsocketMsg m = createImWebsocketMsgWithQueryMsg(body);
        return m.toByteArray();
    }

    byte[] deleteMessage(Conversation conversation, List<ConcreteMessage> msgList, int index) {
        Appmessages.DelHisMsgsReq.Builder builder = Appmessages.DelHisMsgsReq.newBuilder()
                .setTargetId(conversation.getConversationId())
                .setChannelTypeValue(conversation.getConversationType().getValue())
                .setDelScope(0);
        for (ConcreteMessage msg : msgList) {
            Appmessages.SimpleMsg simpleMsg = Appmessages.SimpleMsg.newBuilder()
                    .setMsgId(msg.getMessageId())
                    .setMsgTime(msg.getTimestamp())
                    .build();
            builder.addMsgs(simpleMsg);
        }
        Appmessages.DelHisMsgsReq req = builder.build();

        Connect.QueryMsgBody body = Connect.QueryMsgBody.newBuilder()
                .setIndex(index)
                .setTopic(DELETE_MSG)
                .setTargetId(conversation.getConversationId())
                .setData(req.toByteString())
                .build();
        mMsgCmdMap.put(index, body.getTopic());
        Connect.ImWebsocketMsg m = createImWebsocketMsgWithQueryMsg(body);
        return m.toByteArray();
    }

    byte[] getUploadFileCred(String userId, UploadFileType fileType, String ext, Integer index) {
        Appmessages.QryFileCredReq req = Appmessages.QryFileCredReq.newBuilder()
                .setFileTypeValue(fileType.getValue())
                .setExt(ext == null ? "" : ext)
                .build();
        Connect.QueryMsgBody body = Connect.QueryMsgBody.newBuilder()
                .setIndex(index)
                .setTopic(QRY_FILE_CRED)
                .setTargetId(userId)
                .setData(req.toByteString())
                .build();

        mMsgCmdMap.put(index, body.getTopic());
        Connect.ImWebsocketMsg m = createImWebsocketMsgWithQueryMsg(body);
        return m.toByteArray();
    }

    byte[] setGlobalMute(boolean isMute, String userId, String timezone, List<TimePeriod> periods, int index) {
        Appmessages.UserUndisturb.Builder builder = Appmessages.UserUndisturb.newBuilder()
                .setSwitch(isMute)
                .setTimezone(timezone);
        for (TimePeriod period : periods) {
            Appmessages.UserUndisturbItem item = Appmessages.UserUndisturbItem.newBuilder()
                    .setStart(period.getStartTime())
                    .setEnd(period.getEndTime())
                    .build();
            builder.addRules(item);
        }
        Appmessages.UserUndisturb req = builder.build();

        Connect.QueryMsgBody body = Connect.QueryMsgBody.newBuilder()
                .setIndex(index)
                .setTopic(SET_USER_UNDISTURB)
                .setTargetId(userId)
                .setData(req.toByteString())
                .build();

        mMsgCmdMap.put(index, body.getTopic());
        Connect.ImWebsocketMsg m = createImWebsocketMsgWithQueryMsg(body);
        return m.toByteArray();
    }

    byte[] getGlobalMute(String userId, int index) {
        Connect.QueryMsgBody body = Connect.QueryMsgBody.newBuilder()
                .setIndex(index)
                .setTopic(GET_USER_UNDISTURB)
                .setTargetId(userId)
                .build();
        mMsgCmdMap.put(index, body.getTopic());
        Connect.ImWebsocketMsg m = createImWebsocketMsgWithQueryMsg(body);
        return m.toByteArray();
    }

    byte[] pingData() {
        Connect.ImWebsocketMsg msg = Connect.ImWebsocketMsg.newBuilder()
                .setVersion(PROTOCOL_VERSION)
                .setCmd(CmdType.ping)
                .setQos(Qos.no)
                .build();
        return msg.toByteArray();
    }

    byte[] publishAckData(int index) {
        Connect.PublishAckMsgBody body = Connect.PublishAckMsgBody.newBuilder()
                .setIndex(index)
                .build();

        Connect.ImWebsocketMsg msg = Connect.ImWebsocketMsg.newBuilder()
                .setVersion(PROTOCOL_VERSION)
                .setCmd(CmdType.publishAck)
                .setQos(Qos.no)
                .setPubAckMsgBody(body)
                .build();
        return msg.toByteArray();
    }

    PBRcvObj rcvObjWithBytes(ByteBuffer byteBuffer) {
        PBRcvObj obj = new PBRcvObj();
        try {
            Connect.ImWebsocketMsg msg = Connect.ImWebsocketMsg.parseFrom(byteBuffer);
            if (msg == null) {
                JLogger.e("PB-Parse", "rcvObjWithBytes msg is null");
                obj.setRcvType(PBRcvObj.PBRcvType.parseError);
                return obj;
            }
            if (msg.getCmd() == CmdType.pong) {
                obj.setRcvType(PBRcvObj.PBRcvType.pong);
                return obj;
            }
            switch (msg.getTestofCase()) {
                case CONNECTACKMSGBODY:
                    obj.setRcvType(PBRcvObj.PBRcvType.connectAck);
                    PBRcvObj.ConnectAck ack = new PBRcvObj.ConnectAck();
                    ack.code = msg.getConnectAckMsgBody().getCode();
                    ack.userId = msg.getConnectAckMsgBody().getUserId();
                    ack.session = msg.getConnectAckMsgBody().getSession();
                    ack.extra = msg.getConnectAckMsgBody().getExt();
                    obj.mConnectAck = ack;
                    break;

                case PUBACKMSGBODY: {
                    int type = getTypeInCmdMap(msg.getPubAckMsgBody().getIndex());
                    obj.setRcvType(type);
                    if (type == PBRcvObj.PBRcvType.cmdMatchError) {
                        break;
                    }
                    PBRcvObj.PublishMsgAck a = new PBRcvObj.PublishMsgAck();
                    a.index = msg.getPubAckMsgBody().getIndex();
                    a.code = msg.getPubAckMsgBody().getCode();
                    a.msgId = msg.getPubAckMsgBody().getMsgId();
                    a.timestamp = msg.getPubAckMsgBody().getTimestamp();
                    a.seqNo = msg.getPubAckMsgBody().getMsgSeqNo();
                    obj.mPublishMsgAck = a;
                }
                break;

                case QRYACKMSGBODY:
                    int type = getTypeInCmdMap(msg.getQryAckMsgBody().getIndex());
                    obj.setRcvType(type);

                    switch (type) {
                        case PBRcvObj.PBRcvType.qryHisMessagesAck:
                            obj = qryHisMsgAckWithImWebsocketMsg(msg);
                            break;
                        case PBRcvObj.PBRcvType.syncConversationsAck:
                            obj = syncConversationsAckWithImWebsocketMsg(msg);
                            break;
                        case PBRcvObj.PBRcvType.syncMessagesAck:
                            obj = syncMsgAckWithImWebsocketMsg(msg);
                            break;
                        case PBRcvObj.PBRcvType.qryReadDetailAck:
                            obj = qryReadDetailAckWithImWebsocketMsg(msg);
                            break;
                        case PBRcvObj.PBRcvType.simpleQryAck:
                            obj = simpleQryAckWithImWebsocketMsg(msg);
                            break;
                        case PBRcvObj.PBRcvType.simpleQryAckCallbackTimestamp:
                            obj = simpleQryAckCallbackTimestampWithImWebsocketMsg(msg);
                            break;
                        case PBRcvObj.PBRcvType.conversationSetTopAck:
                            obj = conversationSetTopAckWithImWebsocketMsg(msg);
                            break;
                        case PBRcvObj.PBRcvType.qryFileCredAck:
                            obj = qryFileCredAckWithImWebsocketMsg(msg);
                            break;
                        case PBRcvObj.PBRcvType.addConversationAck:
                            obj = addConversationAckWithImWebsocketMsg(msg);
                            break;
                        case PBRcvObj.PBRcvType.globalMuteAck:
                            obj = globalMuteAckWithImWebsocketMsg(msg);
                            break;
                        default:
                            break;
                    }
                    break;

                case PUBLISHMSGBODY:
                    if (msg.getPublishMsgBody().getTopic().equals(NTF)) {
                        Appmessages.Notify ntf = Appmessages.Notify.parseFrom(msg.getPublishMsgBody().getData());
                        if (ntf.getType() == Appmessages.NotifyType.Msg) {
                            obj.setRcvType(PBRcvObj.PBRcvType.publishMsgNtf);
                            PBRcvObj.PublishMsgNtf n = new PBRcvObj.PublishMsgNtf();
                            n.syncTime = ntf.getSyncTime();
                            obj.mPublishMsgNtf = n;
                        }
                    } else if (msg.getPublishMsgBody().getTopic().equals(MSG)) {
                        Appmessages.DownMsg downMsg = Appmessages.DownMsg.parseFrom(msg.getPublishMsgBody().getData());
                        PBRcvObj.PublishMsgBody body = new PBRcvObj.PublishMsgBody();
                        body.rcvMessage = messageWithDownMsg(downMsg);
                        body.index = msg.getPublishMsgBody().getIndex();
                        body.qos = msg.getQos();

                        obj.setRcvType(PBRcvObj.PBRcvType.publishMsg);
                        obj.mPublishMsgBody = body;
                    }
                    break;

                case DISCONNECTMSGBODY:
                    obj.setRcvType(PBRcvObj.PBRcvType.disconnectMsg);
                    PBRcvObj.DisconnectMsg m = new PBRcvObj.DisconnectMsg();
                    m.code = msg.getDisconnectMsgBody().getCode();
                    m.timestamp = msg.getDisconnectMsgBody().getTimestamp();
                    m.extra = msg.getDisconnectMsgBody().getExt();
                    obj.mDisconnectMsg = m;
                    break;

            }
        } catch (InvalidProtocolBufferException e) {
            JLogger.e("PB-Parse", "rcvObjWithBytes msg parse error, msgType is " + obj.getRcvType() + ", exception is " + e.getMessage());
            obj.setRcvType(PBRcvObj.PBRcvType.parseError);
        }
        return obj;
    }

    @NonNull
    private PBRcvObj qryHisMsgAckWithImWebsocketMsg(@NonNull Connect.ImWebsocketMsg msg) throws InvalidProtocolBufferException {
        PBRcvObj obj = new PBRcvObj();
        Appmessages.DownMsgSet set = Appmessages.DownMsgSet.parseFrom(msg.getQryAckMsgBody().getData());
        obj.setRcvType(PBRcvObj.PBRcvType.qryHisMessagesAck);
        PBRcvObj.QryHisMsgAck a = new PBRcvObj.QryHisMsgAck(msg.getQryAckMsgBody());
        a.isFinished = set.getIsFinished();
        List<ConcreteMessage> list = new ArrayList<>();
        for (Appmessages.DownMsg downMsg : set.getMsgsList()) {
            ConcreteMessage concreteMessage = messageWithDownMsg(downMsg);
            list.add(concreteMessage);
        }
        a.msgList = list;
        obj.mQryHisMsgAck = a;
        return obj;
    }

    private PBRcvObj syncConversationsAckWithImWebsocketMsg(Connect.ImWebsocketMsg msg) throws InvalidProtocolBufferException {
        PBRcvObj obj = new PBRcvObj();
        Appmessages.QryConversationsResp resp = Appmessages.QryConversationsResp.parseFrom(msg.getQryAckMsgBody().getData());
        obj.setRcvType(PBRcvObj.PBRcvType.syncConversationsAck);
        PBRcvObj.SyncConvAck a = new PBRcvObj.SyncConvAck(msg.getQryAckMsgBody());
        a.isFinished = resp.getIsFinished();
        List<ConcreteConversationInfo> list = new ArrayList<>();
        List<ConcreteConversationInfo> deletedList = new ArrayList<>();
        for (Appmessages.Conversation conversation : resp.getConversationsList()) {
            ConcreteConversationInfo info = conversationInfoWithPBConversation(conversation);
            if (conversation.getIsDelete() > 0) {
                deletedList.add(info);
            } else {
                list.add(info);
            }
        }
        a.convList = list;
        a.deletedConvList = deletedList;
        obj.mSyncConvAck = a;
        return obj;
    }

    private PBRcvObj syncMsgAckWithImWebsocketMsg(Connect.ImWebsocketMsg msg) throws InvalidProtocolBufferException {
        PBRcvObj obj = new PBRcvObj();
        Appmessages.DownMsgSet set = Appmessages.DownMsgSet.parseFrom(msg.getQryAckMsgBody().getData());
        obj.setRcvType(PBRcvObj.PBRcvType.syncMessagesAck);
        //sync 和 query history 共用一个 ack
        PBRcvObj.QryHisMsgAck a = new PBRcvObj.QryHisMsgAck(msg.getQryAckMsgBody());
        a.isFinished = set.getIsFinished();
        List<ConcreteMessage> list = new ArrayList<>();
        for (Appmessages.DownMsg downMsg : set.getMsgsList()) {
            ConcreteMessage concreteMessage = messageWithDownMsg(downMsg);
            list.add(concreteMessage);
        }
        a.msgList = list;
        obj.mQryHisMsgAck = a;
        return obj;
    }

    private PBRcvObj simpleQryAckWithImWebsocketMsg(Connect.ImWebsocketMsg msg) {
        PBRcvObj obj = new PBRcvObj();
        obj.setRcvType(PBRcvObj.PBRcvType.simpleQryAck);
        obj.mSimpleQryAck = new PBRcvObj.SimpleQryAck(msg.getQryAckMsgBody());
        return obj;
    }

    private PBRcvObj simpleQryAckCallbackTimestampWithImWebsocketMsg(Connect.ImWebsocketMsg msg) throws InvalidProtocolBufferException {
        PBRcvObj obj = new PBRcvObj();
        obj.setRcvType(PBRcvObj.PBRcvType.simpleQryAckCallbackTimestamp);
        obj.mSimpleQryAck = new PBRcvObj.SimpleQryAck(msg.getQryAckMsgBody());
        return obj;
    }

    private PBRcvObj conversationSetTopAckWithImWebsocketMsg(Connect.ImWebsocketMsg msg) throws InvalidProtocolBufferException {
        PBRcvObj obj = new PBRcvObj();
        obj.setRcvType(PBRcvObj.PBRcvType.conversationSetTopAck);
        Appmessages.TopConversResp resp = Appmessages.TopConversResp.parseFrom(msg.getQryAckMsgBody().getData());
        PBRcvObj.TimestampQryAck a = new PBRcvObj.TimestampQryAck(msg.getQryAckMsgBody());
        a.operationTime = resp.getOptTime();
        obj.mTimestampQryAck = a;
        return obj;
    }

    private PBRcvObj qryReadDetailAckWithImWebsocketMsg(Connect.ImWebsocketMsg msg) throws InvalidProtocolBufferException {
        PBRcvObj obj = new PBRcvObj();
        Appmessages.QryReadDetailResp resp = Appmessages.QryReadDetailResp.parseFrom(msg.getQryAckMsgBody().getData());
        obj.setRcvType(PBRcvObj.PBRcvType.qryReadDetailAck);
        PBRcvObj.QryReadDetailAck a = new PBRcvObj.QryReadDetailAck(msg.getQryAckMsgBody());
        List<UserInfo> readMembers = new ArrayList<>();
        List<UserInfo> unreadMembers = new ArrayList<>();
        for (Appmessages.MemberReadDetailItem item : resp.getReadMembersList()) {
            UserInfo userInfo = userInfoWithMemberReadDetailItem(item);
            readMembers.add(userInfo);
        }
        for (Appmessages.MemberReadDetailItem item : resp.getUnreadMembersList()) {
            UserInfo userInfo = userInfoWithMemberReadDetailItem(item);
            unreadMembers.add(userInfo);
        }
        a.readMembers = readMembers;
        a.unreadMembers = unreadMembers;
        obj.mQryReadDetailAck = a;
        return obj;
    }

    private PBRcvObj qryFileCredAckWithImWebsocketMsg(Connect.ImWebsocketMsg msg) throws InvalidProtocolBufferException {
        PBRcvObj obj = new PBRcvObj();
        obj.setRcvType(PBRcvObj.PBRcvType.qryFileCredAck);
        Appmessages.QryFileCredResp resp = Appmessages.QryFileCredResp.parseFrom(msg.getQryAckMsgBody().getData());
        PBRcvObj.QryFileCredAck a = new PBRcvObj.QryFileCredAck(msg.getQryAckMsgBody());
        a.ossType = UploadOssType.setValue(resp.getOssType().getNumber());
        if (resp.getQiNiuCred() != null) {
            UploadQiNiuCred qiNiuCred = new UploadQiNiuCred();
            qiNiuCred.setDomain(resp.getQiNiuCred().getDomain());
            qiNiuCred.setToken(resp.getQiNiuCred().getToken());
            a.qiNiuCred = qiNiuCred;
        }
        if (resp.getPreSignResp() != null) {
            UploadPreSignCred preSignCred = new UploadPreSignCred();
            preSignCred.setUrl(resp.getPreSignResp().getUrl());
            a.preSignCred = preSignCred;
        }
        obj.mQryFileCredAck = a;
        return obj;
    }

    private PBRcvObj addConversationAckWithImWebsocketMsg(Connect.ImWebsocketMsg msg) throws InvalidProtocolBufferException {
        PBRcvObj obj = new PBRcvObj();
        obj.setRcvType(PBRcvObj.PBRcvType.addConversationAck);
        Appmessages.Conversation resp = Appmessages.Conversation.parseFrom(msg.getQryAckMsgBody().getData());
        PBRcvObj.ConversationInfoAck a = new PBRcvObj.ConversationInfoAck(msg.getQryAckMsgBody());
        a.conversationInfo = conversationInfoWithPBConversation(resp);
        obj.mConversationInfoAck = a;
        return obj;
    }

    private PBRcvObj globalMuteAckWithImWebsocketMsg(Connect.ImWebsocketMsg msg) throws InvalidProtocolBufferException {
        PBRcvObj obj = new PBRcvObj();
        obj.setRcvType(PBRcvObj.PBRcvType.globalMuteAck);
        Appmessages.UserUndisturb resp = Appmessages.UserUndisturb.parseFrom(msg.getQryAckMsgBody().getData());
        PBRcvObj.GlobalMuteAck a = new PBRcvObj.GlobalMuteAck(msg.getQryAckMsgBody());
        a.isMute = resp.getSwitch();
        a.timezone = resp.getTimezone();
        List<TimePeriod> periods = new ArrayList<>();
        for (Appmessages.UserUndisturbItem item : resp.getRulesList()) {
            TimePeriod p = new TimePeriod();
            p.setStartTime(item.getStart());
            p.setEndTime(item.getEnd());
            periods.add(p);
        }
        a.periods = periods;
        obj.mGlobalMuteAck = a;
        return obj;
    }

    private Connect.ImWebsocketMsg createImWebsocketMsgWithPublishMsg(Connect.PublishMsgBody publishMsgBody) {
        return Connect.ImWebsocketMsg.newBuilder()
                .setVersion(PROTOCOL_VERSION)
                .setCmd(CmdType.publish)
                .setQos(Qos.yes)
                .setPublishMsgBody(publishMsgBody)
                .build();
    }

    private Connect.ImWebsocketMsg createImWebsocketMsgWithQueryMsg(Connect.QueryMsgBody body) {
        return Connect.ImWebsocketMsg.newBuilder()
                .setVersion(PROTOCOL_VERSION)
                .setCmd(CmdType.query)
                .setQos(Qos.yes)
                .setQryMsgBody(body)
                .build();
    }

    private ConcreteMessage messageWithDownMsg(Appmessages.DownMsg downMsg) {
        ConcreteMessage message = new ConcreteMessage();
        Conversation.ConversationType type = conversationTypeFromChannelType(downMsg.getChannelType());
        Conversation conversation = new Conversation(type, downMsg.getTargetId());
        message.setConversation(conversation);
        message.setContentType(downMsg.getMsgType());
        message.setMessageId(downMsg.getMsgId());
        message.setClientUid(downMsg.getClientUid());
        message.setDirection(downMsg.getIsSend() ? Message.MessageDirection.SEND : Message.MessageDirection.RECEIVE);
        message.setHasRead(downMsg.getIsRead());
        message.setState(Message.MessageState.SENT);
        message.setTimestamp(downMsg.getMsgTime());
        message.setSenderUserId(downMsg.getSenderId());
        message.setSeqNo(downMsg.getMsgSeqNo());
        message.setMsgIndex(downMsg.getUnreadIndex());
        MessageContent messageContent = ContentTypeCenter.getInstance().getContent(downMsg.getMsgContent().toByteArray(), downMsg.getMsgType());
        if (messageContent != null) {
            if (messageContent instanceof MergeMessage) {
                if (TextUtils.isEmpty(((MergeMessage) messageContent).getContainerMsgId())) {
                    ((MergeMessage) messageContent).setContainerMsgId(message.getMessageId());
                }
            }
        }
        message.setFlags(downMsg.getFlags());
        GroupMessageReadInfo info = new GroupMessageReadInfo();
        info.setReadCount(downMsg.getReadCount());
        info.setMemberCount(downMsg.getMemberCount());
        message.setGroupMessageReadInfo(info);
        message.setGroupInfo(groupInfoWithPBGroupInfo(downMsg.getGroupInfo()));
        message.setTargetUserInfo(userInfoWithPBUserInfo(downMsg.getTargetUserInfo()));
        if (downMsg.hasMentionInfo() && Appmessages.MentionType.MentionDefault != downMsg.getMentionInfo().getMentionType()) {
            MessageMentionInfo mentionInfo = new MessageMentionInfo();
            mentionInfo.setType(mentionTypeFromPbMentionType(downMsg.getMentionInfo().getMentionType()));
            List<UserInfo> mentionUserList = new ArrayList<>();
            for (Appmessages.UserInfo pbUserInfo : downMsg.getMentionInfo().getTargetUsersList()) {
                UserInfo user = userInfoWithPBUserInfo(pbUserInfo);
                if (user != null) {
                    mentionUserList.add(user);
                }
            }
            mentionInfo.setTargetUsers(mentionUserList);
            message.setMentionInfo(mentionInfo);
        }
        if (downMsg.hasReferMsg()) {
            ConcreteMessage referMsg = messageWithDownMsg(downMsg.getReferMsg());
            message.setReferredMessage(referMsg);
        }
        message.setContent(messageContent);
        return message;
    }

    private Appmessages.DownMsg downMsgWithMessage(ConcreteMessage message) {
        if (message.getContent() == null) return null;
        if (message.getConversation() == null) return null;

        Appmessages.DownMsg.Builder downMsgBuilder = Appmessages.DownMsg.newBuilder()
                .setTargetId(message.getConversation().getConversationId())
                .setChannelType(channelTypeFromConversationType(message.getConversation().getConversationType()))
                .setMsgType(message.getContentType())
                .setSenderId(message.getSenderUserId())
                .setMsgId(message.getMessageId())
                .setMsgSeqNo(message.getSeqNo())
                .setMsgContent(ByteString.copyFrom(message.getContent().encode()))
                .setMsgTime(message.getTimestamp())
                .setFlags(message.getFlags())
                .setIsSend(Message.MessageDirection.SEND == message.getDirection())
//                .setPlatform("")
                .setClientUid(message.getClientUid())
//                .setPushData()
                .setIsRead(message.isHasRead())
//                .setMergedMsgs()
//                .setUndisturbType()
                .setUnreadIndex(message.getMsgIndex());

        if (message.getGroupMessageReadInfo() != null) {
            downMsgBuilder
                    .setReadCount(message.getGroupMessageReadInfo().getReadCount())
                    .setMemberCount(message.getGroupMessageReadInfo().getMemberCount());
        }
        if (message.getGroupInfo() != null) {
            downMsgBuilder
                    .setGroupInfo(pbGroupInfoWithGroupInfo(message.getGroupInfo()));
        }
        if (message.getTargetUserInfo() != null) {
            downMsgBuilder
                    .setTargetUserInfo(pbUserInfoWithUserInfo(message.getTargetUserInfo()));
        }
        if (message.hasMentionInfo()) {
            Appmessages.MentionInfo.Builder pbMentionInfo = Appmessages.MentionInfo.newBuilder()
                    .setMentionType(pbMentionTypeFromMentionType(message.getMentionInfo().getType()));
            if (message.getMentionInfo().getTargetUsers() != null) {
                for (UserInfo targetUser : message.getMentionInfo().getTargetUsers()) {
                    pbMentionInfo.addTargetUsers(pbUserInfoWithUserInfo(targetUser));
                }
            }
            downMsgBuilder
                    .setMentionInfo(pbMentionInfo.build());
        }
        if (message.getReferredMessage() != null) {
            downMsgBuilder
                    .setReferMsg(downMsgWithMessage((ConcreteMessage) message.getReferredMessage()));
        }
        return downMsgBuilder.build();
    }

    private ConcreteConversationInfo conversationInfoWithPBConversation(Appmessages.Conversation conversation) {
        ConcreteConversationInfo info = new ConcreteConversationInfo();
        Conversation c = new Conversation(conversationTypeFromChannelType(conversation.getChannelType()), conversation.getTargetId());
        info.setConversation(c);
        info.setUnreadCount((int) conversation.getUnreadCount());
        info.setSortTime(conversation.getSortTime());
        info.setLastMessage(messageWithDownMsg(conversation.getMsg()));
        info.setLastReadMessageIndex(conversation.getLatestReadIndex());
        info.setLastMessageIndex(conversation.getLatestUnreadIndex());
        info.setSyncTime(conversation.getSyncTime());
        info.setMute(conversation.getUndisturbType() == 1);
        info.setTop(conversation.getIsTop() == 1);
        info.setTopTime(conversation.getTopUpdatedTime());
        info.setGroupInfo(groupInfoWithPBGroupInfo(conversation.getGroupInfo()));
        info.setTargetUserInfo(userInfoWithPBUserInfo(conversation.getTargetUserInfo()));
        if (conversation.getMentions() != null && conversation.getMentions().getIsMentioned()) {
            ConversationMentionInfo mentionInfo = new ConversationMentionInfo();
            //解析@消息列表
            if (conversation.getMentions().getMentionMsgsList() != null) {
                List<ConversationMentionInfo.MentionMsg> mentionMsgList = new ArrayList<>();
                for (Appmessages.MentionMsg pbMentionMsg : conversation.getMentions().getMentionMsgsList()) {
                    ConversationMentionInfo.MentionMsg mentionMsg = mentionMsgWithPBMentionMsg(pbMentionMsg);
                    if (mentionMsg != null) {
                        mentionMsgList.add(mentionMsg);
                    }
                }
                mentionInfo.setMentionMsgList(mentionMsgList);
            }
            info.setMentionInfo(mentionInfo);
            //解析用户信息列表
            if (conversation.getMentions().getSendersList() != null) {
                List<UserInfo> mentionUserList = new ArrayList<>();
                for (Appmessages.UserInfo pbUserInfo : conversation.getMentions().getSendersList()) {
                    UserInfo user = userInfoWithPBUserInfo(pbUserInfo);
                    if (user != null) {
                        mentionUserList.add(user);
                    }
                }
                info.setMentionUserList(mentionUserList);
            }
        }
        return info;
    }

    private UserInfo userInfoWithMemberReadDetailItem(Appmessages.MemberReadDetailItem item) {
        UserInfo userInfo = new UserInfo();
        userInfo.setUserId(item.getMember().getUserId());
        userInfo.setUserName(item.getMember().getNickname());
        userInfo.setPortrait(item.getMember().getUserPortrait());
        if (item.getMember().getExtFieldsCount() > 0) {
            Map<String, String> extra = new HashMap<>();
            for (Appmessages.KvItem it : item.getMember().getExtFieldsList()) {
                extra.put(it.getKey(), it.getValue());
            }
            userInfo.setExtra(extra);
        }
        return userInfo;
    }

    private UserInfo userInfoWithPBUserInfo(Appmessages.UserInfo pbUserInfo) {
        if (pbUserInfo == null) {
            return null;
        }
        UserInfo result = new UserInfo();
        result.setUserId(pbUserInfo.getUserId());
        result.setUserName(pbUserInfo.getNickname());
        result.setPortrait(pbUserInfo.getUserPortrait());
        if (pbUserInfo.getExtFieldsCount() > 0) {
            Map<String, String> extra = new HashMap<>();
            for (Appmessages.KvItem item : pbUserInfo.getExtFieldsList()) {
                extra.put(item.getKey(), item.getValue());
            }
            result.setExtra(extra);
        }
        return result;
    }

    private Appmessages.UserInfo pbUserInfoWithUserInfo(UserInfo userInfo) {
        if (userInfo == null) {
            return null;
        }
        Appmessages.UserInfo.Builder pbUserInfoBuilder = Appmessages.UserInfo.newBuilder()
                .setUserId(userInfo.getUserId())
                .setNickname(userInfo.getUserName())
                .setUserPortrait(userInfo.getPortrait());
        if (userInfo.getExtra() != null) {
            for (Map.Entry<String, String> entry : userInfo.getExtra().entrySet()) {
                if (entry.getKey() == null || entry.getValue() == null) continue;
                Appmessages.KvItem kvItem = Appmessages.KvItem.newBuilder()
                        .setKey(entry.getKey())
                        .setValue(entry.getValue())
                        .build();
                pbUserInfoBuilder.addExtFields(kvItem);
            }
        }
        return pbUserInfoBuilder.build();
    }

    private GroupInfo groupInfoWithPBGroupInfo(Appmessages.GroupInfo pbGroupInfo) {
        if (pbGroupInfo == null) {
            return null;
        }
        GroupInfo result = new GroupInfo();
        result.setGroupId(pbGroupInfo.getGroupId());
        result.setGroupName(pbGroupInfo.getGroupName());
        result.setPortrait(pbGroupInfo.getGroupPortrait());
        if (pbGroupInfo.getExtFieldsCount() > 0) {
            Map<String, String> extra = new HashMap<>();
            for (Appmessages.KvItem item : pbGroupInfo.getExtFieldsList()) {
                extra.put(item.getKey(), item.getValue());
            }
            result.setExtra(extra);
        }
        return result;
    }

    private Appmessages.GroupInfo pbGroupInfoWithGroupInfo(GroupInfo groupInfo) {
        if (groupInfo == null) {
            return null;
        }
        Appmessages.GroupInfo.Builder pbGroupInfoBuilder = Appmessages.GroupInfo.newBuilder()
                .setGroupId(groupInfo.getGroupId())
                .setGroupName(groupInfo.getGroupName())
                .setGroupPortrait(groupInfo.getPortrait());

        if (groupInfo.getExtra() != null) {
            for (Map.Entry<String, String> entry : groupInfo.getExtra().entrySet()) {
                if (entry.getKey() == null || entry.getValue() == null) continue;
                Appmessages.KvItem kvItem = Appmessages.KvItem.newBuilder()
                        .setKey(entry.getKey())
                        .setValue(entry.getValue())
                        .build();
                pbGroupInfoBuilder.addExtFields(kvItem);
            }
        }
        return pbGroupInfoBuilder.build();
    }

    private ConversationMentionInfo.MentionMsg mentionMsgWithPBMentionMsg(Appmessages.MentionMsg pbMentionMsg) {
        if (pbMentionMsg == null) {
            return null;
        }
        ConversationMentionInfo.MentionMsg result = new ConversationMentionInfo.MentionMsg();
        result.setSenderId(pbMentionMsg.getSenderId());
        result.setMsgId(pbMentionMsg.getMsgId());
        result.setMsgTime(pbMentionMsg.getMsgTime());
        return result;
    }

    private Conversation.ConversationType conversationTypeFromChannelType(Appmessages.ChannelType channelType) {
        Conversation.ConversationType result = Conversation.ConversationType.UNKNOWN;
        switch (channelType) {
            case Private:
                result = Conversation.ConversationType.PRIVATE;
                break;
            case Group:
                result = Conversation.ConversationType.GROUP;
                break;
            case Chatroom:
                result = Conversation.ConversationType.CHATROOM;
                break;
            case System:
                result = Conversation.ConversationType.SYSTEM;
                break;
            default:
                break;
        }
        return result;
    }

    private Appmessages.ChannelType channelTypeFromConversationType(Conversation.ConversationType conversationType) {
        Appmessages.ChannelType result = Appmessages.ChannelType.Unknown;
        switch (conversationType) {
            case PRIVATE:
                result = Appmessages.ChannelType.Private;
                break;
            case GROUP:
                result = Appmessages.ChannelType.Group;
                break;
            case CHATROOM:
                result = Appmessages.ChannelType.Chatroom;
                break;
            case SYSTEM:
                result = Appmessages.ChannelType.System;
                break;
            default:
                break;
        }
        return result;
    }

    private MessageMentionInfo.MentionType mentionTypeFromPbMentionType(Appmessages.MentionType pbMentionType) {
        MessageMentionInfo.MentionType type = MessageMentionInfo.MentionType.DEFAULT;
        switch (pbMentionType) {
            case All:
                type = MessageMentionInfo.MentionType.ALL;
                break;
            case Someone:
                type = MessageMentionInfo.MentionType.SOMEONE;
                break;
            case AllAndSomeone:
                type = MessageMentionInfo.MentionType.ALL_AND_SOMEONE;
                break;
            default:
                break;
        }
        return type;
    }

    private Appmessages.MentionType pbMentionTypeFromMentionType(MessageMentionInfo.MentionType mentionType) {
        Appmessages.MentionType type = Appmessages.MentionType.MentionDefault;
        switch (mentionType) {
            case ALL:
                type = Appmessages.MentionType.All;
                break;
            case SOMEONE:
                type = Appmessages.MentionType.Someone;
                break;
            case ALL_AND_SOMEONE:
                type = Appmessages.MentionType.AllAndSomeone;
                break;
            default:
                break;
        }
        return type;
    }

    private Appmessages.Conversation.Builder pbConversationFromConversation(Conversation conversation) {
        return Appmessages.Conversation.newBuilder()
                .setTargetId(conversation.getConversationId())
                .setChannelTypeValue(conversation.getConversationType().getValue());
    }

    private int getTypeInCmdMap(Integer index) {
        String cachedCmd = mMsgCmdMap.remove(index);
        if (TextUtils.isEmpty(cachedCmd)) {
            JLogger.w("PB-Match", "rcvObjWithBytes ack can't match a cached cmd");
            return PBRcvObj.PBRcvType.cmdMatchError;
        }
        Integer type = sCmdAckMap.get(cachedCmd);
        if (type == null) {
            JLogger.w("PB-Match", "rcvObjWithBytes ack cmd match error, cmd is " + cachedCmd);
            return PBRcvObj.PBRcvType.cmdMatchError;
        }
        return type;
    }

    private static class CmdType {
        private static final int connect = 0;
        private static final int connectAck = 1;
        private static final int disconnect = 2;
        private static final int publish = 3;
        private static final int publishAck = 4;
        private static final int query = 5;
        private static final int queryAck = 6;
        private static final int queryConfirm = 7;
        private static final int ping = 8;
        private static final int pong = 9;
    }

    private static class Qos {
        private static final int no = 0;
        private static final int yes = 1;
    }

    private static final String PROTO_ID = "1";
    private static final int PROTOCOL_VERSION = 1;
    private static final String SDK_VERSION = "1.0.0";
    private static final int PLATFORM_ANDROID = 1;
    private static final String QRY_HIS_MSG = "qry_hismsgs";
    private static final String QRY_HISMSG_BY_IDS = "qry_hismsg_by_ids";
    private static final String SYNC_CONV = "sync_convers";
    private static final String SYNC_MSG = "sync_msgs";
    private static final String MARK_READ = "mark_read";
    private static final String RECALL_MSG = "recall_msg";
    private static final String DEL_CONV = "del_convers";
    private static final String CLEAR_UNREAD = "clear_unread";
    private static final String CLEAR_TOTAL_UNREAD = "clear_total_unread";
    private static final String QRY_READ_DETAIL = "qry_read_detail";
    private static final String UNDISTURB_CONVERS = "undisturb_convers";
    private static final String TOP_CONVERS = "top_convers";
    private static final String QRY_MERGED_MSGS = "qry_merged_msgs";
    private static final String REG_PUSH_TOKEN = "reg_push_token";
    private static final String QRY_MENTION_MSGS = "qry_mention_msgs";
    private static final String CLEAR_HIS_MSG = "clean_hismsg";
    private static final String DELETE_MSG = "del_msg";
    private static final String QRY_FILE_CRED = "file_cred";
    private static final String ADD_CONVERSATION = "add_conver";
    private static final String SET_USER_UNDISTURB = "set_user_undisturb";
    private static final String GET_USER_UNDISTURB = "get_user_undisturb";
    private static final String P_MSG = "p_msg";
    private static final String G_MSG = "g_msg";
    private static final String C_MSG = "c_msg";
    private static final String NTF = "ntf";
    private static final String MSG = "msg";
    private static final HashMap<String, Integer> sCmdAckMap = new HashMap<String, Integer>() {
        {
            put(QRY_HIS_MSG, PBRcvObj.PBRcvType.qryHisMessagesAck);
            put(SYNC_CONV, PBRcvObj.PBRcvType.syncConversationsAck);
            put(SYNC_MSG, PBRcvObj.PBRcvType.syncMessagesAck);
            put(P_MSG, PBRcvObj.PBRcvType.publishMsgAck);
            put(G_MSG, PBRcvObj.PBRcvType.publishMsgAck);
            put(C_MSG, PBRcvObj.PBRcvType.publishMsgAck);
            put(RECALL_MSG, PBRcvObj.PBRcvType.simpleQryAckCallbackTimestamp);
            put(DEL_CONV, PBRcvObj.PBRcvType.simpleQryAckCallbackTimestamp);
            put(CLEAR_UNREAD, PBRcvObj.PBRcvType.simpleQryAckCallbackTimestamp);
            put(CLEAR_TOTAL_UNREAD, PBRcvObj.PBRcvType.simpleQryAckCallbackTimestamp);
            put(MARK_READ, PBRcvObj.PBRcvType.simpleQryAckCallbackTimestamp);
            put(QRY_READ_DETAIL, PBRcvObj.PBRcvType.qryReadDetailAck);
            put(QRY_HISMSG_BY_IDS, PBRcvObj.PBRcvType.qryHisMessagesAck);
            put(UNDISTURB_CONVERS, PBRcvObj.PBRcvType.simpleQryAckCallbackTimestamp);
            put(TOP_CONVERS, PBRcvObj.PBRcvType.simpleQryAckCallbackTimestamp);
            put(QRY_MERGED_MSGS, PBRcvObj.PBRcvType.qryHisMessagesAck);
            put(REG_PUSH_TOKEN, PBRcvObj.PBRcvType.simpleQryAck);
            put(QRY_MENTION_MSGS, PBRcvObj.PBRcvType.qryHisMessagesAck);
            put(CLEAR_HIS_MSG, PBRcvObj.PBRcvType.simpleQryAckCallbackTimestamp);
            put(DELETE_MSG, PBRcvObj.PBRcvType.simpleQryAckCallbackTimestamp);
            put(QRY_FILE_CRED, PBRcvObj.PBRcvType.qryFileCredAck);
            put(ADD_CONVERSATION, PBRcvObj.PBRcvType.addConversationAck);
            put(SET_USER_UNDISTURB, PBRcvObj.PBRcvType.simpleQryAckCallbackTimestamp);
            put(GET_USER_UNDISTURB, PBRcvObj.PBRcvType.globalMuteAck);
        }
    };

    private final ConcurrentHashMap<Integer, String> mMsgCmdMap = new ConcurrentHashMap<>();

}