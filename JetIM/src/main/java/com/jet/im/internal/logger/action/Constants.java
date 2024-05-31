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
}