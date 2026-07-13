package com.juggle.im.internal.downloader;

/**
 * Get download info
 *
 * @author lvhongzhen
 */
public interface IDownloadInfo {
    /**
     * Local saved file path
     *
     * @return
     */
    String getSavePath();

    /**
     * Download URL
     *
     * @return
     */
    String getDownloadUrl();

    /**
     * File size
     *
     * @return
     */
    long getFileLength();

    /**
     * Downloaded file size
     *
     * @return
     */
    long getCurrentLength();

    /**
     * Task identifier
     *
     * @return
     */
    String getTag();

}
