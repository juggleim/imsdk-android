package com.jet.im.push.vivo;

import android.content.Context;

import com.jet.im.push.IPush;
import com.jet.im.push.PushChannel;
import com.jet.im.push.PushConfig;
import com.vivo.push.IPushActionListener;
import com.vivo.push.PushClient;
import com.vivo.push.listener.IPushQueryActionListener;
import com.vivo.push.util.VivoPushException;

public class VIVOPush implements IPush {
    static IPush.Callback sCallback;

    @Override
    public void getToken(Context context, PushConfig config, IPush.Callback callback) {
        sCallback = callback;
        if (config.getVIVOConfig() == null) {
            return;
        }
        try {
            com.vivo.push.PushConfig pushConfig = new com.vivo.push.PushConfig.Builder()
                    .agreePrivacyStatement(true)
                    .build();
            PushClient.getInstance(context).initialize(pushConfig);
            PushClient.getInstance(context).turnOnPush(new IPushActionListener() {
                @Override
                public void onStateChanged(int state) {
                    if (state != 0) {
                        if (sCallback != null) {
                            sCallback.onError(PushChannel.VIVO, state, "vivo error code: " + state);
                        }
                        return;
                    }
                    PushClient.getInstance(context).getRegId(new IPushQueryActionListener() {
                        @Override
                        public void onSuccess(String s) {
                            if (sCallback != null) {
                                sCallback.onReceivedToken(PushChannel.VIVO, s);
                            }
                        }

                        @Override
                        public void onFail(Integer integer) {
                            if (sCallback != null) {
                                sCallback.onError(PushChannel.VIVO, integer, "vivo error code: " + integer);
                            }
                        }
                    });
                }
            });
        } catch (Throwable e) {
            int errorCode = e instanceof VivoPushException ? ((VivoPushException) e).getCode() : -1;
            callback.onError(getType(), errorCode, e.getMessage());
        }
    }

    @Override
    public PushChannel getType() {
        return PushChannel.VIVO;
    }
}
