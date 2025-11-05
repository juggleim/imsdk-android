package com.juggle.im.internal.core.network.wscallback;

import com.juggle.im.model.FavoriteMessage;

import java.util.List;

public abstract class GetFavoriteMsgCallback implements IWebSocketCallback {
    public abstract void onSuccess(List<FavoriteMessage> messageList, String offset);
    public abstract void onError(int errorCode);
}
