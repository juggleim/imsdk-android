package com.juggle.im.model;

public abstract class MediaMessageContent extends MessageContent {
    private String mLocalPath;
    private String mUrl;

    public String getLocalPath() {
        return mLocalPath;
    }

    public void setLocalPath(String localPath) {
        this.mLocalPath = localPath;
    }

    public String getUrl() {
        return mUrl;
    }

    public void setUrl(String url) {
        this.mUrl = url;
    }
}