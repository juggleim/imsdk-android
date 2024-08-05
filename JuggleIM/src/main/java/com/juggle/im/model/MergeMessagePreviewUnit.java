package com.juggle.im.model;

public class MergeMessagePreviewUnit {
    public String getPreviewContent() {
        return previewContent;
    }

    public void setPreviewContent(String previewContent) {
        this.previewContent = previewContent;
    }

    public UserInfo getSender() {
        return sender;
    }

    public void setSender(UserInfo sender) {
        this.sender = sender;
    }

    private String previewContent;
    private UserInfo sender;
}
