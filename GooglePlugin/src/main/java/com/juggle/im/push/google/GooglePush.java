package com.juggle.im.push.google;

import android.content.Context;

import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.FirebaseMessaging;
import com.juggle.im.internal.util.JLogger;
import com.juggle.im.push.IPush;
import com.juggle.im.push.PushConfig;
import com.juggle.im.push.PushChannel;

public class GooglePush implements IPush {
    static Callback sCallback;

    @Override
    public void getToken(Context context, PushConfig config, Callback callback) {
        sCallback = callback;
        JLogger.i("CON-Push", "google get token");

        try {
            // Verify the configuration and stop if it is invalid.
            if (FirebaseOptions.fromResource(context) == null) {
                callback.onError(getType(), -1, "load fcm sdk applicationId failed");
                return;
            }
            // Trigger initialization early so FirebaseApp failures surface immediately.
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
        return PushChannel.GOOGLE;
    }
}
