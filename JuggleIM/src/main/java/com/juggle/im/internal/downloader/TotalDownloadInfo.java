package com.juggle.im.internal.downloader;

/** @author lvhongzhen */
public class TotalDownloadInfo implements IDownloadInfo {
    private String tag;
    private String savePath;
    private String downloadUrl;
    private long fileLength;

    public TotalDownloadInfo(
            String tag,
            String savePath,
            String downloadUrl,
            long fileLength) {
        this.tag = tag;
        this.savePath = savePath;
        this.downloadUrl = downloadUrl;
        this.fileLength = fileLength;
    }

    @Override
    public String getSavePath() {
        return savePath;
    }

    @Override
    public String getDownloadUrl() {
        return downloadUrl;
    }

    @Override
    public long getFileLength() {
        return fileLength;
    }

    @Override
    public long getCurrentLength() {
        return 0;
    }

    @Override
    public String getTag() {
        return tag;
    }

}
