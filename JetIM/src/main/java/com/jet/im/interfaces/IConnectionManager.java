package com.jet.im.interfaces;

import com.jet.im.JetIMConst;
import com.jet.im.push.PushChannel;

public interface IConnectionManager {
    void connect(String token);

    void disconnect(boolean receivePush);

    void registerPushToken(PushChannel channel, String token);

    void addConnectionStatusListener(String key, IConnectionStatusListener listener);

    void removeConnectionStatusListener(String key);

    interface IConnectionStatusListener {
        void onStatusChange(JetIMConst.ConnectionStatus status, int code);

        void onDbOpen();
    }
}


