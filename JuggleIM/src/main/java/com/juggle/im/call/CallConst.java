package com.juggle.im.call;

public class CallConst {
    // Call status.
    public enum CallStatus {
        // No active call.
        IDLE(0),
        // Incoming call.
        INCOMING(1),
        // Outgoing call.
        OUTGOING(2),
        // Connecting.
        CONNECTING(3),
        // Connected.
        CONNECTED(4),
        // Joined proactively.
        JOIN(5);
        private final int mValue;

        CallStatus(int value) {
            this.mValue = value;
        }

        public int getStatus() {
            return this.mValue;
        }

        public static CallStatus setValue(int value) {
            for (CallStatus t : CallStatus.values()) {
                if (value == t.mValue) {
                    return t;
                }
            }
            return IDLE;
        }
    }

    public enum CallMediaType {
        // Voice call.
        VOICE(0),
        // Video call.
        VIDEO(1);

        private final int mValue;

        CallMediaType(int value) {
            mValue = value;
        }

        public int getValue() {
            return mValue;
        }

        public static CallMediaType setValue(int value) {
            for (CallMediaType t : CallMediaType.values()) {
                if (value == t.mValue) {
                    return t;
                }
            }
            return VOICE;
        }
    }

    /// Call finish reason.
    public enum CallFinishReason {
        /// Unknown reason.
        UNKNOWN(0),
        /// Current user hung up a connected call.
        HANGUP(1),
        /// Current user declined the incoming call.
        DECLINE(2),
        /// Current user was busy.
        BUSY(3),
        /// Current user did not answer.
        NO_RESPONSE(4),
        /// Current user canceled the call.
        CANCEL(5),
        /// The other side hung up a connected call.
        OTHER_SIDE_HANGUP(6),
        /// The other side declined the incoming call.
        OTHER_SIDE_DECLINE(7),
        /// The other side was busy.
        OTHER_SIDE_BUSY(8),
        /// The other side did not answer.
        OTHER_SIDE_NO_RESPONSE(9),
        /// The other side canceled the call.
        OTHER_SIDE_CANCEL(10),
        /// The room was destroyed.
        ROOM_DESTROY(11),
        /// Network error.
        NETWORK_ERROR(12),
        /// Current user answered the incoming call on another client.
        ACCEPT_ON_OTHER_CLIENT(13),
        /// The current user hung up the incoming call on another client.
        HANGUP_ON_OTHER_CLIENT(14);
        private final int value;

        CallFinishReason(int value) {
            this.value = value;
        }

        public int getValue() {
            return this.value;
        }
    }

    public enum CallErrorCode {
        SUCCESS(0),
        CALL_EXIST(1),
        CANT_ACCEPT_WHILE_NOT_INVITED(2),
        ACCEPT_FAIL(3),
        JOIN_MEDIA_ROOM_FAIL(4),
        INVALID_PARAMETER(5),
        INVITE_FAIL(6),
        JOIN_ROOM_FAIL(7);

        private final int value;

        CallErrorCode(int value) {
            this.value = value;
        }

        public int getValue() {
            return this.value;
        }
    }
}
