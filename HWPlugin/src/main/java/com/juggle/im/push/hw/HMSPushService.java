package com.juggle.im.push.hw;

import com.huawei.hms.push.HmsMessageService;
import com.juggle.im.push.PushChannel;

public class HMSPushService extends HmsMessageService {
    @Override
    public void onNewToken(String pS) {
        super.onNewToken(pS);
        if (HWPush.sCallback != null) {
            HWPush.sCallback.onReceivedToken(PushChannel.HUAWEI, pS);
        }
    }

    @Override
    public void onTokenError(Exception e) {
        super.onTokenError(e);
        if (HWPush.sCallback != null) {
            HWPush.sCallback.onError(PushChannel.HUAWEI, -1, e.getMessage());
        }
    }
}
