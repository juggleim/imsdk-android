package com.jet.im.push.hw;

import com.huawei.hms.push.HmsMessageService;
import com.jet.im.push.PushType;

/**
 * Created by Android Studio. User: lvhongzhen Date: 2019-12-25 Time: 14:23
 */
public class HMSPushService extends HmsMessageService {
    @Override
    public void onNewToken(String pS) {
        super.onNewToken(pS);
        if (HWPush.sCallback != null) {
            HWPush.sCallback.onReceivedToken(PushType.HUAWEI, pS);
        }
    }

    @Override
    public void onTokenError(Exception e) {
        super.onTokenError(e);
        if (HWPush.sCallback != null) {
            HWPush.sCallback.onError(PushType.HUAWEI, -1, e.getMessage());
        }
    }
}
