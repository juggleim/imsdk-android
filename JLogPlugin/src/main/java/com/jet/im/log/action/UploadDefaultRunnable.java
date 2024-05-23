package com.jet.im.log.action;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Ye_Guli
 * @create 2024-05-23 11:23
 */
class UploadDefaultRunnable extends UploadRunnable {
    private static final String TAG = "UploadDefaultRunnable";
    private String mUploadUrl;
    private final Map<String, String> mRequestHeaders = new HashMap<>();

    @Override
    public void doRealUpload(File logFile) {
        doRealUploadByAction(logFile, mRequestHeaders, mUploadUrl);
        finish();

        //todo 测试中，上传完成后暂时不删除文件
//        if (logFile != null) {
//            logFile.delete();
//        }
    }

    public void setUploadUrl(String uploadUrl) {
        mUploadUrl = uploadUrl;
    }

    public void setRequestHeader(Map<String, String> headers) {
        mRequestHeaders.clear();
        if (headers != null) {
            mRequestHeaders.putAll(headers);
        }
    }

    private void doRealUploadByAction(File logFile, Map<String, String> mRequestHeaders, String mUploadUrl) {
        try {
            FileInputStream fileStream = new FileInputStream(logFile);
            doPostRequest(mUploadUrl, fileStream, mRequestHeaders);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void doPostRequest(String mUploadUrl, FileInputStream fileStream, Map<String, String> mRequestHeaders) {
        try {
            //todo 上传日志接口未对接，使用sleep模拟网络请求
            Thread.sleep(1000);
            if (mUploadAction.mCallback != null) {
                mUploadAction.mCallback.onSuccess();
            }
            return;
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (mUploadAction.mCallback != null) {
            mUploadAction.mCallback.onError(-1, "doPostRequest failed");
        }
    }
}