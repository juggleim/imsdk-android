package com.juggle.im;

public class JIMConst {
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

        public static ConnectionStatus setStatus(int status) {
            for (ConnectionStatus s : ConnectionStatus.values()) {
                if (status == s.status) {
                    return s;
                }
            }
            return IDLE;
        }
    }
    public enum PullDirection {
        NEWER, OLDER
    }
}
