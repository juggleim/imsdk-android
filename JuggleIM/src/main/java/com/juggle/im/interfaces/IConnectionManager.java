package com.juggle.im.interfaces;

import com.juggle.im.JIMConst;
import com.juggle.im.push.PushChannel;

public interface IConnectionManager {
    void connect(String token);

    void disconnect(boolean receivePush);

    void registerPushToken(PushChannel channel, String token);

    /**
     * 设置语言（主要影响内置消息的推送语言，目前支持中/英文）
     * @param language 语言，例 “en”、“zh”
     * @param callback 结果回调
     */
    void setLanguage(String language, ISimpleCallback callback);

    JIMConst.ConnectionStatus getConnectionStatus();

    void addConnectionStatusListener(String key, IConnectionStatusListener listener);

    void removeConnectionStatusListener(String key);

    interface IConnectionStatusListener {
        void onStatusChange(JIMConst.ConnectionStatus status, int code, String extra);

        void onDbOpen();

        void onDbClose();
    }

    interface ISimpleCallback {
        void onSuccess();

        void onError(int errorCode);
    }
}


