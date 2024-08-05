package com.juggle.im.push.oppo;

import android.content.Context;

import com.heytap.msp.push.HeytapPushManager;
import com.heytap.msp.push.callback.ICallBackResultService;
import com.juggle.im.push.IPush;
import com.juggle.im.push.PushChannel;
import com.juggle.im.push.PushConfig;

public class OPPOPush implements IPush {
    static IPush.Callback sCallback;

    @Override
    public void getToken(Context context, PushConfig config, IPush.Callback callback) {
        sCallback = callback;
        if (config.getOPPOConfig() == null) {
            return;
        }
        try {
            HeytapPushManager.init(context, false);
            if (!HeytapPushManager.isSupportPush(context)) {
                return;
            }
            HeytapPushManager.register(context, config.getOPPOConfig().getAppKey(), config.getOPPOConfig().getAppSecret(), new ICallBackResultService() {
                @Override
                public void onRegister(int responseCode, String registerID, String packageName, String miniPackageName) {
                    if (responseCode != 0) {
                        if (sCallback != null) {
                            sCallback.onError(PushChannel.OPPO, responseCode, "oppo error code: " + responseCode);
                        }
                        return;
                    }
                    if (sCallback != null) {
                        sCallback.onReceivedToken(PushChannel.OPPO, registerID);
                    }
                }

                @Override
                public void onUnRegister(int responseCode, String packageName, String miniProgramPkg) {
                }

                @Override
                public void onSetPushTime(int responseCode, String pushTime) {
                }

                @Override
                public void onGetPushStatus(int responseCode, int status) {
                }

                @Override
                public void onGetNotificationStatus(int responseCode, int status) {
                }

                @Override
                public void onError(int errorCode, String message, String packageName, String miniProgramPkg) {
                }
            });
        } catch (Throwable e) {
            callback.onError(getType(), -1, e.getMessage());
        }
    }

    @Override
    public PushChannel getType() {
        return PushChannel.OPPO;
    }
}
