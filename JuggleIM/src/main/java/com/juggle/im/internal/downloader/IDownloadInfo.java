package com.juggle.im.internal.downloader;

/**
 * 获取下载信息
 *
 * @author lvhongzhen
 */
public interface IDownloadInfo {
    /**
     * 本地保存文件地址
     *
     * @return
     */
    String getSavePath();

    /**
     * 下载地址
     *
     * @return
     */
    String getDownloadUrl();

    /**
     * 文件大小
     *
     * @return
     */
    long getFileLength();

    /**
     * 已经下载的文件大小
     *
     * @return
     */
    long getCurrentLength();

    /**
     * 任务标识
     *
     * @return
     */
    String getTag();

}
