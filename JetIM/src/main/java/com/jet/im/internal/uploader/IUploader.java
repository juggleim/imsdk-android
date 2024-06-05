package com.jet.im.internal.uploader;

/**
 * @author Ye_Guli
 * @create 2024-05-28 15:27
 */
public interface IUploader {
    void start();

    void cancel();

    interface UploaderCallback {
        void onProgress(int progress);

        void onSuccess(String url);

        void onError();

        void onCancel();
    }
}