package com.jet.im.push.oppo;

import android.content.Context;

import com.heytap.msp.push.mode.DataMessage;
import com.heytap.msp.push.service.CompatibleDataMessageCallbackService;
import com.heytap.msp.push.service.DataMessageCallbackService;

public class OPPOPushCompatibleService extends CompatibleDataMessageCallbackService {
    @Override
    public void processMessage(Context context, DataMessage dataMessage) {
        super.processMessage(context, dataMessage);
    }
}
