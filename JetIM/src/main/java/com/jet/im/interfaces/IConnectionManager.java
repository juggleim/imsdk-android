package com.jet.im.interfaces;

public interface IConnectionManager {
    void connect(String token);

    void disconnect(boolean receivePush);

    void addConnectionStatusListener(String key, IConnectionStatusListener listener);

    void removeConnectionStatusListener(String key);

    interface IConnectionStatusListener {
        void onStatusChange(ConnectionStatus status, int code);

        enum ConnectionStatus {
            IDLE(0),
            CONNECTED(1),
            DISCONNECTED(2),
            CONNECTING(3),
            FAILURE(4);
            private int status;
            ConnectionStatus(int status) {
                this.status = status;
            }
            int getStatus() {
                return this.status;
            }
        }
    }
}


