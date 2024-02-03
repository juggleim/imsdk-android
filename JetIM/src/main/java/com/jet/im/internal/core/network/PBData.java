package com.jet.im.internal.core.network;

import android.text.TextUtils;

import androidx.annotation.NonNull;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import com.jet.im.JetIMConst;
import com.jet.im.internal.ContentTypeCenter;
import com.jet.im.internal.model.ConcreteConversationInfo;
import com.jet.im.internal.model.ConcreteMessage;
import com.jet.im.model.Conversation;
import com.jet.im.model.Message;
import com.jet.im.utils.LoggerUtils;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import app_messages.Appmessages;
import app_messages.Connect;

class PBData {
    byte[] connectData(String appKey,
                       String token,
                       String deviceId,
                       String platform,
                       String deviceCompany,
                       String deviceModel,
                       String osVersion,
                       String pushToken,
                       String networkId,
                       String ispNum,
                       String clientIp) {
        Connect.ConnectMsgBody body = Connect.ConnectMsgBody.newBuilder()
                .setProtoId(PROTO_ID)
                .setSdkVersion(SDK_VERSION)
                .setAppkey(appKey)
                .setToken(token)
                .setDeviceId(deviceId)
                .setPlatform(platform)
                .setDeviceCompany(deviceCompany)
                .setDeviceModel(deviceModel)
                .setDeviceOsVersion(osVersion)
                .setPushToken(pushToken)
                .setNetworkId(networkId)
                .setIspNum(ispNum)
                .setClientIp(clientIp).build();
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
                           int index,
                           Conversation.ConversationType conversationType,
                           String conversationId) {
        ByteString byteString = ByteString.copyFrom(msgData);
        Appmessages.UpMsg upMsg = Appmessages.UpMsg.newBuilder()
                .setMsgType(contentType)
                .setMsgContent(byteString)
                .setFlags(flags)
                .setClientUid(clientUid)
                .build();

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

    byte[] recallMessageData(String messageId, Conversation conversation, long timestamp, int index) {
        Appmessages.RecallMsgReq req = Appmessages.RecallMsgReq.newBuilder()
                .setMsgId(messageId)
                .setTargetId(conversation.getConversationId())
                .setChannelTypeValue(conversation.getConversationType().getValue())
                .setMsgTime(timestamp)
                .build();

        Connect.PublishMsgBody publishMsg = Connect.PublishMsgBody.newBuilder()
                .setIndex(index)
                .setTopic(RECALL_MSG)
                .setTargetId(conversation.getConversationId())
                .setData(req.toByteString())
                .build();

        mMsgCmdMap.put(index, RECALL_MSG);

        Connect.ImWebsocketMsg msg = createImWebsocketMsgWithPublishMsg(publishMsg);
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
        Connect.ImWebsocketMsg m = Connect.ImWebsocketMsg.newBuilder()
                .setVersion(PROTOCOL_VERSION)
                .setCmd(CmdType.query)
                .setQos(Qos.yes)
                .setQryMsgBody(body)
                .build();
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
        Connect.ImWebsocketMsg m = Connect.ImWebsocketMsg.newBuilder()
                .setVersion(PROTOCOL_VERSION)
                .setCmd(CmdType.query)
                .setQos(Qos.yes)
                .setQryMsgBody(body)
                .build();
        return m.toByteArray();
    }

    byte[] queryHisMsgData(Conversation conversation, long startTime, int count, JetIMConst.PullDirection direction, int index) {
        int order = direction == JetIMConst.PullDirection.OLDER ? 0 : 1;
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
        Connect.ImWebsocketMsg m = Connect.ImWebsocketMsg.newBuilder()
                .setVersion(PROTOCOL_VERSION)
                .setCmd(CmdType.query)
                .setQos(Qos.yes)
                .setQryMsgBody(body)
                .build();
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
                LoggerUtils.e("rcvObjWithBytes msg is null");
                obj.setRcvType(PBRcvObj.PBRcvType.parseError);
                return obj;
            }
            if (msg.getCmd() == CmdType.pong) {
                obj.setRcvType(PBRcvObj.PBRcvType.pong);
                LoggerUtils.d("mMsgCmdMap size is " + mMsgCmdMap.size());
                return obj;
            }
            switch (msg.getTestofCase()) {
                case CONNECTACKMSGBODY:
                    obj.setRcvType(PBRcvObj.PBRcvType.connectAck);
                    PBRcvObj.ConnectAck ack = new PBRcvObj.ConnectAck();
                    ack.code = msg.getConnectAckMsgBody().getCode();
                    ack.userId = msg.getConnectAckMsgBody().getUserId();
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
                    a.msgIndex = msg.getPubAckMsgBody().getMsgIndex();
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
                        default:
                            break;
                    }
                    break;

                case PUBLISHMSGBODY:
                    if (msg.getPublishMsgBody().getTopic().equals(NTF)) {
                        LoggerUtils.d("publish msg notify");
                        Appmessages.Notify ntf = Appmessages.Notify.parseFrom(msg.getPublishMsgBody().getData());
                        if (ntf.getType() == Appmessages.NotifyType.Msg) {
                            obj.setRcvType(PBRcvObj.PBRcvType.publishMsgNtf);
                            PBRcvObj.PublishMsgNtf n = new PBRcvObj.PublishMsgNtf();
                            n.syncTime = ntf.getSyncTime();
                            obj.mPublishMsgNtf = n;
                        }
                    } else if (msg.getPublishMsgBody().getTopic().equals(MSG)) {
                        LoggerUtils.d("publish msg directly");
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
                    obj.mDisconnectMsg = m;
                    break;

                    //todo
            }
        } catch (InvalidProtocolBufferException e) {
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
        for (Appmessages.Conversation conversation : resp.getConversationsList()) {
            ConcreteConversationInfo info = conversationWithPBConversation(conversation);
            list.add(info);
        }
        a.convList = list;
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

    private Connect.ImWebsocketMsg createImWebsocketMsgWithPublishMsg(Connect.PublishMsgBody publishMsgBody) {
        return Connect.ImWebsocketMsg.newBuilder()
                .setVersion(PROTOCOL_VERSION)
                .setCmd(CmdType.publish)
                .setQos(Qos.yes)
                .setPublishMsgBody(publishMsgBody)
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
        message.setHasRead(downMsg.getIsReaded());
        message.setState(Message.MessageState.SENT);
        message.setTimestamp(downMsg.getMsgTime());
        message.setSenderUserId(downMsg.getSenderId());
        message.setMsgIndex(downMsg.getMsgIndex());
        message.setContent(ContentTypeCenter.getInstance().getContent(downMsg.getMsgContent().toByteArray(), downMsg.getMsgType()));
        int flags = ContentTypeCenter.getInstance().flagsWithType(downMsg.getMsgType());
        if (flags < 0) {
            message.setFlags(downMsg.getFlags());
        } else {
            message.setFlags(flags);
        }
        return message;
    }

    private ConcreteConversationInfo conversationWithPBConversation(Appmessages.Conversation conversation) {
        ConcreteConversationInfo info = new ConcreteConversationInfo();
        Conversation c = new Conversation(conversationTypeFromChannelType(conversation.getChannelType()), conversation.getTargetId());
        info.setConversation(c);
        info.setUnreadCount((int)conversation.getUnreadCount());
        info.setUpdateTime(conversation.getUpdateTime());
        info.setLastMessage(messageWithDownMsg(conversation.getMsg()));
        info.setLastReadMessageIndex(conversation.getLatestReadedMsgIndex());
        //todo mention
        return info;
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

    private int getTypeInCmdMap(Integer index) {
        String cachedCmd = mMsgCmdMap.remove(index);
        if (TextUtils.isEmpty(cachedCmd)) {
            LoggerUtils.e("rcvObjWithBytes ack can't match a cached cmd");
            return PBRcvObj.PBRcvType.cmdMatchError;
        }
        Integer type = sCmdAckMap.get(cachedCmd);
        if (type == null) {
            LoggerUtils.e("rcvObjWithBytes ack cmd match error, cmd is " + cachedCmd);
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
    private static final String QRY_HIS_MSG = "qry_hismsgs";
    private static final String SYNC_CONV = "sync_convers";
    private static final String SYNC_MSG = "sync_msgs";
    private static final String RECALL_MSG = "recall_msg";
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
            put(RECALL_MSG, PBRcvObj.PBRcvType.recall);
        }
    };

    private final ConcurrentHashMap<Integer, String> mMsgCmdMap = new ConcurrentHashMap<>();

}
