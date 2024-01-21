package com.jet.im.interfaces;

import com.jet.im.JetIMConst;

public interface IConnectionManager {
    void connect(String token);

    void disconnect(boolean receivePush);

    void addConnectionStatusListener(String key, IConnectionStatusListener listener);

    void removeConnectionStatusListener(String key);

    interface IConnectionStatusListener {
        void onStatusChange(JetIMConst.ConnectionStatus status, int code);
        void onDbOpen();
    }
}


