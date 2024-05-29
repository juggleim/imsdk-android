package com.jet.im.uploader;

/**
 * @author Ye_Guli
 * @create 2024-05-29 9:04
 */
public abstract class BaseUploader implements IUploader {
    private final UploaderCallback mUploaderCallback;
    protected final String mLocalPath;

    public BaseUploader(String localPath, UploaderCallback uploaderCallback) {
        this.mLocalPath = localPath;
        this.mUploaderCallback = uploaderCallback;
    }

    protected void notifyProgress(int progress) {
        if (mUploaderCallback != null) {
            mUploaderCallback.onProgress(progress);
        }
    }

    protected void notifySuccess(String url) {
        if (mUploaderCallback != null) {
            mUploaderCallback.onSuccess(url);
        }
    }

    protected void notifyFail() {
        if (mUploaderCallback != null) {
            mUploaderCallback.onError();
        }
    }

    protected void notifyCancel() {
        if (mUploaderCallback != null) {
            mUploaderCallback.onCancel();
        }
    }
}