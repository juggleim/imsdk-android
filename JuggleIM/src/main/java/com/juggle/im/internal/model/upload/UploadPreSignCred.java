package com.juggle.im.internal.model.upload;

import androidx.annotation.NonNull;

/**
 * @author Ye_Guli
 * @create 2024-05-28 16:10
 */
public class UploadPreSignCred {
    private String mUrl;
    private String mDownloadUrl;

    public void setUrl(String url) {
        this.mUrl = url;
    }

    public String getUrl() {
        return mUrl;
    }

    public String getDownloadUrl() {
        return mDownloadUrl;
    }

    public void setDownloadUrl(String downloadUrl) {
        mDownloadUrl = downloadUrl;
    }

    @NonNull
    @Override
    public String toString() {
        return "UploadPreSignCred{" +
                "mUrl='" + mUrl + '\'' + ", mDownloadUrl='" + mDownloadUrl + '\'' +
                '}';
    }
}