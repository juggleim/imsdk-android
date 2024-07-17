package com.jet.im.internal.downloader;

import android.text.TextUtils;
import android.util.Log;

import com.jet.im.JErrorCode;
import com.jet.im.internal.exception.HttpException;
import com.jet.im.internal.util.FileUtils;
import com.jet.im.internal.util.NetUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.net.HttpURLConnection;
import java.nio.channels.FileChannel;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class MediaDownloadEngine {
    protected TaskDispatcher taskDispatcher = new TaskDispatcher();

    private static final int SLICE_COUNT = 4;
    private static final int TIMEOUT = 5 * 1000;
    private static final String TAG = MediaDownloadEngine.class.getSimpleName();
    private static final String FILE_PATH = "filePath";
    private static final String INFO_PATH = "infoPath";
    private static final String URL = "url";
    private static final String LENGTH = "length";
    private static final String TASK_TAG = "tag";
    private static final String IS_DOWN_LOADING = "isDownLoading";
    private static final String SLICE_INFO_PATH_LIST = "sliceInfoPathList";
    private static final String PART_NUMBER = "partNumber";
    private static final String SLICE_PATH = "slicePath";
    private static final String START_RANGE = "startRange";
    private static final String END_RANGE = "endRange";
    private static final String MAX_LENGTH = "maxLength";
    private static final String PROPORTION = "proportion";

    private MediaDownloadEngine() {
        // default implementation ignored
    }

    public static MediaDownloadEngine getInstance() {
        return SingletonHolder.INSTANCE;
    }

    public static DownloadInfo getFileInfoFromJson(String jsonString) {
        try {
            DownloadInfo downloadInfo = new DownloadInfo();
            JSONObject jsonObject = new JSONObject(jsonString);
            downloadInfo.setFilePath(jsonObject.getString(FILE_PATH));
            downloadInfo.setInfoPath(jsonObject.getString(INFO_PATH));
            downloadInfo.setUrl(jsonObject.getString(URL));
            downloadInfo.setLength(jsonObject.getLong(LENGTH));
            downloadInfo.setTag(jsonObject.getString(TASK_TAG));
            downloadInfo.setDownLoading(jsonObject.getBoolean(IS_DOWN_LOADING));
            JSONArray sliceInfoPathList = jsonObject.getJSONArray(SLICE_INFO_PATH_LIST);
            for (int i = 0; i < sliceInfoPathList.length(); i++) {
                String sliceInfoPath = sliceInfoPathList.getString(i);
                String sliceInfoString = FileUtils.getStringFromFile(sliceInfoPath);
                DownloadInfo.SliceInfo sliceInfo = getSliceInfoFromJson(sliceInfoString);
                sliceInfo.setTag(downloadInfo.getTag());
                sliceInfo.setUrl(downloadInfo.getUrl());
                downloadInfo.addSliceInfo(sliceInfo);
                downloadInfo.addSliceInfoPath(sliceInfoPath);
            }
            return downloadInfo;
        } catch (JSONException e) {
            Log.e(TAG, "getFileInfoFromJson", e);
        }
        return null;
    }

    public static String getFileInfoToJson(DownloadInfo info) {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put(FILE_PATH, info.getFilePath());
            jsonObject.put(TASK_TAG, info.getTag());
            jsonObject.put(INFO_PATH, info.getInfoPath());
            jsonObject.put(URL, info.getUrl());
            jsonObject.put(LENGTH, info.getLength());
            jsonObject.put(IS_DOWN_LOADING, info.isDownLoading());
            JSONArray jsonArray = new JSONArray();
            for (String sliceInfoPath : info.getSliceInfoPathList()) {
                jsonArray.put(sliceInfoPath);
            }
            jsonObject.put(SLICE_INFO_PATH_LIST, jsonArray);
        } catch (JSONException e) {
            Log.e(TAG, "getSaveJsonString", e);
        }
        return jsonObject.toString();
    }

    public static DownloadInfo.SliceInfo getSliceInfoFromJson(String json) throws JSONException {
        DownloadInfo.SliceInfo sliceInfo = new DownloadInfo.SliceInfo();
        JSONObject jsonObject = new JSONObject(json);
        sliceInfo.setPartNumber(jsonObject.getInt(PART_NUMBER));
        sliceInfo.setInfoPath(jsonObject.getString(INFO_PATH));
        sliceInfo.setSlicePath(jsonObject.getString(SLICE_PATH));
        sliceInfo.setStartRange(jsonObject.getLong(START_RANGE));
        sliceInfo.setEndRange(jsonObject.getLong(END_RANGE));
        sliceInfo.setMaxLength(jsonObject.getLong(MAX_LENGTH));
        sliceInfo.setProportion(jsonObject.getInt(PROPORTION));
        sliceInfo.setCurrentLength(new File(sliceInfo.getSlicePath()).length());
        return sliceInfo;
    }

    public static String getSliceInfoToJson(DownloadInfo.SliceInfo info) {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put(PART_NUMBER, info.getPartNumber());
            jsonObject.put(INFO_PATH, info.getInfoPath());
            jsonObject.put(SLICE_PATH, info.getSlicePath());
            jsonObject.put(MAX_LENGTH, info.getMaxLength());
            jsonObject.put(START_RANGE, info.getStartRange());
            jsonObject.put(END_RANGE, info.getEndRange());
            jsonObject.put(MAX_LENGTH, info.getMaxLength());
            jsonObject.put(PROPORTION, info.getProportion());
        } catch (JSONException e) {
            Log.e(TAG, "getSaveJsonString", e);
        }
        return jsonObject.toString();
    }

    public void download(
            final String tag,
            final String url,
            final String savePath,
            final DownloadEngineCallback callback) {
        if (existsTask(tag)) {
            return;
        }
        // 先判断是否支持断点续传，如果支持，再判断文件是否大于需要分片的逻辑，需要则开始分片，默认分4片
        // 判断是否支持断点续传
        boolean support = checkSupportResumeTransfer(url);
        // 获取媒体文件的大小
        long mediaLength = getMediaLength(url);
        if (!support || mediaLength <= 0) {
            TotalDownloadInfo totalDownloadInfo =
                    new TotalDownloadInfo(tag, savePath, url, mediaLength);
            TotalCallback totalCallBack =
                    new TotalCallback(callback, totalDownloadInfo);
            TotalDownloadRequest request =
                    new TotalDownloadRequest(totalDownloadInfo, totalCallBack);
            taskDispatcher.enqueue(request);
            return;
        }
        // 使用分片下载支持断点续传
        // 获取分片的临界值，大于临界值时开始分4片，否则使用 1 片
        int sliceLength = getDownloadEachSliceLength();
        // 获取文件暂存地址
        final String pausePath = new File(new File(savePath).getParent(), tag + ".txt").getPath();
        // 获取文件信息
        final DownloadInfo downloadInfo =
                getFileInfo(tag, pausePath, mediaLength, savePath, url, sliceLength);

        for (DownloadInfo.SliceInfo sliceInfo : downloadInfo.getSliceInfoList()) {
            if (sliceInfo.isFinish()) {
                continue;
            }
            SliceCallback sliceCallback =
                    new SliceCallback(callback, downloadInfo, sliceInfo);
            SliceDownloadRequest request =
                    new SliceDownloadRequest(downloadInfo, sliceInfo, sliceCallback);
            taskDispatcher.enqueue(request);
        }

    }

    /**
     * 判断是否支持断点续传
     *
     * @param url 文件 fileUri
     * @return true 支持断点续传, false 不支持断点续传
     */
    public boolean checkSupportResumeTransfer(String url) {
        HttpURLConnection conn = null;
        try {
            conn = NetUtils.createURLConnection(url);
            conn.setRequestMethod(BaseRequest.METHOD_GET);
            conn.setReadTimeout(TIMEOUT);
            conn.setConnectTimeout(TIMEOUT);
            conn.setRequestProperty(BaseRequest.HEADER_RANGE, BaseRequest.RANGE_0_1);
            conn.setRequestProperty(
                    BaseRequest.HEADER_ACCEPT_ENCODING, BaseRequest.ACCEPT_ENCODING_IDENTITY);
            conn.connect();
            int responseCode = conn.getResponseCode();

            if (responseCode == HttpURLConnection.HTTP_PARTIAL) {
                String contentRange = conn.getHeaderField(BaseRequest.HEADER_CONTENT_RANGE);
                return !TextUtils.isEmpty(contentRange);
            }
        } catch (Exception e) {
            Log.e(TAG, "checkSupportResumeTransfer", e);
        } finally {

            if (conn != null) {
                conn.disconnect();
            }
        }
        return false;
    }

    private long getMediaLength(String url) {
        // 先使用 HEAD 请求，如果异常，使用 get 请求
        HttpURLConnection connection = null;
        String[] methodList = new String[]{BaseRequest.METHOD_GET};
        for (String method : methodList) {
            try {
                // 连接网络
                connection = NetUtils.createURLConnection(url);
                connection.setRequestMethod(method);
                connection.setConnectTimeout(TIMEOUT);
                connection.setReadTimeout(TIMEOUT);
                connection.setRequestProperty(
                        BaseRequest.HEADER_ACCEPT_ENCODING, BaseRequest.ACCEPT_ENCODING_IDENTITY);
                connection.setRequestProperty(
                        BaseRequest.HEADER_CONNECTION, BaseRequest.CONNECTION_CLOSE);
                connection.connect();
                if (connection.getResponseCode() >= HttpURLConnection.HTTP_OK
                        && connection.getResponseCode() < HttpURLConnection.HTTP_MULT_CHOICE) {
                    // 获得文件长度
                    return connection.getContentLength();
                }
            } catch (Exception e) {
                Log.e(TAG, "getMediaLength", e);
            } finally {
                // 释放资源
                try {
                    if (connection != null) {
                        connection.disconnect();
                    }
                } catch (Exception e) {
                    Log.e(TAG, "getMediaLength", e);
                }
            }
        }
        return 0;
    }

    /**
     * 从 rc_resume_file_transfer_size_each_slice 取值
     *
     * @return 获取分片下载临界值
     */
    public int getDownloadEachSliceLength() {
        return 1024 * 1024 * 10;
    }

    /**
     * 磁盘有，返回磁盘信息，没有则创建
     *
     * @param tag         唯一标识（messageId）
     * @param pausedPath  暂存地址
     * @param fileLength  文件大小
     * @param filePath    文件存储地址
     * @param downloadUrl 服务端下载地址
     * @param sliceLimit  临界值
     * @return 文件信息
     */
    private DownloadInfo getFileInfo(
            String tag,
            String pausedPath,
            long fileLength,
            String filePath,
            String downloadUrl,
            int sliceLimit) {
        DownloadInfo downloadInfo = null;
        try {
            String savedFileInfoString = FileUtils.getStringFromFile(pausedPath);
            if (!TextUtils.isEmpty(savedFileInfoString)) {
                downloadInfo = getFileInfoFromJson(savedFileInfoString);
            }
        } catch (Exception e) {
            Log.e(TAG, "getFileInfo", e);
        }
        if (downloadInfo == null) {
            downloadInfo =
                    createFileInfo(tag, pausedPath, fileLength, filePath, downloadUrl, sliceLimit);
            FileUtils.saveFile(getFileInfoToJson(downloadInfo), pausedPath);
        }
        downloadInfo.setDownLoading(true);
        return downloadInfo;
    }

    private DownloadInfo createFileInfo(
            String tag,
            String pausedPath,
            long fileLength,
            String filePath,
            String downloadUrl,
            int sliceLimit) {
        DownloadInfo downloadInfo = new DownloadInfo();
        downloadInfo.setUrl(downloadUrl);
        downloadInfo.setFilePath(filePath);
        downloadInfo.setInfoPath(pausedPath);
        downloadInfo.setLength(fileLength);
        downloadInfo.setTag(tag);
        if (sliceLimit >= fileLength) {
            DownloadInfo.SliceInfo sliceInfo = new DownloadInfo.SliceInfo();
            final String pausePath = new File(new File(pausedPath).getParent(), tag + "_" + 0).getPath();
            sliceInfo.setInfoPath(pausePath);
            sliceInfo.setSlicePath(filePath + "_" + 0);
            sliceInfo.setPartNumber(0);
            sliceInfo.setProportion(100);
            sliceInfo.setStartRange(0);
            sliceInfo.setEndRange(fileLength - 1);
            sliceInfo.setMaxLength(fileLength);
            sliceInfo.setUrl(downloadUrl);
            sliceInfo.setTag(tag);
            FileUtils.saveFile(getSliceInfoToJson(sliceInfo), pausePath);
            downloadInfo.addSliceInfo(sliceInfo);
            downloadInfo.addSliceInfoPath(sliceInfo.getInfoPath());
            return downloadInfo;
        }

        long currentRange = 0;
        long sliceLength = fileLength / 4;
        long remainder = fileLength % 4;
        for (int i = 0; i < SLICE_COUNT; i++) {
            DownloadInfo.SliceInfo sliceInfo = new DownloadInfo.SliceInfo();
            final String pausePath = new File(new File(pausedPath).getParent(), tag + "_" + i).getPath();
            sliceInfo.setInfoPath(pausePath);
            sliceInfo.setSlicePath(filePath + "_" + i);
            sliceInfo.setPartNumber(i);
            sliceInfo.setProportion(25);
            sliceInfo.setUrl(downloadUrl);
            sliceInfo.setTag(tag);
            sliceInfo.setStartRange(currentRange);
            if (remainder > 0) {
                sliceInfo.setEndRange(currentRange + sliceLength);
                sliceInfo.setMaxLength(sliceLength + 1);
                remainder--;
            } else {
                sliceInfo.setEndRange(currentRange + sliceLength - 1);
                sliceInfo.setMaxLength(sliceLength);
            }
            currentRange = sliceInfo.getEndRange();
            currentRange++;
            FileUtils.saveFile(getSliceInfoToJson(sliceInfo), pausePath);
            downloadInfo.addSliceInfo(sliceInfo);
            downloadInfo.addSliceInfoPath(sliceInfo.getInfoPath());
        }
        return downloadInfo;
    }
    public interface DownloadEngineCallback {
        /**
         * 下载出错的回调
         *
         * @param errorCode 返回错误码
         */
        void onError(int errorCode);

        /**
         * 下载成功的回调
         *
         * @param savePath 文件保存地址
         */
        void onComplete(String savePath);

        /**
         * 下载进度回到
         *
         * @param progress 当前进度（0-100）
         */
        void onProgress(int progress);

        /**
         * 下载被取消的回调
         *
         * @param tag 任务标识
         */
        void onCanceled(String tag);
    }

    private static class SingletonHolder {
        private static final MediaDownloadEngine INSTANCE = new MediaDownloadEngine();
    }

    private static class TotalCallback implements RequestCallback {

        private DownloadEngineCallback callback;
        private TotalDownloadInfo totalDownloadInfo;


        public TotalCallback(
                DownloadEngineCallback callback,
                TotalDownloadInfo totalDownloadInfo) {
            this.callback = callback;
            this.totalDownloadInfo = totalDownloadInfo;
        }

        @Override
        public void onSuccess(String savePath) {
            callback.onComplete(savePath);
        }

        @Override
        public void onError(BaseDownloadRequest request, Throwable e) {
            Log.e(TAG, "download", e);
            if (e instanceof HttpException) {
                callback.onError(((HttpException) e).getErrorCode());
            } else {
                callback.onError(JErrorCode.MESSAGE_DOWNLOAD_ERROR);
            }
        }

        @Override
        public void onProgress(int progress) {
            callback.onProgress(progress);
        }

        @Override
        public void onCancel(String tag) {
            callback.onCanceled(tag);
        }
    }

    private static class SliceCallback implements RequestCallback {
        private final DownloadEngineCallback callback;
        private final DownloadInfo downloadInfo;
        private final DownloadInfo.SliceInfo sliceInfo;
        int totalProgress;
        private boolean isCancel;
        private boolean isError;

        public SliceCallback(
                DownloadEngineCallback callback,
                DownloadInfo downloadInfo,
                DownloadInfo.SliceInfo sliceInfo) {
            this.callback = callback;
            this.downloadInfo = downloadInfo;
            this.sliceInfo = sliceInfo;
            totalProgress = downloadInfo.currentProgress();
        }

        @Override
        public synchronized void onSuccess(String sliceSavePath) {
            // 合流，删除缓存文件，返回完成
            if (!downloadInfo.isFinished()) {
                return;
            }
            File file = new File(downloadInfo.getFilePath());
            try (FileOutputStream fileOutputStream = new FileOutputStream(file);
                 FileChannel outChannel = fileOutputStream.getChannel()) {
                Collections.sort(
                        downloadInfo.getSliceInfoList(),
                        new Comparator<DownloadInfo.SliceInfo>() {
                            @Override
                            public int compare(
                                    DownloadInfo.SliceInfo o1, DownloadInfo.SliceInfo o2) {
                                return o1.getPartNumber() - o2.getPartNumber();
                            }
                        });
                for (DownloadInfo.SliceInfo sliceInfo : downloadInfo.getSliceInfoList()) {
                    try (FileInputStream fileInputStream =
                                 new FileInputStream(sliceInfo.getSlicePath());
                         FileChannel inChannel = fileInputStream.getChannel()) {
                        outChannel.transferFrom(inChannel, outChannel.size(), inChannel.size());
                    }
                }
                for (DownloadInfo.SliceInfo sliceInfo : downloadInfo.getSliceInfoList()) {
                    deleteFile(sliceInfo.getSlicePath());
                    deleteFile(sliceInfo.getInfoPath());
                }
                deleteFile(downloadInfo.getInfoPath());
                downloadInfo.setDownLoading(false);
                callback.onComplete(downloadInfo.getFilePath());
            } catch (Exception e) {
                Log.e(TAG, "compound error", e);
                callback.onError(JErrorCode.FILE_SAVED_FAILED);
            }
        }

        @Override
        public void onError(BaseDownloadRequest request, Throwable e) {
            if (isError) {
                return;
            }
            isError = true;
            Log.e(TAG, "download", e);
            if (e instanceof HttpException) {
                callback.onError(((HttpException) e).getErrorCode());
            } else {
                callback.onError(JErrorCode.MESSAGE_DOWNLOAD_ERROR);
            }
        }

        @Override
        public synchronized void onProgress(int progress) {
            int temp = downloadInfo.currentProgress();
            if (totalProgress != temp) {
                totalProgress = temp;
                callback.onProgress(totalProgress);
            }
        }

        @Override
        public synchronized void onCancel(String tag) {
            if (isCancel) {
                return;
            }
            isCancel = true;
            callback.onCanceled(tag);
        }

        private void deleteFile(String path) {
            try {
                if (!new File(path).delete()) {
                    Log.d(TAG, "delete fail path is " + path);
                }
            } catch (SecurityException e) {
                Log.d(TAG, "delete fail path is " + path);
            }
        }
    }

    public boolean cancel(int id) {
        return cancel(String.valueOf(id));
    }

    public boolean cancel(String tag) {
        return taskDispatcher.cancel(tag);
    }

    public boolean pause(int id) {
        return pause(String.valueOf(id));
    }

    public boolean pause(String tag) {
        return taskDispatcher.pause(tag);
    }

    public void cancelAll() {
        taskDispatcher.cancelAll();
    }

    public boolean existsTask(String tag) {
        return taskDispatcher.existsTask(tag);
    }

    public List<Task> getTask(String tag) {
        return taskDispatcher.getTask(tag);
    }

    public void execute(Runnable r) {
        taskDispatcher.execute(r);
    }

    public void addTag(String tag) {
        taskDispatcher.addTag(tag);
    }
}
