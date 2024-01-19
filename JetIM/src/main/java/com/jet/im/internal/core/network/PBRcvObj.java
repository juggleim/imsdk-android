package com.jet.im.internal.core.network;

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
    }

    public int getRcvType() {
        return mRcvType;
    }

    public void setRcvType(int rcvType) {
        mRcvType = rcvType;
    }

    ConnectAck mConnectAck;
    PublishMsgAck mPublishMsgAck;
    private int mRcvType;
}


