package com.juggle.im.internal.core.network;

import com.juggle.im.internal.model.ConcreteMessage;

import java.util.List;

public abstract class QryHisMsgCallback implements IWebSocketCallback {
    public abstract void onSuccess(List<ConcreteMessage> messages, boolean isFinished);
    public abstract void onError(int errorCode);
}
