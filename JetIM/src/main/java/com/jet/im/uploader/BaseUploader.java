package com.jet.im.uploader;

import com.jet.im.internal.util.JThreadPoolExecutor;

/**
 * @author Ye_Guli
 * @create 2024-05-29 9:04
 */
public abstract class BaseUploader implements IUploader {
    private static final long PROGRESS_CALLBACK_INTERVAL = 500;//进度回调时间间隔

    private final UploaderCallback mUploaderCallback;
    protected final String mLocalPath;
    private volatile long mLastProgressCallbackTime = 0;//上次进度回调时间

    public BaseUploader(String localPath, UploaderCallback uploaderCallback) {
        this.mLocalPath = localPath;
        this.mUploaderCallback = uploaderCallback;
    }

    protected void notifyProgress(int progress) {
        long currentTime = System.currentTimeMillis();
        if (currentTime - mLastProgressCallbackTime >= PROGRESS_CALLBACK_INTERVAL) {
            JThreadPoolExecutor.runOnMainThread(() -> {
                if (mUploaderCallback != null) {
                    mUploaderCallback.onProgress(progress);
                }
            });
            mLastProgressCallbackTime = currentTime;
        }
    }

    protected void notifySuccess(String url) {
        JThreadPoolExecutor.runOnMainThread(() -> {
            if (mUploaderCallback != null) {
                mUploaderCallback.onSuccess(url);
            }
        });
    }

    protected void notifyFail() {
        JThreadPoolExecutor.runOnMainThread(() -> {
            if (mUploaderCallback != null) {
                mUploaderCallback.onError();
            }
        });
    }

    protected void notifyCancel() {
        JThreadPoolExecutor.runOnMainThread(() -> {
            if (mUploaderCallback != null) {
                mUploaderCallback.onCancel();
            }
        });
    }
}