package com.juggle.im.model;

import java.util.List;

public class MessageReaction {

    public String getMessageId() {
        return mMessageId;
    }

    public void setMessageId(String messageId) {
        mMessageId = messageId;
    }

    public List<MessageReactionItem> getItemList() {
        return mItemList;
    }

    public void setItemList(List<MessageReactionItem> itemList) {
        mItemList = itemList;
    }

    private String mMessageId;
    private List<MessageReactionItem> mItemList;
}
