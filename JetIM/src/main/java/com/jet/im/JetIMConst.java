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

    public enum PullDirection {
        NEWER,OLDER
    }
}
