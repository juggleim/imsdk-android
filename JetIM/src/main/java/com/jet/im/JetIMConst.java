package com.jet.im;

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
    }

    public enum PullDirection {
        NEWER,OLDER
    }
}
