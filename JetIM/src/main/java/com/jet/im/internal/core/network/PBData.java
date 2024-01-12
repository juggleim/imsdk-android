package com.jet.im.internal.core.network;

import com.google.protobuf.InvalidProtocolBufferException;

import java.nio.ByteBuffer;

import web_socket_msg.ImWebSocket;

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
        ImWebSocket.ConnectMsgBody body = ImWebSocket.ConnectMsgBody.newBuilder()
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
        ImWebSocket.ImWebsocketMsg msg = ImWebSocket.ImWebsocketMsg.newBuilder()
                .setVersion(PROTOCOL_VERSION)
                .setCmd(CmdType.connect)
                .setQos(Qos.yes)
                .setConnectMsgBody(body)
                .build();
        return msg.toByteArray();
    }

    PBRcvObj rcvObjWithBytes(ByteBuffer byteBuffer) {
        PBRcvObj obj = new PBRcvObj();
        try {
            ImWebSocket.ImWebsocketMsg msg = ImWebSocket.ImWebsocketMsg.parseFrom(byteBuffer);
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
                    obj.connectAck = ack;
                    break;

//                case PUBACKMSGBODY:
//                    break;

                    //todo
            }
        } catch (InvalidProtocolBufferException e) {
            obj.setRcvType(PBRcvObj.PBRcvType.parseError);
        }
        return obj;
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
}
