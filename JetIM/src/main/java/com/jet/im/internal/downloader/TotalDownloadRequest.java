package com.jet.im.internal.downloader;

import java.net.HttpURLConnection;

/**
 * 基本的 request 不支持断点续传,只能取消，不能暂停 多媒体下载Reuqest
 *
 * @author lvhongzhen
 */
public class TotalDownloadRequest extends BaseDownloadRequest<IDownloadInfo> {

    protected TotalDownloadRequest(IDownloadInfo downloadInfo, RequestCallback callback) {
        super(downloadInfo, callback);
    }

    @Override
    protected void setRequestProperty(HttpURLConnection conn) {
        // Do nothing
    }

    @Override
    protected boolean appendOutputStream() {
        return false;
    }

    @Override
    protected void onWriteFile(long total, long current, int length) {
        // Do nothing
    }
}
