package com.juggle.im.internal.downloader;

/** @author lvhongzhen */
public interface RequestCallback {
    /**
     * Callback for successful download
     *
     * @param savePath Media storage path
     */
    void onSuccess(String savePath);

    /**
     * Callback for download file exceptions
     *
     * @param request Local download task
     * @param e Error reason
     */
    void onError(BaseDownloadRequest request, Throwable e);

    /**
     * Download file progress
     *
     * @param progress Progress value
     */
    void onProgress(int progress);

    /**
     * Cancel the task
     *
     * @param tag
     */
    void onCancel(String tag);
}
