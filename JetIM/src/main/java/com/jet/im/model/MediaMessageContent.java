package com.jet.im.model;

import com.jet.im.model.upload.UploadFileType;

public abstract class MediaMessageContent extends MessageContent {
    private String mLocalPath;
    protected String mUrl;

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

    public abstract UploadFileType getUploadFileType();
}