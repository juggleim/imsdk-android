package com.jet.im.internal.logger.action;

import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

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
        //上传完成后删除文件
        if (logFile != null) {
            logFile.delete();
        }
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
            doPostRequest(mUploadUrl, logFile, mRequestHeaders);
        } catch (Exception e) {
            e.printStackTrace();
            notifyUploadActionCallbackFail(-1, "doRealUploadByAction failed, e= " + e.getMessage());
        }
    }

    private void doPostRequest(String mUploadUrl, File file, Map<String, String> mRequestHeaders) {
        //构造OkHttpClient
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(Constants.LOG_UPLOAD_TIME_OUT, TimeUnit.MILLISECONDS)
                .readTimeout(Constants.LOG_UPLOAD_TIME_OUT, TimeUnit.MILLISECONDS)
                .writeTimeout(Constants.LOG_UPLOAD_TIME_OUT, TimeUnit.MILLISECONDS)
                .build();
        //创建文件请求体
        RequestBody fileBody = RequestBody.create(file, null);
        //创建 multipart 请求体
        MultipartBody.Builder requestBodyBuilder = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("log", file.getName(), fileBody);
        //构建请求体
        RequestBody requestBody = requestBodyBuilder.build();
        //构建请求
        Request.Builder requestBuilder = new Request.Builder()
                .url(mUploadUrl)
                .post(requestBody);
        //添加请求头
        Set<Map.Entry<String, String>> entrySet = mRequestHeaders.entrySet();
        for (Map.Entry<String, String> tempEntry : entrySet) {
            requestBuilder.addHeader(tempEntry.getKey(), tempEntry.getValue());
        }
        Request request = requestBuilder.build();
        Response response = null;
        try {
            //同步请求
            response = client.newCall(request).execute();
            if (!response.isSuccessful()) {
                notifyUploadActionCallbackFail(-1, "doPostRequest failed, statusCode= " + response.code());
                return;
            }
            try {
                //解析JSON数据
                String resultData = response.body().string();
                JSONObject jsonResponse = new JSONObject(resultData);
                int code = jsonResponse.getInt("code");
                if (code == 0) {
                    notifyUploadActionCallbackSuccess();
                } else {
                    notifyUploadActionCallbackFail(-1, "doPostRequest failed, resultData= " + resultData);
                }
            } catch (Exception e) {
                e.printStackTrace();
                notifyUploadActionCallbackFail(-1, "doPostRequest failed, e= " + e.getMessage());
            }
        } catch (IOException e) {
            e.printStackTrace();
            notifyUploadActionCallbackFail(-1, "doPostRequest failed, e= " + e.getMessage());
        } finally {
            if (response != null) {
                response.close();
            }
        }
    }
}