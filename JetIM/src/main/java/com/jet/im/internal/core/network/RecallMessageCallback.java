package com.jet.im.internal.core.network;

public abstract class RecallMessageCallback  implements IWebSocketCallback {
    public RecallMessageCallback(String messageId) {
        this.mMessageId = messageId;
    }

    public abstract void onSuccess(long timestamp);
    public abstract void onError(int errorCode);
    public String getMessageId() {
        return mMessageId;
    }

    private final String mMessageId;
}
