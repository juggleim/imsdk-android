package com.juggle.im.push.honor;

import android.content.Context;
import android.text.TextUtils;

import com.hihonor.push.sdk.HonorPushCallback;
import com.hihonor.push.sdk.HonorPushClient;
import com.juggle.im.internal.util.JLogger;
import com.juggle.im.push.IPush;
import com.juggle.im.push.PushChannel;
import com.juggle.im.push.PushConfig;

public class HonorPush implements IPush {
    static IPush.Callback sCallback;

    @Override
    public void getToken(Context context, PushConfig config, Callback callback) {
        sCallback = callback;
        JLogger.i("CON-Push", "Honor get token, honorConfig null is " + (config.getHonorConfig() == null));
        if (config.getHonorConfig() == null) {
            return;
        }
        HonorPushClient.getInstance().init(context, false);
        HonorPushClient.getInstance().getPushToken(new HonorPushCallback<String>() {
            @Override
            public void onSuccess(String s) {
                JLogger.i("CON-Push", "honor get token success");
                if (callback != null && !TextUtils.isEmpty(s)) {
                    callback.onReceivedToken(getType(), s);
                }
            }

            @Override
            public void onFailure(int i, String s) {
                JLogger.e("CON-Push", "honor get token error, code is " + i + ", message is " + s);
                if (callback != null) {
                    callback.onError(getType(), i, s);
                }
            }
        });
    }

    @Override
    public PushChannel getType() {
        return PushChannel.HONOR;
    }
}
