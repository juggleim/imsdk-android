package com.jet.im.internal.logger.action;

/**
 * @author Ye_Guli
 * @create 2024-05-23 11:09
 */
class Constants {
    static final int DEFAULT_QUEUE = 500;
    static final int MINUTE = 60 * 1000;
    static final int HOUR = 60 * MINUTE;
    static final long M = 1024 * 1024;
    static final long DEFAULT_MAX_USE_SIZE = 100 * M; //日志文件目录的最大使用限制
    static final String LOG_FILE_SUFFIX = ".jlog";
    static final String ZIP_FILE_SUFFIX = ".zip";
    static final String LOG_TIMESTAMP_FORMAT = "yyyyMMddHH";
    static final String LOG_TIMESTAMP_FORMAT_DETAILED = "yyyy-MM-dd HH:mm:ss SSSS";

    static final int LOG_UPLOAD_TIME_OUT = 15 * 1000;
    static final String LOG_UPLOAD_PREFIX = "--";
    static final String LOG_UPLOAD_LINE_END = "\r\n";
    static final String LOG_UPLOAD_CHARSET = "UTF-8";
    static final String LOG_UPLOAD_KEEP_ALIVE = "keep-alive";
}