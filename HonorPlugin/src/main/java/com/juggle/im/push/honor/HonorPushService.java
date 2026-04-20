package com.juggle.im.push.honor;

import com.hihonor.push.sdk.HonorMessageService;
import com.juggle.im.push.PushChannel;

public class HonorPushService extends HonorMessageService {
    @Override
    public void onNewToken(String s) {
        super.onNewToken(s);
        if (HonorPush.sCallback != null) {
            HonorPush.sCallback.onReceivedToken(PushChannel.HONOR, s);
        }
    }
}
