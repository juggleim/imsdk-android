package com.jet.im.internal.core.network;

import com.jet.im.internal.model.ConcreteConversationInfo;
import com.jet.im.internal.model.ConcreteMessage;
import com.jet.im.model.UserInfo;

import java.util.List;

import app_messages.Connect;

class PBRcvObj {

    static class ConnectAck {
        int code;
        String userId;
    }

    static class PublishMsgAck {
        int index;
        int code;
        String msgId;
        long timestamp;
        long msgIndex;
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

    static class QryReadDetailAck extends QryAck {
        List<UserInfo> readMembers;
        List<UserInfo> unreadMembers;
        QryReadDetailAck(Connect.QueryAckMsgBody body) {
            super(body);
        }
    }

    static class SimpleQryAck extends QryAck {
        SimpleQryAck(Connect.QueryAckMsgBody body) {
            super(body);
        }
    }


    static class PublishMsgNtf {
        long syncTime;
    }

    static class DisconnectMsg {
        int code;
        long timestamp;
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
        static final int recall = 11;
        static final int delConvAck = 12;
        static final int clearUnreadAck = 13;
        static final int markReadAck = 14;
        static final int qryReadDetailAck = 15;
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
    QryReadDetailAck mQryReadDetailAck;
    SimpleQryAck mSimpleQryAck;
    private int mRcvType;
}


