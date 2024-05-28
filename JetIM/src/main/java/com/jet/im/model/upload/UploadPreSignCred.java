package com.jet.im.model.upload;

import androidx.annotation.NonNull;

/**
 * @author Ye_Guli
 * @create 2024-05-28 16:10
 */
public class UploadPreSignCred {
    private String mUrl;

    public void setUrl(String url) {
        this.mUrl = url;
    }

    public String getUrl() {
        return mUrl;
    }

    @NonNull
    @Override
    public String toString() {
        return "UploadPreSignCred{" +
                "mUrl='" + mUrl + '\'' +
                '}';
    }
}