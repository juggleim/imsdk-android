package com.jet.im.internal.downloader;

/** @author lvhongzhen */
public interface RequestCallback {
    /**
     * 下载成功的回调
     *
     * @param savePath 媒体存放地址
     */
    void onSuccess(String savePath);

    /**
     * 下载文件异常回调
     *
     * @param request 本地下载任务
     * @param e 出错原因
     */
    void onError(BaseDownloadRequest request, Throwable e);

    /**
     * 下载文件进度
     *
     * @param progress 进度值
     */
    void onProgress(int progress);

    /**
     * 取消任务
     *
     * @param tag
     */
    void onCancel(String tag);
}
