package com.juggle.im.call;

public class CallConst {
    // 通话状态
    public enum CallStatus {
        // 无通话
        IDLE(0),
        // 被呼叫
        INCOMING(1),
        // 呼出
        OUTGOING(2),
        // 连接中
        CONNECTING(3),
        // 连接成功
        CONNECTED(4);
        private final int status;

        CallStatus(int status) {
            this.status = status;
        }

        int getStatus() {
            return this.status;
        }
    }

    /// 通话结束原因
    public enum CallFinishReason {
        /// 未知原因
        UNKNOWN(0),
        /// 当前用户挂断已接通的来电
        HANGUP(1),
        /// 当前用户拒接来电
        DECLINE(2),
        /// 当前用户忙线
        BUSY(3),
        /// 当前用户未接听
        NO_RESPONSE(4),
        /// 当前用户取消呼叫
        CANCEL(5),
        /// 对端用户挂断已接通的来电
        OTHER_SIDE_HANGUP(6),
        /// 对端用户拒接来电
        OTHER_SIDE_DECLINE(7),
        /// 对端用户忙线
        OTHER_SIDE_BUSY(8),
        /// 对端用户未接听
        OTHER_SIDE_NO_RESPONSE(9),
        /// 对端用户取消呼叫
        OTHER_SIDE_CANCEL(10),
        /// 房间被销毁
        ROOM_DESTROY(11),
        /// 网络出错
        NETWORK_ERROR(12);
        private final int value;

        CallFinishReason(int value) {
            this.value = value;
        }

        int getValue() {
            return this.value;
        }
    }

    public enum CallErrorCode {
        SUCCESS(0),
        CALL_EXIST(1),
        CANT_ACCEPT_WHILE_NOT_INVITED(2),
        ACCEPT_ERROR(3),
        JOIN_MEDIA_ROOM_FAIL(4);
        private final int value;

        CallErrorCode(int value) {
            this.value = value;
        }

        int getValue() {
            return this.value;
        }
    }
}
