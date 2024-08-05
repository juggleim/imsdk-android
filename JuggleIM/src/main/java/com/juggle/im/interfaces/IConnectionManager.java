package com.juggle.im.interfaces;

import com.juggle.im.JIMConst;
import com.juggle.im.push.PushChannel;

public interface IConnectionManager {
    void connect(String token);

    void disconnect(boolean receivePush);

    void registerPushToken(PushChannel channel, String token);

    JIMConst.ConnectionStatus getConnectionStatus();

    void addConnectionStatusListener(String key, IConnectionStatusListener listener);

    void removeConnectionStatusListener(String key);

    interface IConnectionStatusListener {
        void onStatusChange(JIMConst.ConnectionStatus status, int code, String extra);

        void onDbOpen();

        void onDbClose();
    }
}


