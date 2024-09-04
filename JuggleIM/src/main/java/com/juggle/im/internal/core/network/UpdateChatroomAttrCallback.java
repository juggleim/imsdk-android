package com.juggle.im.internal.core.network;

import com.juggle.im.internal.model.ChatroomAttributeItem;

import java.util.List;

public abstract class UpdateChatroomAttrCallback implements IWebSocketCallback {
    public abstract void onComplete(int code, List<ChatroomAttributeItem> items);
}
