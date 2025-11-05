package com.juggle.im.internal.core.network.wscallback;

import com.juggle.im.model.MessageReaction;

import java.util.List;

public abstract class MessageReactionListCallback implements IWebSocketCallback {
    public abstract void onSuccess(List<MessageReaction> reactionList);
    public abstract void onError(int errorCode);
}
