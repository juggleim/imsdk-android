package com.juggle.im.internal.core.network;

import static com.juggle.im.internal.ConstInternal.SDK_VERSION;

import static app_messages.Rtcroom.RtcState.RtcConnected;

import android.text.TextUtils;

import androidx.annotation.NonNull;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import com.juggle.im.JIMConst;
import com.juggle.im.call.CallConst;
import com.juggle.im.call.internal.model.RtcRoom;
import com.juggle.im.call.model.CallMember;
import com.juggle.im.interfaces.GroupMember;
import com.juggle.im.internal.ContentTypeCenter;
import com.juggle.im.internal.model.ChatroomAttributeItem;
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
import com.juggle.im.model.MessageReaction;
import com.juggle.im.model.MessageReactionItem;
import com.juggle.im.model.PushData;
import com.juggle.im.model.TimePeriod;
import com.juggle.im.model.UserInfo;
import com.juggle.im.model.messages.MergeMessage;
import com.juggle.im.push.PushChannel;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import app_messages.Appmessages;
import app_messages.Chatroom;
import app_messages.Connect;
import app_messages.Pushtoken;
import app_messages.Rtcroom;

class PBData {
    void resetDataConverter() {
        mConverter = new SimpleDataConverter();
    }

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
                       String clientIp,
                       String language) {
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
                case OPPO:
                    builder.setPushChannel("Oppo");
                    break;
                case HONOR:
                    builder.setPushChannel("");
                    break;
                case VIVO:
                    builder.setPushChannel("Vivo");
                    break;
                case MEIZU:
                    builder.setPushChannel("");
                    break;
                case GOOGLE:
                    builder.setPushChannel("FCM");
                    break;
                case JIGUANG:
                    builder.setPushChannel("Jpush");
                    break;
            }
        }
//        builder.setLanguage(language);
        Connect.ConnectMsgBody body = builder.build();
        byte[] payload = mConverter.encode(body.toByteArray());

        Connect.ImWebsocketMsg msg = Connect.ImWebsocketMsg.newBuilder()
                .setVersion(PROTOCOL_VERSION)
                .setCmd(CmdType.connect)
                .setQos(Qos.yes)
                .setPayload(ByteString.copyFrom(payload))
                .build();
        return msg.toByteArray();
    }

    byte[] disconnectData(boolean receivePush) {
        int code = receivePush ? 0 : 11012;
        Connect.DisconnectMsgBody body = Connect.DisconnectMsgBody.newBuilder()
                .setCode(code)
                .setTimestamp(System.currentTimeMillis())
                .build();
        byte[] payload = mConverter.encode(body.toByteArray());
        Connect.ImWebsocketMsg msg = Connect.ImWebsocketMsg.newBuilder()
                .setVersion(PROTOCOL_VERSION)
                .setCmd(CmdType.disconnect)
                .setQos(Qos.no)
                .setPayload(ByteString.copyFrom(payload))
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
                           ConcreteMessage referMsg,
                           PushData pushData) {
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
        if (pushData != null) {
            Appmessages.PushData.Builder pbPushData = Appmessages.PushData.newBuilder();
            if (pushData.getContent() != null) {
                pbPushData.setPushText(pushData.getContent());
            }
            if (pushData.getExtra() != null) {
                pbPushData.setPushExtraData(pushData.getExtra());
            }
            upMsgBuilder.setPushData(pbPushData);
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

    byte[] updateMessageData(String messageId, String contentType, byte[] msgData, Conversation conversation, long timestamp, long msgSeqNo, int index) {
        Appmessages.ModifyMsgReq req = Appmessages.ModifyMsgReq.newBuilder()
                .setMsgId(messageId)
                .setTargetId(conversation.getConversationId())
                .setChannelTypeValue(conversation.getConversationType().getValue())
                .setMsgTime(timestamp)
                .setMsgSeqNo(msgSeqNo)
                .setMsgContent(ByteString.copyFrom(msgData))
                .setMsgType(contentType)
                .build();
        Connect.QueryMsgBody body = Connect.QueryMsgBody.newBuilder()
                .setIndex(index)
                .setTopic(MODIFY_MSG)
                .setTargetId(conversation.getConversationId())
                .setData(req.toByteString())
                .build();
        mMsgCmdMap.put(index, MODIFY_MSG);

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

    byte[] clearUnreadCountData(Conversation conversation, String userId, long msgIndex, String msgId, long timestamp, int index) {
        Appmessages.Conversation.Builder builder = pbConversationFromConversation(conversation);
        builder.setLatestReadIndex(msgIndex);
        builder.setLatestReadMsgId(msgId);
        builder.setLatestReadMsgTime(timestamp);
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

    byte[] queryHisMsgData(Conversation conversation, long startTime, int count, JIMConst.PullDirection direction, List<String> contentTypes, int index) {
        int order = direction == JIMConst.PullDirection.OLDER ? 0 : 1;
        Appmessages.QryHisMsgsReq.Builder builder = Appmessages.QryHisMsgsReq.newBuilder();
        builder.setTargetId(conversation.getConversationId())
                .setChannelTypeValue(conversation.getConversationType().getValue())
                .setStartTime(startTime)
                .setCount(count)
                .setOrder(order);
        if (contentTypes != null && !contentTypes.isEmpty()) {
            builder.addAllMsgTypes(contentTypes);
        }
        Appmessages.QryHisMsgsReq req = builder.build();

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

    byte[] markUnread(Conversation conversation, String userId, int index) {
        Appmessages.Conversation pbConversation = Appmessages.Conversation.newBuilder()
                .setChannelTypeValue(conversation.getConversationType().getValue())
                .setTargetId(conversation.getConversationId())
                .setUnreadTag(1)
                .build();
        Appmessages.ConversationsReq req = Appmessages.ConversationsReq.newBuilder()
                .addConversations(pbConversation)
                .build();
        Connect.QueryMsgBody body = Connect.QueryMsgBody.newBuilder()
                .setIndex(index)
                .setTopic(MARK_UNREAD)
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
                              long lastReadIndex,
                              int index) {
        int order = direction == JIMConst.PullDirection.OLDER ? 0 : 1;
        Appmessages.QryMentionMsgsReq req = Appmessages.QryMentionMsgsReq.newBuilder()
                .setTargetId(conversation.getConversationId())
                .setChannelTypeValue(conversation.getConversationType().getValue())
                .setStartTime(timestamp)
                .setCount(count)
                .setOrder(order)
                .setLatestReadIndex(lastReadIndex)
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

    byte[] deleteMessage(Conversation conversation, List<ConcreteMessage> msgList, boolean forAllUsers, int index) {
        int scope = forAllUsers ? 1 : 0;
        Appmessages.DelHisMsgsReq.Builder builder = Appmessages.DelHisMsgsReq.newBuilder()
                .setTargetId(conversation.getConversationId())
                .setChannelTypeValue(conversation.getConversationType().getValue())
                .setDelScope(scope);
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

    byte[] joinChatroom(String chatroomId, boolean isAutoCreate, int index) {
        Chatroom.ChatroomReq req = Chatroom.ChatroomReq.newBuilder()
                .setChatId(chatroomId)
                .setIsAutoCreate(isAutoCreate)
                .build();

        Connect.QueryMsgBody body = Connect.QueryMsgBody.newBuilder()
                .setIndex(index)
                .setTopic(C_JOIN)
                .setTargetId(chatroomId)
                .setData(req.toByteString())
                .build();
        mMsgCmdMap.put(index, body.getTopic());
        Connect.ImWebsocketMsg m = createImWebsocketMsgWithQueryMsg(body);
        return m.toByteArray();
    }

    byte[] quitChatroom(String chatroomId, int index) {
        Chatroom.ChatroomReq req = Chatroom.ChatroomReq.newBuilder()
                .setChatId(chatroomId)
                .build();

        Connect.QueryMsgBody body = Connect.QueryMsgBody.newBuilder()
                .setIndex(index)
                .setTopic(C_QUIT)
                .setTargetId(chatroomId)
                .setData(req.toByteString())
                .build();
        mMsgCmdMap.put(index, body.getTopic());
        Connect.ImWebsocketMsg m = createImWebsocketMsgWithQueryMsg(body);
        return m.toByteArray();
    }

    byte[] callInvite(String callId, boolean isMultiCall, List<String> userIdList, int engineType, int index) {
        Rtcroom.RtcInviteReq.Builder builder = Rtcroom.RtcInviteReq.newBuilder();
        if (isMultiCall) {
            builder.setRoomType(Rtcroom.RtcRoomType.OneMore);
        } else {
            builder.setRoomType(Rtcroom.RtcRoomType.OneOne);
        }
        builder.addAllTargetIds(userIdList);
        builder.setRoomId(callId);
        builder.setRtcChannelValue(engineType);
        Rtcroom.RtcInviteReq req = builder.build();

        Connect.QueryMsgBody body = Connect.QueryMsgBody.newBuilder()
                .setIndex(index)
                .setTopic(RTC_INVITE)
                .setTargetId(callId)
                .setData(req.toByteString())
                .build();
        mMsgCmdMap.put(index, body.getTopic());
        Connect.ImWebsocketMsg m = createImWebsocketMsgWithQueryMsg(body);
        return m.toByteArray();
    }

    byte[] callHangup(String callId, int index) {
        Connect.QueryMsgBody body = Connect.QueryMsgBody.newBuilder()
                .setIndex(index)
                .setTopic(RTC_HANGUP)
                .setTargetId(callId)
                .build();
        mMsgCmdMap.put(index, body.getTopic());
        Connect.ImWebsocketMsg m = createImWebsocketMsgWithQueryMsg(body);
        return m.toByteArray();
    }

    byte[] callAccept(String callId, int index) {
        Connect.QueryMsgBody body = Connect.QueryMsgBody.newBuilder()
                .setIndex(index)
                .setTopic(RTC_ACCEPT)
                .setTargetId(callId)
                .build();
        mMsgCmdMap.put(index, body.getTopic());
        Connect.ImWebsocketMsg m = createImWebsocketMsgWithQueryMsg(body);
        return m.toByteArray();
    }

    byte[] callConnected(String callId, int index) {
        Rtcroom.RtcMember member = Rtcroom.RtcMember.newBuilder()
                .setRtcState(RtcConnected)
                .build();

        Connect.QueryMsgBody body = Connect.QueryMsgBody.newBuilder()
                .setIndex(index)
                .setTopic(RTC_UPD_STATE)
                .setTargetId(callId)
                .setData(member.toByteString())
                .build();
        mMsgCmdMap.put(index, body.getTopic());
        Connect.ImWebsocketMsg m = createImWebsocketMsgWithQueryMsg(body);
        return m.toByteArray();
    }

    byte[] queryCallRooms(String userId, int index) {
        Connect.QueryMsgBody body = Connect.QueryMsgBody.newBuilder()
                .setIndex(index)
                .setTopic(RTC_MEMBER_ROOMS)
                .setTargetId(userId)
                .build();
        mMsgCmdMap.put(index, body.getTopic());
        Connect.ImWebsocketMsg m = createImWebsocketMsgWithQueryMsg(body);
        return m.toByteArray();
    }

    byte[] queryCallRoom(String roomId, int index) {
        Connect.QueryMsgBody body = Connect.QueryMsgBody.newBuilder()
                .setIndex(index)
                .setTopic(RTC_QRY)
                .setTargetId(roomId)
                .build();
        mMsgCmdMap.put(index, body.getTopic());
        Connect.ImWebsocketMsg m = createImWebsocketMsgWithQueryMsg(body);
        return m.toByteArray();
    }

    byte[] rtcPingData(String callId, int index) {
        Connect.QueryMsgBody body = Connect.QueryMsgBody.newBuilder()
                .setIndex(index)
                .setTopic(RTC_PING)
                .setTargetId(callId)
                .build();
        mMsgCmdMap.put(index, body.getTopic());
        Connect.ImWebsocketMsg m = createImWebsocketMsgWithQueryMsg(body);
        return m.toByteArray();
    }

    byte[] syncChatroomMessages(String chatroomId, String userId, long syncTime, int prevMessageCount, int index) {
        Chatroom.SyncChatroomReq req = Chatroom.SyncChatroomReq.newBuilder()
                .setChatroomId(chatroomId)
                .setSyncTime(syncTime)
                .setCount(prevMessageCount)
                .build();
        Connect.QueryMsgBody body = Connect.QueryMsgBody.newBuilder()
                .setIndex(index)
                .setTopic(SYNC_CHATROOM_MESSAGE)
                .setTargetId(userId)
                .setData(req.toByteString())
                .build();
        mMsgCmdMap.put(index, body.getTopic());
        Connect.ImWebsocketMsg m = createImWebsocketMsgWithQueryMsg(body);
        return m.toByteArray();
    }

    byte[] syncChatroomAttributes(String chatroomId, String userId, long syncTime, int index) {
        Chatroom.SyncChatroomReq req = Chatroom.SyncChatroomReq.newBuilder()
                .setChatroomId(chatroomId)
                .setSyncTime(syncTime)
                .build();
        Connect.QueryMsgBody body = Connect.QueryMsgBody.newBuilder()
                .setIndex(index)
                .setTopic(SYNC_CHATROOM_ATTS)
                .setTargetId(userId)
                .setData(req.toByteString())
                .build();
        mMsgCmdMap.put(index, body.getTopic());
        Connect.ImWebsocketMsg m = createImWebsocketMsgWithQueryMsg(body);
        return m.toByteArray();
    }

    byte[] setAttributes(String chatroomId, Map<String, String> attributes, int index) {
        Chatroom.ChatAttBatchReq.Builder builder = Chatroom.ChatAttBatchReq.newBuilder();
        for (Map.Entry<String, String> entry : attributes.entrySet()) {
            Chatroom.ChatAttReq req = Chatroom.ChatAttReq.newBuilder()
                    .setKey(entry.getKey())
                    .setValue(entry.getValue())
                    .setIsForce(false)
                    .build();
            builder.addAtts(req);
        }
        Connect.QueryMsgBody body = Connect.QueryMsgBody.newBuilder()
                .setIndex(index)
                .setTopic(BATCH_ADD_ATT)
                .setTargetId(chatroomId)
                .setData(builder.build().toByteString())
                .build();
        mMsgCmdMap.put(index, body.getTopic());
        Connect.ImWebsocketMsg m = createImWebsocketMsgWithQueryMsg(body);
        return m.toByteArray();
    }

    byte[] removeAttributes(String chatroomId, List<String> keys, int index) {
        Chatroom.ChatAttBatchReq.Builder builder = Chatroom.ChatAttBatchReq.newBuilder();
        for (String key : keys) {
            Chatroom.ChatAttReq req = Chatroom.ChatAttReq.newBuilder()
                    .setKey(key)
                    .setIsForce(false)
                    .build();
            builder.addAtts(req);
        }
        Connect.QueryMsgBody body = Connect.QueryMsgBody.newBuilder()
                .setIndex(index)
                .setTopic(BATCH_DEL_ATT)
                .setTargetId(chatroomId)
                .setData(builder.build().toByteString())
                .build();
        mMsgCmdMap.put(index, body.getTopic());
        Connect.ImWebsocketMsg m = createImWebsocketMsgWithQueryMsg(body);
        return m.toByteArray();
    }

    byte[] addMsgSet(String messageId, Conversation conversation, String key, String userId, int index) {
        Appmessages.MsgExtItem item = Appmessages.MsgExtItem.newBuilder().setKey(key).setValue(userId).build();
        Appmessages.MsgExt ext = Appmessages.MsgExt.newBuilder()
                .setTargetId(conversation.getConversationId())
                .setChannelTypeValue(conversation.getConversationType().getValue())
                .setMsgId(messageId)
                .setExt(item)
                .build();
        Connect.QueryMsgBody body = Connect.QueryMsgBody.newBuilder()
                .setIndex(index)
                .setTopic(MSG_EX_SET)
                .setTargetId(messageId)
                .setData(ext.toByteString())
                .build();
        mMsgCmdMap.put(index, body.getTopic());
        Connect.ImWebsocketMsg m = createImWebsocketMsgWithQueryMsg(body);
        return m.toByteArray();
    }

    byte[] removeMsgSet(String messageId, Conversation conversation, String key, String userId, int index) {
        Appmessages.MsgExtItem item = Appmessages.MsgExtItem.newBuilder().setKey(key).setValue(userId).build();
        Appmessages.MsgExt ext = Appmessages.MsgExt.newBuilder()
                .setTargetId(conversation.getConversationId())
                .setChannelTypeValue(conversation.getConversationType().getValue())
                .setMsgId(messageId)
                .setExt(item)
                .build();
        Connect.QueryMsgBody body = Connect.QueryMsgBody.newBuilder()
                .setIndex(index)
                .setTopic(DEL_MSG_EX_SET)
                .setTargetId(messageId)
                .setData(ext.toByteString())
                .build();
        mMsgCmdMap.put(index, body.getTopic());
        Connect.ImWebsocketMsg m = createImWebsocketMsgWithQueryMsg(body);
        return m.toByteArray();
    }

    byte[] queryMsgExSet(List<String> messageIdList, Conversation conversation, int index) {
        Appmessages.QryMsgExtReq.Builder builder = Appmessages.QryMsgExtReq.newBuilder()
                .setTargetId(conversation.getConversationId())
                .setChannelTypeValue(conversation.getConversationType().getValue());
        for (String messageId : messageIdList) {
            if (messageId != null) {
                builder.addMsgIds(messageId);
            }
        }
        Appmessages.QryMsgExtReq req = builder.build();
        Connect.QueryMsgBody body = Connect.QueryMsgBody.newBuilder()
                .setIndex(index)
                .setTopic(QRY_MSG_EX_SET)
                .setTargetId(conversation.getConversationId())
                .setData(req.toByteString())
                .build();
        mMsgCmdMap.put(index, body.getTopic());
        Connect.ImWebsocketMsg m = createImWebsocketMsgWithQueryMsg(body);
        return m.toByteArray();
    }

    byte[] qryFirstUnreadMessage(Conversation conversation, int index) {
        Appmessages.QryFirstUnreadMsgReq req = Appmessages.QryFirstUnreadMsgReq.newBuilder()
                .setTargetId(conversation.getConversationId())
                .setChannelType(channelTypeFromConversationType(conversation.getConversationType()))
                .build();
        Connect.QueryMsgBody body = Connect.QueryMsgBody.newBuilder()
                .setIndex(index)
                .setTopic(QRY_FIRST_UNREAD_MSG)
                .setTargetId(conversation.getConversationId())
                .setData(req.toByteString())
                .build();
        mMsgCmdMap.put(index, body.getTopic());
        Connect.ImWebsocketMsg m = createImWebsocketMsgWithQueryMsg(body);
        return m.toByteArray();
    }

    byte[] pushSwitch(boolean enablePush, String userId, int index) {
        Appmessages.PushSwitch ps = Appmessages.PushSwitch.newBuilder()
                .setSwitch(enablePush ? 1 : 0)
                .build();

        Connect.QueryMsgBody body = Connect.QueryMsgBody.newBuilder()
                .setIndex(index)
                .setTopic(PUSH_SWITCH)
                .setTargetId(userId)
                .setData(ps.toByteString())
                .build();
        Connect.ImWebsocketMsg m = createImWebsocketMsgWithQueryMsg(body);
        return m.toByteArray();
    }

    byte[] uploadLogStatus(int result, String userId, String messageId, String url, int index) {
        Appmessages.UploadLogStatusReq.Builder builder = Appmessages.UploadLogStatusReq.newBuilder();
        builder.setMsgId(messageId);
        if (!TextUtils.isEmpty(url)) {
            builder.setLogUrl(url);
        }
        builder.setState(result);

        Connect.QueryMsgBody body = Connect.QueryMsgBody.newBuilder()
                .setIndex(index)
                .setTopic(UPLOAD_LOG_STATUS)
                .setTargetId(userId)
                .setData(builder.build().toByteString())
                .build();
        Connect.ImWebsocketMsg m = createImWebsocketMsgWithQueryMsg(body);
        return m.toByteArray();
    }

    byte[] setLanguage(String language, String userId, int index) {
        Appmessages.KvItem item = Appmessages.KvItem.newBuilder()
                .setKey(LANGUAGE)
                .setValue(language)
                .build();
        Appmessages.UserInfo userInfo = Appmessages.UserInfo.newBuilder()
                .addSettings(item)
                .build();
        Connect.QueryMsgBody body = Connect.QueryMsgBody.newBuilder()
                .setIndex(index)
                .setTopic(SET_USER_SETTINGS)
                .setTargetId(userId)
                .setData(userInfo.toByteString())
                .build();
        mMsgCmdMap.put(index, body.getTopic());
        Connect.ImWebsocketMsg m = createImWebsocketMsgWithQueryMsg(body);
        return m.toByteArray();
    }

    byte[] getLanguage(String userId, int index) {
        Appmessages.KvItem item = Appmessages.KvItem.newBuilder()
                .setKey(LANGUAGE)
                .build();
        Appmessages.UserInfo userInfo = Appmessages.UserInfo.newBuilder()
                .addSettings(item)
                .build();
        Connect.QueryMsgBody body = Connect.QueryMsgBody.newBuilder()
                .setIndex(index)
                .setTopic(GET_USER_SETTINGS)
                .setTargetId(userId)
                .setData(userInfo.toByteString())
                .build();
        mMsgCmdMap.put(index, body.getTopic());
        Connect.ImWebsocketMsg m = createImWebsocketMsgWithQueryMsg(body);
        return m.toByteArray();
    }

    byte[] addConversationsToTag(List<Conversation> conversations, String tagId, String userId, int index) {
        Appmessages.TagConvers.Builder builder = Appmessages.TagConvers.newBuilder();
        builder.setTag(tagId);
        for (Conversation conversation : conversations) {
            Appmessages.SimpleConversation sc = Appmessages.SimpleConversation.newBuilder()
                    .setTargetId(conversation.getConversationId())
                    .setChannelTypeValue(conversation.getConversationType().getValue())
                    .build();
            builder.addConvers(sc);
        }
        Appmessages.TagConvers tagConvers = builder.build();

        Connect.QueryMsgBody body = Connect.QueryMsgBody.newBuilder()
                .setIndex(index)
                .setTopic(TAG_ADD_CONVERS)
                .setTargetId(userId)
                .setData(tagConvers.toByteString())
                .build();
        mMsgCmdMap.put(index, body.getTopic());
        Connect.ImWebsocketMsg m = createImWebsocketMsgWithQueryMsg(body);
        return m.toByteArray();
    }

    byte[] removeConversationsFromTag(List<Conversation> conversations, String tagId, String userId, int index) {
        Appmessages.TagConvers.Builder builder = Appmessages.TagConvers.newBuilder();
        builder.setTag(tagId);
        for (Conversation conversation : conversations) {
            Appmessages.SimpleConversation sc = Appmessages.SimpleConversation.newBuilder()
                    .setTargetId(conversation.getConversationId())
                    .setChannelTypeValue(conversation.getConversationType().getValue())
                    .build();
            builder.addConvers(sc);
        }
        Appmessages.TagConvers tagConvers = builder.build();

        Connect.QueryMsgBody body = Connect.QueryMsgBody.newBuilder()
                .setIndex(index)
                .setTopic(TAG_DEL_CONVERS)
                .setTargetId(userId)
                .setData(tagConvers.toByteString())
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
        byte[] payload = mConverter.encode(body.toByteArray());
        Connect.ImWebsocketMsg msg = Connect.ImWebsocketMsg.newBuilder()
                .setVersion(PROTOCOL_VERSION)
                .setCmd(CmdType.publishAck)
                .setQos(Qos.no)
                .setPayload(ByteString.copyFrom(payload))
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
            byte[] decodeData = mConverter.decode(msg.getPayload().toByteArray());
            switch (msg.getCmd()) {
                case CmdType.connectAck:
                    obj.setRcvType(PBRcvObj.PBRcvType.connectAck);
                    PBRcvObj.ConnectAck ack = new PBRcvObj.ConnectAck();
                    Connect.ConnectAckMsgBody connectAckMsgBody = Connect.ConnectAckMsgBody.parseFrom(decodeData);
                    ack.code = connectAckMsgBody.getCode();
                    ack.userId = connectAckMsgBody.getUserId();
                    ack.session = connectAckMsgBody.getSession();
                    ack.extra = connectAckMsgBody.getExt();
                    obj.mConnectAck = ack;
                    break;

                case CmdType.publishAck: {
                    Connect.PublishAckMsgBody publishAckMsgBody = Connect.PublishAckMsgBody.parseFrom(decodeData);
                    int type = getTypeInCmdMap(publishAckMsgBody.getIndex());
                    obj.setRcvType(type);
                    if (type == PBRcvObj.PBRcvType.cmdMatchError) {
                        break;
                    }
                    PBRcvObj.PublishMsgAck a = new PBRcvObj.PublishMsgAck();
                    a.index = publishAckMsgBody.getIndex();
                    a.code = publishAckMsgBody.getCode();
                    a.msgId = publishAckMsgBody.getMsgId();
                    a.timestamp = publishAckMsgBody.getTimestamp();
                    a.seqNo = publishAckMsgBody.getMsgSeqNo();
                    a.clientUid = publishAckMsgBody.getClientMsgId();
                    if (publishAckMsgBody.hasModifiedMsg()) {
                        Message modifiedMsg = messageWithDownMsg(publishAckMsgBody.getModifiedMsg());
                        a.contentType = modifiedMsg.getContentType();
                        a.content = modifiedMsg.getContent();
                    }
                    obj.mPublishMsgAck = a;
                }
                break;

                case CmdType.queryAck:
                    Connect.QueryAckMsgBody queryAckMsgBody = Connect.QueryAckMsgBody.parseFrom(decodeData);
                    int type = getTypeInCmdMap(queryAckMsgBody.getIndex());
                    obj.setRcvType(type);

                    switch (type) {
                        case PBRcvObj.PBRcvType.qryHisMessagesAck:
                            obj = qryHisMsgAckWithImWebsocketMsg(queryAckMsgBody);
                            break;
                        case PBRcvObj.PBRcvType.syncConversationsAck:
                            obj = syncConversationsAckWithImWebsocketMsg(queryAckMsgBody);
                            break;
                        case PBRcvObj.PBRcvType.syncMessagesAck:
                            obj = syncMsgAckWithImWebsocketMsg(queryAckMsgBody);
                            break;
                        case PBRcvObj.PBRcvType.qryReadDetailAck:
                            obj = qryReadDetailAckWithImWebsocketMsg(queryAckMsgBody);
                            break;
                        case PBRcvObj.PBRcvType.simpleQryAck:
                            obj = simpleQryAckWithImWebsocketMsg(queryAckMsgBody);
                            break;
                        case PBRcvObj.PBRcvType.simpleQryAckCallbackTimestamp:
                            obj = simpleQryAckCallbackTimestampWithImWebsocketMsg(queryAckMsgBody);
                            break;
                        case PBRcvObj.PBRcvType.conversationSetTopAck:
                            obj = conversationSetTopAckWithImWebsocketMsg(queryAckMsgBody);
                            break;
                        case PBRcvObj.PBRcvType.qryFileCredAck:
                            obj = qryFileCredAckWithImWebsocketMsg(queryAckMsgBody);
                            break;
                        case PBRcvObj.PBRcvType.addConversationAck:
                            obj = addConversationAckWithImWebsocketMsg(queryAckMsgBody);
                            break;
                        case PBRcvObj.PBRcvType.globalMuteAck:
                            obj = globalMuteAckWithImWebsocketMsg(queryAckMsgBody);
                            break;
                        case PBRcvObj.PBRcvType.qryFirstUnreadMsgAck:
                            obj = qryFirstUnreadMsgAckWithImWebsocketMsg(queryAckMsgBody);
                            break;
                        case PBRcvObj.PBRcvType.syncChatroomMsgAck:
                            obj = syncChatroomMsgAckWithImWebsocketMsg(queryAckMsgBody);
                            break;
                        case PBRcvObj.PBRcvType.setChatroomAttrAck:
                            obj = setChatroomAttrAckWithImWebsocketMsg(queryAckMsgBody);
                            break;
                        case PBRcvObj.PBRcvType.removeChatroomAttrAck:
                            obj = removeChatroomAttrAckWithImWebsocketMsg(queryAckMsgBody);
                            break;
                        case PBRcvObj.PBRcvType.syncChatroomAttrsAck:
                            obj = syncChatroomAttrsAckWithImWebsocketMsg(queryAckMsgBody);
                            break;
                        case PBRcvObj.PBRcvType.callAuthAck:
                            obj = callInviteAckWithImWebsocketMsg(queryAckMsgBody);
                            break;
                        case PBRcvObj.PBRcvType.rtcPingAck:
                            obj.setRcvType(PBRcvObj.PBRcvType.rtcPingAck);
                            break;
                        case PBRcvObj.PBRcvType.qryCallRoomsAck:
                            obj = qryCallRoomsAckWithImWebsocketMsg(queryAckMsgBody);
                            break;
                        case PBRcvObj.PBRcvType.qryCallRoomAck:
                            obj = qryCallRoomAckWithImWebsocketMsg(queryAckMsgBody);
                            break;
                        case PBRcvObj.PBRcvType.getUserInfoAck:
                            obj = getUserInfoAckWithImWebsocketMsg(queryAckMsgBody);
                            break;
                        case PBRcvObj.PBRcvType.qryMsgExtAck:
                            obj = qryMsgExtAckWithImWebsocketMsg(queryAckMsgBody);
                            break;
                        default:
                            break;
                    }
                    break;

                case CmdType.publish:
                    Connect.PublishMsgBody publishMsgBody = Connect.PublishMsgBody.parseFrom(decodeData);
                    if (publishMsgBody.getTopic().equals(NTF)) {
                        Appmessages.Notify ntf = Appmessages.Notify.parseFrom(publishMsgBody.getData());
                        if (ntf.getType() == Appmessages.NotifyType.Msg) {
                            obj.setRcvType(PBRcvObj.PBRcvType.publishMsgNtf);
                            PBRcvObj.PublishMsgNtf n = new PBRcvObj.PublishMsgNtf();
                            n.syncTime = ntf.getSyncTime();
                            obj.mPublishMsgNtf = n;
                        } else if (ntf.getType() == Appmessages.NotifyType.ChatroomMsg) {
                            obj.setRcvType(PBRcvObj.PBRcvType.publishChatroomMsgNtf);
                            PBRcvObj.PublishMsgNtf n = new PBRcvObj.PublishMsgNtf();
                            n.syncTime = ntf.getSyncTime();
                            n.chatroomId = ntf.getChatroomId();
                            obj.mPublishMsgNtf = n;
                        } else if (ntf.getType() == Appmessages.NotifyType.ChatroomAtt) {
                            obj.setRcvType(PBRcvObj.PBRcvType.publishChatroomAttrNtf);
                            PBRcvObj.PublishMsgNtf n = new PBRcvObj.PublishMsgNtf();
                            n.syncTime = ntf.getSyncTime();
                            n.chatroomId = ntf.getChatroomId();
                            obj.mPublishMsgNtf = n;
                        } else if (ntf.getType() == Appmessages.NotifyType.ChatroomDestroy) {
                            obj.setRcvType(PBRcvObj.PBRcvType.chatroomDestroyNtf);
                            PBRcvObj.PublishMsgNtf n = new PBRcvObj.PublishMsgNtf();
                            n.syncTime = ntf.getSyncTime();
                            n.chatroomId = ntf.getChatroomId();
                            obj.mPublishMsgNtf = n;
                        }
                    } else if (publishMsgBody.getTopic().equals(MSG)) {
                        Appmessages.DownMsg downMsg = Appmessages.DownMsg.parseFrom(publishMsgBody.getData());
                        PBRcvObj.PublishMsgBody body = new PBRcvObj.PublishMsgBody();
                        body.rcvMessage = messageWithDownMsg(downMsg);
                        body.index = publishMsgBody.getIndex();
                        body.qos = msg.getQos();
                        obj.setRcvType(PBRcvObj.PBRcvType.publishMsg);
                        obj.mPublishMsgBody = body;
                    } else if (publishMsgBody.getTopic().equals(C_USER_NTF)) {
                        Chatroom.ChrmEvent event = Chatroom.ChrmEvent.parseFrom(publishMsgBody.getData());
                        obj.setRcvType(PBRcvObj.PBRcvType.chatroomEventNtf);
                        PBRcvObj.PublishMsgNtf n = new PBRcvObj.PublishMsgNtf();
                        n.chatroomId = event.getChatId();
                        n.type = PBRcvObj.PBChatroomEventType.setValue(event.getEventType().getNumber());
                        obj.mPublishMsgNtf = n;
                    } else if (publishMsgBody.getTopic().equals(RTC_ROOM_EVENT)) {
                        Rtcroom.RtcRoomEvent event = Rtcroom.RtcRoomEvent.parseFrom(publishMsgBody.getData());
                        obj.setRcvType(PBRcvObj.PBRcvType.rtcRoomEventNtf);
                        PBRcvObj.RtcRoomEventNtf n = new PBRcvObj.RtcRoomEventNtf();
                        n.eventType = PBRcvObj.PBRtcRoomEventType.setValue(event.getRoomEventTypeValue());
                        n.member = callMemberWithPBRtcMember(event.getMember());
                        n.room = rtcRoomWithPBRtcRoom(event.getRoom());
                        obj.mRtcRoomEventNtf = n;
                    } else if (publishMsgBody.getTopic().equals(RTC_INVITE_EVENT)) {
                        Rtcroom.RtcInviteEvent event = Rtcroom.RtcInviteEvent.parseFrom(publishMsgBody.getData());
                        obj.setRcvType(PBRcvObj.PBRcvType.rtcInviteEventNtf);
                        PBRcvObj.RtcInviteEventNtf n = new PBRcvObj.RtcInviteEventNtf();
                        n.type = PBRcvObj.PBRtcInviteType.setValue(event.getInviteTypeValue());
                        n.user = userInfoWithPBUserInfo(event.getUser());
                        n.room = rtcRoomWithPBRtcRoom(event.getRoom());
                        List<UserInfo> targetUserList = new ArrayList<>();
                        for (Appmessages.UserInfo pbUserInfo : event.getTargetUsersList()) {
                            UserInfo u = userInfoWithPBUserInfo(pbUserInfo);
                            targetUserList.add(u);
                        }
                        n.targetUsers = targetUserList;
                        obj.mRtcInviteEventNtf = n;
                    }
                    break;

                case CmdType.disconnect:
                    Connect.DisconnectMsgBody disconnectMsgBody = Connect.DisconnectMsgBody.parseFrom(decodeData);
                    obj.setRcvType(PBRcvObj.PBRcvType.disconnectMsg);
                    PBRcvObj.DisconnectMsg m = new PBRcvObj.DisconnectMsg();
                    m.code = disconnectMsgBody.getCode();
                    m.timestamp = disconnectMsgBody.getTimestamp();
                    m.extra = disconnectMsgBody.getExt();
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
    private PBRcvObj qryHisMsgAckWithImWebsocketMsg(@NonNull Connect.QueryAckMsgBody body) throws InvalidProtocolBufferException {
        PBRcvObj obj = new PBRcvObj();
        Appmessages.DownMsgSet set = Appmessages.DownMsgSet.parseFrom(body.getData());
        obj.setRcvType(PBRcvObj.PBRcvType.qryHisMessagesAck);
        PBRcvObj.QryHisMsgAck a = new PBRcvObj.QryHisMsgAck(body);
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

    private PBRcvObj syncConversationsAckWithImWebsocketMsg(Connect.QueryAckMsgBody body) throws InvalidProtocolBufferException {
        PBRcvObj obj = new PBRcvObj();
        Appmessages.QryConversationsResp resp = Appmessages.QryConversationsResp.parseFrom(body.getData());
        obj.setRcvType(PBRcvObj.PBRcvType.syncConversationsAck);
        PBRcvObj.SyncConvAck a = new PBRcvObj.SyncConvAck(body);
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

    private PBRcvObj syncMsgAckWithImWebsocketMsg(Connect.QueryAckMsgBody body) throws InvalidProtocolBufferException {
        PBRcvObj obj = new PBRcvObj();
        Appmessages.DownMsgSet set = Appmessages.DownMsgSet.parseFrom(body.getData());
        obj.setRcvType(PBRcvObj.PBRcvType.syncMessagesAck);
        //sync 和 query history 共用一个 ack
        PBRcvObj.QryHisMsgAck a = new PBRcvObj.QryHisMsgAck(body);
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

    private PBRcvObj syncChatroomMsgAckWithImWebsocketMsg(Connect.QueryAckMsgBody body) throws InvalidProtocolBufferException {
        PBRcvObj obj = new PBRcvObj();
        Chatroom.SyncChatroomMsgResp resp = Chatroom.SyncChatroomMsgResp.parseFrom(body.getData());
        obj.setRcvType(PBRcvObj.PBRcvType.syncChatroomMsgAck);
        PBRcvObj.QryHisMsgAck a = new PBRcvObj.QryHisMsgAck(body);
        a.isFinished = true;
        List<ConcreteMessage> list = new ArrayList<>();
        for (Appmessages.DownMsg downMsg : resp.getMsgsList()) {
            ConcreteMessage concreteMessage = messageWithDownMsg(downMsg);
            list.add(concreteMessage);
        }
        a.msgList = list;
        obj.mQryHisMsgAck = a;
        return obj;
    }

    private PBRcvObj syncChatroomAttrsAckWithImWebsocketMsg(Connect.QueryAckMsgBody body) throws InvalidProtocolBufferException {
        PBRcvObj obj = new PBRcvObj();
        Chatroom.SyncChatroomAttResp resp = Chatroom.SyncChatroomAttResp.parseFrom(body.getData());
        obj.setRcvType(PBRcvObj.PBRcvType.syncChatroomAttrsAck);
        PBRcvObj.ChatroomAttrsAck a = new PBRcvObj.ChatroomAttrsAck(body);
        List<ChatroomAttributeItem> list = new ArrayList<>();
        for (Chatroom.ChatAttItem chatAttItem : resp.getAttsList()) {
            ChatroomAttributeItem item = new ChatroomAttributeItem();
            item.setKey(chatAttItem.getKey());
            item.setValue(chatAttItem.getValue());
            item.setTimestamp(chatAttItem.getAttTime());
            item.setUserId(chatAttItem.getUserId());
            item.setType(ChatroomAttributeItem.ChatroomAttrOptType.setValue(chatAttItem.getOptType().getNumber()));
            list.add(item);
        }
        a.items = list;
        obj.mChatroomAttrsAck = a;
        return obj;
    }

    private PBRcvObj callInviteAckWithImWebsocketMsg(Connect.QueryAckMsgBody body) throws InvalidProtocolBufferException {
        PBRcvObj obj = new PBRcvObj();
        Rtcroom.RtcAuth rtcAuth = Rtcroom.RtcAuth.parseFrom(body.getData());
        obj.setRcvType(PBRcvObj.PBRcvType.callAuthAck);
        PBRcvObj.StringAck a = new PBRcvObj.StringAck(body);
        Rtcroom.ZegoAuth zegoAuth = rtcAuth.getZegoAuth();
        a.str = zegoAuth.getToken();
        obj.mStringAck = a;
        return obj;
    }

    private PBRcvObj qryCallRoomsAckWithImWebsocketMsg(Connect.QueryAckMsgBody body) throws InvalidProtocolBufferException {
        PBRcvObj obj = new PBRcvObj();
        Rtcroom.RtcMemberRooms rooms = Rtcroom.RtcMemberRooms.parseFrom(body.getData());
        obj.setRcvType(PBRcvObj.PBRcvType.qryCallRoomsAck);
        List<RtcRoom> outRooms = new ArrayList<>();
        if (rooms.getRoomsCount() > 0) {
            for (Rtcroom.RtcMemberRoom room : rooms.getRoomsList()) {
                RtcRoom outRoom = new RtcRoom();
                outRoom.setRoomId(room.getRoomId());
                outRoom.setDeviceId(room.getDeviceId());
                outRoom.setCallStatus(CallConst.CallStatus.setValue(room.getRtcStateValue()));
                outRooms.add(outRoom);
            }
        }
        PBRcvObj.RtcQryCallRoomsAck a = new PBRcvObj.RtcQryCallRoomsAck(body);
        a.rooms = outRooms;
        obj.mRtcQryCallRoomsAck = a;
        return obj;
    }

    private PBRcvObj qryCallRoomAckWithImWebsocketMsg(Connect.QueryAckMsgBody body) throws InvalidProtocolBufferException {
        PBRcvObj obj = new PBRcvObj();
        Rtcroom.RtcRoom room = Rtcroom.RtcRoom.parseFrom(body.getData());
        obj.setRcvType(PBRcvObj.PBRcvType.qryCallRoomAck);
        List<RtcRoom> outRooms = new ArrayList<>();
        RtcRoom outRoom = new RtcRoom();
        outRoom.setMultiCall(room.getRoomType() == Rtcroom.RtcRoomType.OneMore);
        outRoom.setRoomId(room.getRoomId());
        outRoom.setOwner(userInfoWithPBUserInfo(room.getOwner()));
        List<CallMember> members = new ArrayList<>();
        for (Rtcroom.RtcMember member : room.getMembersList()) {
            CallMember outMember = callMemberWithPBRtcMember(member);
            members.add(outMember);
        }
        outRoom.setMembers(members);
        outRooms.add(outRoom);
        //共用 RtcQryCallRoomsAck
        PBRcvObj.RtcQryCallRoomsAck a = new PBRcvObj.RtcQryCallRoomsAck(body);
        a.rooms = outRooms;
        obj.mRtcQryCallRoomsAck = a;
        return obj;
    }

    private PBRcvObj getUserInfoAckWithImWebsocketMsg(Connect.QueryAckMsgBody body) throws InvalidProtocolBufferException {
        PBRcvObj obj = new PBRcvObj();
        Appmessages.UserInfo userInfo = Appmessages.UserInfo.parseFrom(body.getData());
        obj.setRcvType(PBRcvObj.PBRcvType.getUserInfoAck);
        String s = "";
        for (Appmessages.KvItem item : userInfo.getSettingsList()) {
            if (item.getKey().equals(LANGUAGE)) {
                s = item.getValue();
                break;
            }
        }
        PBRcvObj.StringAck a = new PBRcvObj.StringAck(body);
        a.str = s;
        obj.mStringAck = a;
        return obj;
    }

    private PBRcvObj qryMsgExtAckWithImWebsocketMsg(Connect.QueryAckMsgBody body) throws InvalidProtocolBufferException {
        PBRcvObj obj = new PBRcvObj();
        Appmessages.MsgExtItemsList list = Appmessages.MsgExtItemsList.parseFrom(body.getData());
        obj.setRcvType(PBRcvObj.PBRcvType.qryMsgExtAck);
        List<MessageReaction> reactionList = new ArrayList<>();
        for (Appmessages.MsgExtItems pbItems : list.getItemsList()) {
            MessageReaction reaction = new MessageReaction();
            reaction.setMessageId(pbItems.getMsgId());
            List<MessageReactionItem> itemList = new ArrayList<>();
            boolean isUpdate = false;
            for (Appmessages.MsgExtItem pbItem : pbItems.getExtsList()) {
                UserInfo user = userInfoWithPBUserInfo(pbItem.getUserInfo());
                isUpdate = false;
                for (MessageReactionItem loopItem : itemList) {
                    if (loopItem.getReactionId().equals(pbItem.getKey())) {
                        isUpdate = true;
                        List<UserInfo> userInfoList = loopItem.getUserInfoList();
                        userInfoList.add(user);
                        loopItem.setUserInfoList(userInfoList);
                        break;
                    }
                }
                if (!isUpdate) {
                    MessageReactionItem reactionItem = new MessageReactionItem();
                    reactionItem.setReactionId(pbItem.getKey());
                    reactionItem.setUserInfoList(new ArrayList<>(Collections.singletonList(user)));
                    itemList.add(reactionItem);
                }
            }
            reaction.setItemList(itemList);
            reactionList.add(reaction);
        }
        PBRcvObj.QryMsgExtAck ack = new PBRcvObj.QryMsgExtAck(body);
        ack.reactionList = reactionList;
        obj.mQryMsgExtAck = ack;
        return obj;
    }

    private PBRcvObj setChatroomAttrAckWithImWebsocketMsg(Connect.QueryAckMsgBody body) throws InvalidProtocolBufferException {
        PBRcvObj obj = new PBRcvObj();
        obj.setRcvType(PBRcvObj.PBRcvType.setChatroomAttrAck);
        PBRcvObj.ChatroomAttrsAck a = new PBRcvObj.ChatroomAttrsAck(body);
        List<ChatroomAttributeItem> list = new ArrayList<>();
        Chatroom.ChatAttBatchResp batchResp = Chatroom.ChatAttBatchResp.parseFrom(body.getData());
        for (Chatroom.ChatAttResp resp : batchResp.getAttRespsList()) {
            ChatroomAttributeItem item = new ChatroomAttributeItem();
            item.setKey(resp.getKey());
            item.setCode(resp.getCode());
            item.setTimestamp(resp.getAttTime());
            list.add(item);
        }
        a.items = list;
        obj.mChatroomAttrsAck = a;
        return obj;
    }

    private PBRcvObj removeChatroomAttrAckWithImWebsocketMsg(Connect.QueryAckMsgBody body) throws InvalidProtocolBufferException {
        PBRcvObj obj = new PBRcvObj();
        obj.setRcvType(PBRcvObj.PBRcvType.removeChatroomAttrAck);
        PBRcvObj.ChatroomAttrsAck a = new PBRcvObj.ChatroomAttrsAck(body);
        List<ChatroomAttributeItem> list = new ArrayList<>();
        Chatroom.ChatAttBatchResp batchResp = Chatroom.ChatAttBatchResp.parseFrom(body.getData());
        for (Chatroom.ChatAttResp resp : batchResp.getAttRespsList()) {
            ChatroomAttributeItem item = new ChatroomAttributeItem();
            item.setKey(resp.getKey());
            item.setCode(resp.getCode());
            item.setTimestamp(resp.getAttTime());
            list.add(item);
        }
        a.items = list;
        obj.mChatroomAttrsAck = a;
        return obj;
    }

    private PBRcvObj simpleQryAckWithImWebsocketMsg(Connect.QueryAckMsgBody body) {
        PBRcvObj obj = new PBRcvObj();
        obj.setRcvType(PBRcvObj.PBRcvType.simpleQryAck);
        obj.mSimpleQryAck = new PBRcvObj.SimpleQryAck(body);
        return obj;
    }

    private PBRcvObj simpleQryAckCallbackTimestampWithImWebsocketMsg(Connect.QueryAckMsgBody body) throws InvalidProtocolBufferException {
        PBRcvObj obj = new PBRcvObj();
        obj.setRcvType(PBRcvObj.PBRcvType.simpleQryAckCallbackTimestamp);
        obj.mSimpleQryAck = new PBRcvObj.SimpleQryAck(body);
        return obj;
    }

    private PBRcvObj conversationSetTopAckWithImWebsocketMsg(Connect.QueryAckMsgBody body) throws InvalidProtocolBufferException {
        PBRcvObj obj = new PBRcvObj();
        obj.setRcvType(PBRcvObj.PBRcvType.conversationSetTopAck);
        Appmessages.TopConversResp resp = Appmessages.TopConversResp.parseFrom(body.getData());
        PBRcvObj.TimestampQryAck a = new PBRcvObj.TimestampQryAck(body);
        a.operationTime = resp.getOptTime();
        obj.mTimestampQryAck = a;
        return obj;
    }

    private PBRcvObj qryReadDetailAckWithImWebsocketMsg(Connect.QueryAckMsgBody body) throws InvalidProtocolBufferException {
        PBRcvObj obj = new PBRcvObj();
        Appmessages.QryReadDetailResp resp = Appmessages.QryReadDetailResp.parseFrom(body.getData());
        obj.setRcvType(PBRcvObj.PBRcvType.qryReadDetailAck);
        PBRcvObj.QryReadDetailAck a = new PBRcvObj.QryReadDetailAck(body);
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

    private PBRcvObj qryFileCredAckWithImWebsocketMsg(Connect.QueryAckMsgBody body) throws InvalidProtocolBufferException {
        PBRcvObj obj = new PBRcvObj();
        obj.setRcvType(PBRcvObj.PBRcvType.qryFileCredAck);
        Appmessages.QryFileCredResp resp = Appmessages.QryFileCredResp.parseFrom(body.getData());
        PBRcvObj.QryFileCredAck a = new PBRcvObj.QryFileCredAck(body);
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

    private PBRcvObj addConversationAckWithImWebsocketMsg(Connect.QueryAckMsgBody body) throws InvalidProtocolBufferException {
        PBRcvObj obj = new PBRcvObj();
        obj.setRcvType(PBRcvObj.PBRcvType.addConversationAck);
        Appmessages.Conversation resp = Appmessages.Conversation.parseFrom(body.getData());
        PBRcvObj.ConversationInfoAck a = new PBRcvObj.ConversationInfoAck(body);
        a.conversationInfo = conversationInfoWithPBConversation(resp);
        obj.mConversationInfoAck = a;
        return obj;
    }

    private PBRcvObj globalMuteAckWithImWebsocketMsg(Connect.QueryAckMsgBody body) throws InvalidProtocolBufferException {
        PBRcvObj obj = new PBRcvObj();
        obj.setRcvType(PBRcvObj.PBRcvType.globalMuteAck);
        Appmessages.UserUndisturb resp = Appmessages.UserUndisturb.parseFrom(body.getData());
        PBRcvObj.GlobalMuteAck a = new PBRcvObj.GlobalMuteAck(body);
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

    private PBRcvObj qryFirstUnreadMsgAckWithImWebsocketMsg(Connect.QueryAckMsgBody body) throws InvalidProtocolBufferException {
        PBRcvObj obj = new PBRcvObj();
        obj.setRcvType(PBRcvObj.PBRcvType.qryFirstUnreadMsgAck);
        PBRcvObj.QryHisMsgAck a = new PBRcvObj.QryHisMsgAck(body);
        a.isFinished = true;
        List<ConcreteMessage> messages = new ArrayList<>();
        if (!body.getData().isEmpty()) {
            Appmessages.DownMsg downMsg = Appmessages.DownMsg.parseFrom(body.getData());
            ConcreteMessage message = messageWithDownMsg(downMsg);
            messages.add(message);
        }
        a.msgList = messages;
        obj.mQryHisMsgAck = a;
        return obj;
    }

    private Connect.ImWebsocketMsg createImWebsocketMsgWithPublishMsg(Connect.PublishMsgBody publishMsgBody) {
        byte[] payload = mConverter.encode(publishMsgBody.toByteArray());
        return Connect.ImWebsocketMsg.newBuilder()
                .setVersion(PROTOCOL_VERSION)
                .setCmd(CmdType.publish)
                .setQos(Qos.yes)
                .setPayload(ByteString.copyFrom(payload))
                .build();
    }

    private Connect.ImWebsocketMsg createImWebsocketMsgWithQueryMsg(Connect.QueryMsgBody body) {
        byte[] payload = mConverter.encode(body.toByteArray());
        return Connect.ImWebsocketMsg.newBuilder()
                .setVersion(PROTOCOL_VERSION)
                .setCmd(CmdType.query)
                .setQos(Qos.yes)
                .setPayload(ByteString.copyFrom(payload))
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
        message.setEdit((message.getFlags() & MessageContent.MessageFlag.IS_MODIFIED.getValue()) != 0);
        GroupMessageReadInfo info = new GroupMessageReadInfo();
        info.setReadCount(downMsg.getReadCount());
        info.setMemberCount(downMsg.getMemberCount());
        message.setGroupMessageReadInfo(info);
        message.setGroupInfo(groupInfoWithPBGroupInfo(downMsg.getGroupInfo()));
        message.setTargetUserInfo(userInfoWithPBUserInfo(downMsg.getTargetUserInfo()));
        message.setGroupMemberInfo(groupMemberWithPBGroupMember(downMsg.getGrpMemberInfo(), message.getGroupInfo().getGroupId(), message.getTargetUserInfo().getUserId()));
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
        info.setUnread(conversation.getUnreadTag()==1);
        if (conversation.getConverTagsCount() > 0) {
            List<String> tagIdList = new ArrayList<>();
            for (Appmessages.ConverTag pbTag : conversation.getConverTagsList()) {
                tagIdList.add(pbTag.getTag());
            }
            info.setTagIdList(tagIdList);
        }
        return info;
    }

    private CallMember callMemberWithPBRtcMember(Rtcroom.RtcMember pbMember) {
        if (pbMember == null) {
            return null;
        }
        CallMember result = new CallMember();
        result.setUserInfo(userInfoWithPBUserInfo(pbMember.getMember()));
        result.setCallStatus(CallConst.CallStatus.setValue(pbMember.getRtcStateValue()));
        result.setStartTime(pbMember.getCallTime());
        result.setConnectTime(pbMember.getConnectTime());
        result.setFinishTime(pbMember.getHangupTime());
        result.setInviter(userInfoWithPBUserInfo(pbMember.getInviter()));
        return result;
    }

    private RtcRoom rtcRoomWithPBRtcRoom(Rtcroom.RtcRoom pbRoom) {
        if (pbRoom == null) {
            return null;
        }
        RtcRoom result = new RtcRoom();
        result.setRoomId(pbRoom.getRoomId());
        result.setOwner(userInfoWithPBUserInfo(pbRoom.getOwner()));
        result.setMultiCall(pbRoom.getRoomType() == Rtcroom.RtcRoomType.OneMore);
        return result;
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

    private GroupMember groupMemberWithPBGroupMember(Appmessages.GrpMemberInfo pbGroupMember, String groupId, String userId) {
        if (pbGroupMember == null) {
            return null;
        }
        if (pbGroupMember.getUpdatedTime() == 0) {
            return null;
        }
        GroupMember result = new GroupMember();
        result.setGroupId(groupId);
        result.setUserId(userId);
        result.setGroupDisplayName(pbGroupMember.getGrpDisplayName());
        if (pbGroupMember.getExtFieldsCount() > 0) {
            Map<String, String> extra = new HashMap<>();
            for (Appmessages.KvItem item : pbGroupMember.getExtFieldsList()) {
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
        result.setType(MessageMentionInfo.MentionType.setValue(pbMentionMsg.getMentionTypeValue()));
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

    private static final String PROTO_ID = "jug9le1m";
    private static final int PROTOCOL_VERSION = 1;
    private static final int PLATFORM_ANDROID = 1;
    private static final String QRY_HIS_MSG = "qry_hismsgs";
    private static final String QRY_HISMSG_BY_IDS = "qry_hismsg_by_ids";
    private static final String SYNC_CONV = "sync_convers";
    private static final String SYNC_MSG = "sync_msgs";
    private static final String MARK_READ = "mark_read";
    private static final String RECALL_MSG = "recall_msg";
    private static final String MODIFY_MSG = "modify_msg";
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
    private static final String MARK_UNREAD = "mark_unread";
    private static final String QRY_FIRST_UNREAD_MSG = "qry_first_unread_msg";
    private static final String SYNC_CHATROOM_MESSAGE = "c_sync_msgs";
    private static final String SYNC_CHATROOM_ATTS = "c_sync_atts";
    private static final String BATCH_ADD_ATT = "c_batch_add_att";
    private static final String BATCH_DEL_ATT = "c_batch_del_att";
    private static final String C_JOIN = "c_join";
    private static final String C_QUIT = "c_quit";
    private static final String PUSH_SWITCH = "push_switch";
    private static final String UPLOAD_LOG_STATUS = "upd_log_state";
    private static final String RTC_INVITE = "rtc_invite";
    private static final String RTC_HANGUP = "rtc_hangup";
    private static final String RTC_ACCEPT = "rtc_accept";
    private static final String RTC_QUIT = "rtc_quit";
    private static final String RTC_UPD_STATE = "rtc_upd_state";
    private static final String RTC_MEMBER_ROOMS = "rtc_member_rooms";
    private static final String RTC_QRY = "rtc_qry";
    private static final String RTC_PING = "rtc_ping";
    private static final String SET_USER_SETTINGS = "set_user_settings";
    private static final String GET_USER_SETTINGS = "get_user_settings";
    private static final String LANGUAGE = "language";
    private static final String MSG_EX_SET = "msg_exset";
    private static final String DEL_MSG_EX_SET = "del_msg_exset";
    private static final String QRY_MSG_EX_SET = "qry_msg_exset";
    private static final String TAG_ADD_CONVERS = "tag_add_convers";
    private static final String TAG_DEL_CONVERS = "tag_del_convers";

    private static final String P_MSG = "p_msg";
    private static final String G_MSG = "g_msg";
    private static final String C_MSG = "c_msg";
    private static final String NTF = "ntf";
    private static final String MSG = "msg";
    private static final String C_USER_NTF = "c_user_ntf";
    private static final String RTC_ROOM_EVENT = "rtc_room_event";
    private static final String RTC_INVITE_EVENT = "rtc_invite_event";

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
            put(C_JOIN, PBRcvObj.PBRcvType.simpleQryAckCallbackTimestamp);
            put(C_QUIT, PBRcvObj.PBRcvType.simpleQryAckCallbackTimestamp);
            put(MARK_UNREAD, PBRcvObj.PBRcvType.simpleQryAckCallbackTimestamp);
            put(QRY_FIRST_UNREAD_MSG, PBRcvObj.PBRcvType.qryFirstUnreadMsgAck);
            put(SYNC_CHATROOM_MESSAGE, PBRcvObj.PBRcvType.syncChatroomMsgAck);
            put(BATCH_ADD_ATT, PBRcvObj.PBRcvType.setChatroomAttrAck);
            put(SYNC_CHATROOM_ATTS, PBRcvObj.PBRcvType.syncChatroomAttrsAck);
            put(BATCH_DEL_ATT, PBRcvObj.PBRcvType.removeChatroomAttrAck);
            put(RTC_INVITE, PBRcvObj.PBRcvType.callAuthAck);
            put(RTC_HANGUP, PBRcvObj.PBRcvType.simpleQryAck);
            put(RTC_ACCEPT, PBRcvObj.PBRcvType.callAuthAck);
            put(RTC_UPD_STATE, PBRcvObj.PBRcvType.simpleQryAck);
            put(RTC_PING, PBRcvObj.PBRcvType.rtcPingAck);
            put(RTC_MEMBER_ROOMS, PBRcvObj.PBRcvType.qryCallRoomsAck);
            put(RTC_QRY, PBRcvObj.PBRcvType.qryCallRoomAck);
            put(SET_USER_SETTINGS, PBRcvObj.PBRcvType.simpleQryAck);
            put(GET_USER_SETTINGS, PBRcvObj.PBRcvType.getUserInfoAck);
            put(MSG_EX_SET, PBRcvObj.PBRcvType.simpleQryAckCallbackTimestamp);
            put(DEL_MSG_EX_SET, PBRcvObj.PBRcvType.simpleQryAckCallbackTimestamp);
            put(QRY_MSG_EX_SET, PBRcvObj.PBRcvType.qryMsgExtAck);
            put(MODIFY_MSG, PBRcvObj.PBRcvType.simpleQryAckCallbackTimestamp);
            put(TAG_ADD_CONVERS, PBRcvObj.PBRcvType.simpleQryAck);
            put(TAG_DEL_CONVERS, PBRcvObj.PBRcvType.simpleQryAck);
        }
    };

    private final ConcurrentHashMap<Integer, String> mMsgCmdMap = new ConcurrentHashMap<>();
    private IDataConverter mConverter = new SimpleDataConverter();

}