package com.juggle.im.interfaces;

import com.juggle.im.JIMConst;
import com.juggle.im.push.PushChannel;

import java.util.Map;

public interface IConnectionManager {
    void connect(String token);

    void disconnect(boolean receivePush);

    void registerPushToken(PushChannel channel, String token);

    /**
     * Sets the language. This mainly affects the push language for built-in messages.
     * Currently supports English and Chinese.
     * @param language Language, for example "en" or "zh".
     * @param callback Result callback.
     */
    void setLanguage(String language, ISimpleCallback callback);

    /**
     * Gets the language. This mainly affects the push language for built-in messages.
     * Currently supports English and Chinese.
     * @param callback Result callback.
     */
    void getLanguage(JIMConst.IResultCallback<String> callback);

    JIMConst.ConnectionStatus getConnectionStatus();

    void setConnectParams(String signKey, Map<String, String> headers);

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

