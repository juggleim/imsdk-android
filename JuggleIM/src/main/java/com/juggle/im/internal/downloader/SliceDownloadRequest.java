package com.juggle.im.internal.downloader;

import java.net.HttpURLConnection;

/**
 * 分片下载
 *
 * @author lvhongzhen
 */
public class SliceDownloadRequest extends BaseDownloadRequest<DownloadInfo.SliceInfo> {
    private DownloadInfo info;

    protected SliceDownloadRequest(
            DownloadInfo info, DownloadInfo.SliceInfo sliceInfo, RequestCallback callback) {
        super(sliceInfo, callback);
        this.info = info;
    }

    @Override
    protected void setRequestProperty(HttpURLConnection conn) {
        conn.setRequestProperty(
                "Range",
                "bytes=" + downloadInfo.getCurrentRange() + "-" + downloadInfo.getEndRange());
    }

    @Override
    protected boolean appendOutputStream() {
        return true;
    }

    @Override
    protected void onWriteFile(long total, long current, int length) {
        downloadInfo.setCurrentLength(current);
    }

    public DownloadInfo getInfo() {
        return info;
    }
}
