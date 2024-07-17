package com.jet.im.internal.downloader;

import java.net.HttpURLConnection;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 上传 或 下载的基类
 *
 * @author lvhongzhen
 */
public abstract class BaseRequest<T extends RequestCallback> {
    public static final String METHOD_GET = "GET";
    public static final String METHOD_HEAD = "HEAD";
    public static final String METHOD_POST = "POST";
    public static final String METHOD_PUT = "PUT";
    public static final String HEADER_ACCEPT_ENCODING = "Accept-Encoding";
    public static final String ACCEPT_ENCODING_IDENTITY = "identity";
    public static final String HEADER_CONTENT_LENGTH = "Content-Length";
    public static final String HEADER_RANGE = "Range";
    public static final String HEADER_CONTENT_RANGE = "Content-Range";
    public static final String RANGE_0_1 = "bytes=0-1";
    public static final String HEADER_FILED_CONTENT_ENCODING = "Content-Encoding";
    public static final String HEADER_FILED_CONTENT_LENGTH = "Content-Length";
    public static final String HEADER_CONNECTION = "Connection";
    public static final String CONNECTION_CLOSE = "close";
    public static final String CONTENT_ENCODING_GZIP = "gzip";
    protected String tag;
    protected AtomicBoolean isCancel = new AtomicBoolean(false);
    protected int retryCount;
    protected T requestCallback;

    protected BaseRequest(String tag, T requestCallback) {
        this.tag = tag;
        this.requestCallback = requestCallback;
    }

    public static void disconnect(HttpURLConnection conn) {
        if (conn == null) {
            return;
        }
        conn.disconnect();
    }

    public String getTag() {
        return tag;
    }

    public void cancel() {
        isCancel.set(true);
    }

    public void retry() {
        run();
    }

    /** 需要执行的代码逻辑，上传 或 下载 */
    public abstract void run();
}
