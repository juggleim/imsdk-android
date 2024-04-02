package com.jet.im;

import com.jet.im.push.PushType;

public class JetIMConst {
    public enum ConnectionStatus {
        IDLE(0),
        CONNECTED(1),
        DISCONNECTED(2),
        CONNECTING(3),
        FAILURE(4);
        private final int status;

        ConnectionStatus(int status) {
            this.status = status;
        }

        int getStatus() {
            return this.status;
        }
    }

    public enum PushChannel {
        DEFAULT(0),
        HUAWEI(2),
        XIAOMI(3);
        private final int value;

        PushChannel(int value) {
            this.value = value;
        }

        public int getValue() {
            return this.value;
        }

        public static PushChannel getPushChannel(PushType type) {
            PushChannel result = DEFAULT;
            switch (type) {
                case HUAWEI:
                    result = HUAWEI;
                    break;
                case XIAOMI:
                    result = XIAOMI;
                    break;
            }
            return result;
        }
    }

    public enum PullDirection {
        NEWER, OLDER
    }
}
