package com.juggle.im.internal.downloader;

import java.net.HttpURLConnection;

/**
 * Basic request for media download. It does not support resume; it can only be canceled, not paused
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
