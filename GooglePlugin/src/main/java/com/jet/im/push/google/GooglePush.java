package com.jet.im.push.google;

import android.content.Context;

import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.FirebaseMessaging;
import com.jet.im.push.IPush;
import com.jet.im.push.PushConfig;
import com.jet.im.push.PushChannel;

public class GooglePush implements IPush {
    static Callback sCallback;

    @Override
    public void getToken(Context context, PushConfig config, Callback callback) {
        sCallback = callback;
        // 检验是否正确配置，如果配置有问题，不再往下执行
        if (FirebaseOptions.fromResource(context) == null) {
            callback.onError(getType(), -1, "load fcm sdk applicationId failed");
            return;
        }
        try {
            // 提前触发一次初始化，确保某些情况下FirebaseApp初始化失败，抛出异常
            FirebaseApp.initializeApp(context);
        } catch (Exception e) {
            callback.onError(getType(), -1, e.getMessage());
            return;
        }
        try {
            FirebaseMessaging.getInstance().setAutoInitEnabled(true);
            FirebaseMessaging.getInstance()
                    .getToken()
                    .addOnCompleteListener(
                            task -> {
                                if (!task.isSuccessful()) {
                                    Exception exception = task.getException();
                                    callback.onError(getType(), -1, exception == null ? "get fcm token error" : exception.getMessage());
                                    return;
                                }

                                String token = task.getResult();
                                callback.onReceivedToken(getType(), token);
                            });
        } catch (Exception e) {
            callback.onError(getType(), -1, e.getMessage());
        }
    }

    @Override
    public PushChannel getType() {
        return PushChannel.XIAOMI;
    }
}
