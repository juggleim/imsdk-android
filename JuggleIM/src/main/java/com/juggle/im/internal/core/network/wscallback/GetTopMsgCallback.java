package com.juggle.im.internal.core.network.wscallback;

import com.juggle.im.internal.model.ConcreteMessage;
import com.juggle.im.model.UserInfo;

public abstract class GetTopMsgCallback implements IWebSocketCallback {
    public abstract void onSuccess(ConcreteMessage message, UserInfo userInfo, long timestamp);
    public abstract void onError(int errorCode);
}
