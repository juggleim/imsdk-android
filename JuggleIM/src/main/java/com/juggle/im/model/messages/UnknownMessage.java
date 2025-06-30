package com.juggle.im.model.messages;

import com.juggle.im.model.MessageContent;

import java.nio.charset.StandardCharsets;

public class UnknownMessage extends MessageContent {
    @Override
    public byte[] encode() {
        return mContent.getBytes(StandardCharsets.UTF_8);
    }

    @Override
    public void decode(byte[] data) {
        mContent = new String(data, StandardCharsets.UTF_8);
    }

    public String getMessageType() {
        return mMessageType;
    }

    public void setMessageType(String messageType) {
        mMessageType = messageType;
    }

    public String getContent() {
        return mContent;
    }

    public void setContent(String content) {
        mContent = content;
    }

    private String mMessageType;
    private String mContent;
}
