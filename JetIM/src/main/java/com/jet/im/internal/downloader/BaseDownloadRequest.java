package com.jet.im.internal.downloader;

import android.text.TextUtils;
import android.util.Log;

import com.jet.im.internal.exception.HttpException;
import com.jet.im.internal.util.NetUtils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;

/**
 * 多媒体下载基类
 *
 * @author lvhongzhen
 */
public abstract class BaseDownloadRequest<V extends IDownloadInfo>
        extends BaseRequest<RequestCallback> {
    /**
     * 默认 60 秒超时时间
     */
    protected static final int TIMEOUT = 60 * 1000;

    private static final String TAG = BaseDownloadRequest.class.getSimpleName();
    protected V downloadInfo;
    protected RequestCallback callback;

    protected BaseDownloadRequest(V downloadInfo, RequestCallback callback) {
        super(downloadInfo.getTag(), callback);
        this.downloadInfo = downloadInfo;
        this.callback = callback;
    }

    @Override
    public void run() {
        download();
    }

    protected void download() {
        // 任务不存在直接取消
        if (!MediaDownloadEngine.getInstance().existsTask(tag)) {
            return;
        }
        HttpURLConnection conn = null;
        try {
            conn = NetUtils.createURLConnection(downloadInfo.getDownloadUrl());
            conn.setConnectTimeout(TIMEOUT);
            conn.setReadTimeout(TIMEOUT);
            conn.setUseCaches(false);
            conn.setRequestMethod(METHOD_GET);
            conn.setDoInput(true);
            conn.setRequestProperty(HEADER_CONNECTION, CONNECTION_CLOSE);
            conn.setRequestProperty(HEADER_ACCEPT_ENCODING, ACCEPT_ENCODING_IDENTITY);
            setRequestProperty(conn);
            conn.connect();
            int responseCode = conn.getResponseCode();
            if (responseCode < HttpURLConnection.HTTP_OK
                    || responseCode >= HttpURLConnection.HTTP_MULT_CHOICE) {
                callback.onError(this, new HttpException(responseCode));
                return;
            }
            try (OutputStream out =
                         new BufferedOutputStream(
                                 new FileOutputStream(
                                         downloadInfo.getSavePath(), appendOutputStream()));
                 InputStream inputStream = new BufferedInputStream(conn.getInputStream())) {
                if (!writeInputStream(
                        inputStream,
                        out,
                        downloadInfo.getCurrentLength(),
                        getContentLength(conn))) {
                    return;
                }
            }
            callback.onSuccess(downloadInfo.getSavePath());

        } catch (Exception e) {
            callback.onError(this, e);
            disconnect(conn);
        } finally {
            disconnect(conn);
        }
    }

    /**
     * 可以通过此方法设置头信息等
     *
     * @param conn HttpURLConnection 对象
     */
    protected abstract void setRequestProperty(HttpURLConnection conn);

    /**
     * 确认文件是覆盖写入还是继续写入
     *
     * @return true，文件继续写入，false 覆盖写入
     */
    protected abstract boolean appendOutputStream();

    //    private boolean supportGzip(HttpURLConnection conn) {
    //        String contentEncoding = conn.getHeaderField(HEADER_FILED_CONTENT_ENCODING);
    //        return CONTENT_ENCODING_GZIP.equals(contentEncoding);
    //    }

    private boolean writeInputStream(
            InputStream inputStream, final OutputStream raf, long current, long total)
            throws IOException {
        if (total <= 0) {
            total = 1;
        }
        int length;
        int progress = (int) (100L * current / total);
        int temp;
        byte[] buffer = new byte[1024 * 512];
        while ((length = inputStream.read(buffer)) != -1) {
            if (isCancel.get()) {
                return false;
            }
            raf.write(buffer, 0, length);
            current += length;
            onWriteFile(total, current, length);
            temp = (int) (100L * current / total);
            if (progress >= temp) {
                continue;
            }
            progress = temp;
            callback.onProgress(temp);
        }
        return true;
    }

    private long getContentLength(HttpURLConnection conn) {
        String contentLength = conn.getHeaderField(HEADER_FILED_CONTENT_LENGTH);
        if (TextUtils.isEmpty(contentLength)) {
            return downloadInfo.getFileLength();
        }
        try {
            return Long.parseLong(contentLength);
        } catch (NumberFormatException e) {
            Log.e(TAG, "string can not cast to long,string is" + contentLength);
            return downloadInfo.getFileLength();
        }
    }

    /**
     * 当开始写入文件的回调
     *
     * @param total   文件总大小
     * @param current 当前已经下载大小
     * @param length  当次读取大小
     */
    protected abstract void onWriteFile(long total, long current, int length);
}
