package com.juggle.im.internal.core.network;

import com.juggle.im.call.internal.model.RtcRoom;
import com.juggle.im.call.model.CallMember;
import com.juggle.im.internal.model.ChatroomAttributeItem;
import com.juggle.im.internal.model.ConcreteConversationInfo;
import com.juggle.im.internal.model.ConcreteMessage;
import com.juggle.im.internal.model.upload.UploadOssType;
import com.juggle.im.internal.model.upload.UploadPreSignCred;
import com.juggle.im.internal.model.upload.UploadQiNiuCred;
import com.juggle.im.model.FavoriteMessage;
import com.juggle.im.model.MessageContent;
import com.juggle.im.model.MessageReaction;
import com.juggle.im.model.TimePeriod;
import com.juggle.im.model.UserInfo;

import java.util.List;

import app_messages.Connect;

class PBRcvObj {

    static class ConnectAck {
        int code;
        String userId;
        String session;
        String extra;
    }

    static class PublishMsgAck {
        int index;
        int code;
        String msgId;
        long timestamp;
        long seqNo;
        String clientUid;
        String contentType;
        MessageContent content;
        int groupMemberCount;
    }

    static class PublishMsgBody {
        ConcreteMessage rcvMessage;
        int index;
        int qos;
    }

    static class QryAck {
        QryAck(Connect.QueryAckMsgBody body) {
            this.index = body.getIndex();
            this.code = body.getCode();
            this.timestamp = body.getTimestamp();
        }

        int index;
        int code;
        long timestamp;
    }

    static class QryHisMsgAck extends QryAck {
        boolean isFinished;
        List<ConcreteMessage> msgList;

        QryHisMsgAck(Connect.QueryAckMsgBody body) {
            super(body);
        }
    }

    static class SyncConvAck extends QryAck {
        boolean isFinished;
        List<ConcreteConversationInfo> convList;
        List<ConcreteConversationInfo> deletedConvList;

        SyncConvAck(Connect.QueryAckMsgBody body) {
            super(body);
        }
    }

    static class SimpleQryAck extends QryAck {
        SimpleQryAck(Connect.QueryAckMsgBody body) {
            super(body);
        }
    }

    static class TimestampQryAck extends QryAck {
        long operationTime;

        TimestampQryAck(Connect.QueryAckMsgBody body) {
            super(body);
        }
    }

    static class QryFileCredAck extends QryAck {
        UploadOssType ossType;
        UploadQiNiuCred qiNiuCred;
        UploadPreSignCred preSignCred;

        QryFileCredAck(Connect.QueryAckMsgBody body) {
            super(body);
        }
    }

    static class ConversationInfoAck extends QryAck {
        ConcreteConversationInfo conversationInfo;

        ConversationInfoAck(Connect.QueryAckMsgBody body) {
            super(body);
        }
    }

    static class GlobalMuteAck extends QryAck {
        boolean isMute;
        String timezone;
        List<TimePeriod> periods;

        GlobalMuteAck(Connect.QueryAckMsgBody body) {
            super(body);
        }
    }

    static class ChatroomAttrsAck extends QryAck {
        List<ChatroomAttributeItem> items;

        ChatroomAttrsAck(Connect.QueryAckMsgBody body) {
            super(body);
        }
    }

    static class StringAck extends QryAck {
        String str;
        StringAck(Connect.QueryAckMsgBody body) {
            super(body);
        }
    }

    static class RtcQryCallRoomsAck extends QryAck {
        List<RtcRoom> rooms;
        RtcQryCallRoomsAck(Connect.QueryAckMsgBody body) {
            super(body);
        }
    }

    static class QryMsgExtAck extends QryAck {
        List<MessageReaction> reactionList;
        QryMsgExtAck(Connect.QueryAckMsgBody body) {
            super(body);
        }
    }

    static class GetTopMsgAck extends QryAck {
        ConcreteMessage message;
        UserInfo userInfo;
        long createdTime;
        GetTopMsgAck(Connect.QueryAckMsgBody body) {
            super(body);
        }
    }

    static class GetFavoriteMsgAck extends QryAck {
        List<FavoriteMessage> favoriteMessages;
        String offset;
        GetFavoriteMsgAck(Connect.QueryAckMsgBody body) {
            super(body);
        }
    }

    static class TemplateAck<T> extends QryAck {
        T t;
        TemplateAck(Connect.QueryAckMsgBody body) {
            super(body);
        }
    }

    static class PublishMsgNtf {
        long syncTime;
        String chatroomId;
        PBChatroomEventType type;
    }

    static class RtcInviteEventNtf {
        PBRtcInviteType type;
        UserInfo user;
        RtcRoom room;
        List<UserInfo> targetUsers;
    }

    static class RtcRoomEventNtf {
        PBRtcRoomEventType eventType;
        List<CallMember> members;
        RtcRoom room;
    }

    static class DisconnectMsg {
        int code;
        long timestamp;
        String extra;
    }

    static class PBRcvType {
        static final int parseError = 0;
        static final int cmdMatchError = 1;
        static final int connectAck = 2;
        static final int publishMsgAck = 3;
        static final int qryHisMessagesAck = 4;
        static final int syncConversationsAck = 5;
        static final int syncMessagesAck = 6;
        static final int publishMsg = 7;
        static final int publishMsgNtf = 8;
        static final int pong = 9;
        static final int disconnectMsg = 10;
        static final int qryReadDetailAck = 11;
        static final int simpleQryAck = 12;
        static final int simpleQryAckCallbackTimestamp = 13;
        static final int conversationSetTopAck = 14;
        static final int qryFileCredAck = 15;
        static final int addConversationAck = 16;
        static final int globalMuteAck = 17;
        static final int publishChatroomMsgNtf = 18;
        static final int syncChatroomMsgAck = 19;
        static final int qryFirstUnreadMsgAck = 20;
        static final int setChatroomAttrAck = 21;
        static final int publishChatroomAttrNtf = 22;
        static final int syncChatroomAttrsAck = 23;
        static final int removeChatroomAttrAck = 24;
        static final int chatroomDestroyNtf = 25;
        static final int chatroomEventNtf = 26;
        static final int rtcRoomEventNtf = 27;
        static final int rtcInviteEventNtf = 28;
        static final int callAuthAck = 29;
        static final int rtcPingAck = 30;
        static final int qryCallRoomsAck = 31;
        static final int qryCallRoomAck = 32;
        static final int qryMsgExtAck = 33;
        static final int getUserInfoAck = 34;
        static final int getTopMsgAck = 35;
        static final int getFavoriteMsgAck = 36;
        static final int getConversationConfAck = 37;
    }

    public enum PBChatroomEventType {
        JOIN(0),
        QUIT(1),
        KICK(2),
        FALLOUT(3);
        /// 系统会话
        PBChatroomEventType(int value) {
            this.mValue = value;
        }
        public int getValue() {
            return mValue;
        }
        public static PBChatroomEventType setValue(int value) {
            for (PBChatroomEventType t : PBChatroomEventType.values()) {
                if (value == t.mValue) {
                    return t;
                }
            }
            return JOIN;
        }
        private final int mValue;
    }

    public enum PBRtcInviteType {
        INVITE(0),
        ACCEPT(1),
        HANGUP(2);
        PBRtcInviteType(int value) {
            this.mValue = value;
        }
        public int getValue() {
            return mValue;
        }
        public static PBRtcInviteType setValue(int value) {
            for (PBRtcInviteType t : PBRtcInviteType.values()) {
                if (value == t.mValue) {
                    return t;
                }
            }
            return INVITE;
        }
        private final int mValue;
    }

    public enum PBRtcRoomEventType {
        DEFAULT(0),
        JOIN(1),
        QUIT(2),
        DESTROY(3),
        STATE_CHANGE(4);
        PBRtcRoomEventType(int value) {
            this.mValue = value;
        }
        public int getValue() {
            return mValue;
        }
        public static PBRtcRoomEventType setValue(int value) {
            for (PBRtcRoomEventType t : PBRtcRoomEventType.values()) {
                if (value == t.mValue) {
                    return t;
                }
            }
            return DEFAULT;
        }
        private final int mValue;
    }

    public int getRcvType() {
        return mRcvType;
    }

    public void setRcvType(int rcvType) {
        mRcvType = rcvType;
    }

    ConnectAck mConnectAck;
    PublishMsgAck mPublishMsgAck;
    QryHisMsgAck mQryHisMsgAck;
    SyncConvAck mSyncConvAck;
    PublishMsgBody mPublishMsgBody;
    PublishMsgNtf mPublishMsgNtf;
    DisconnectMsg mDisconnectMsg;
    SimpleQryAck mSimpleQryAck;
    TimestampQryAck mTimestampQryAck;
    QryFileCredAck mQryFileCredAck;
    ConversationInfoAck mConversationInfoAck;
    GlobalMuteAck mGlobalMuteAck;
    ChatroomAttrsAck mChatroomAttrsAck;
    RtcRoomEventNtf mRtcRoomEventNtf;
    RtcInviteEventNtf mRtcInviteEventNtf;
    StringAck mStringAck;
    RtcQryCallRoomsAck mRtcQryCallRoomsAck;
    QryMsgExtAck mQryMsgExtAck;
    GetTopMsgAck mGetTopMsgAck;
    GetFavoriteMsgAck mGetFavoriteMsgAck;
    TemplateAck mTemplateAck;
    long timestamp;

    private int mRcvType;
}


