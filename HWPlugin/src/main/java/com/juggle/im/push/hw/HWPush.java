package com.juggle.im.push.hw;

import android.content.Context;
import android.text.TextUtils;

import com.huawei.hms.aaid.HmsInstanceId;
import com.huawei.hms.common.ApiException;
import com.juggle.im.push.IPush;
import com.juggle.im.push.PushConfig;
import com.juggle.im.push.PushChannel;

public class HWPush implements IPush {
    static IPush.Callback sCallback;

    @Override
    public void getToken(Context context, PushConfig config, IPush.Callback callback) {
        sCallback = callback;
        if (config.getHWConfig() == null) {
            return;
        }
        try {
            String token = HmsInstanceId.getInstance(context).getToken(config.getHWConfig().getAppId(), "HCM");
            if (!TextUtils.isEmpty(token)) {
                callback.onReceivedToken(getType(), token);
            }
        } catch (Throwable e) {
            int errorCode = e instanceof ApiException ? ((ApiException) e).getStatusCode() : -1;
            callback.onError(getType(), errorCode, e.getMessage());
        }
    }

    @Override
    public PushChannel getType() {
        return PushChannel.HUAWEI;
    }
}
