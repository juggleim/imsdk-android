package com.juggle.im.internal.uploader;

import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.juggle.im.internal.model.upload.UploadOssType;
import com.juggle.im.internal.model.upload.UploadPreSignCred;
import com.juggle.im.internal.util.JLogger;
import com.juggle.im.internal.util.JThreadPoolExecutor;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okio.BufferedSink;
import okio.Okio;
import okio.Source;

/**
 * @author Ye_Guli
 * @create 2024-05-29 9:12
 */
public class PreSignUploader extends BaseUploader {
    private static final int BUFFER_SIZE = 4096; // Buffer size
    private static final int CONNECT_TIMEOUT = 20 * 1000; // Connection timeout
    private static final int READ_TIMEOUT = 20 * 1000; // Read timeout
    private static final int WRITE_TIMEOUT = 20 * 1000; // Write timeout

    private final UploadPreSignCred mPreSignCred;
    private volatile boolean mIsCancelled = false;
    private Call currentCall;
    private UploadOssType mOssType;

    public PreSignUploader(String localPath, UploaderCallback uploaderCallback, UploadPreSignCred preSignCred, UploadOssType ossType) {
        super(localPath, uploaderCallback);
        this.mPreSignCred = preSignCred;
        this.mOssType = ossType;
    }

    @Override
    public void start() {
        //Check whether the file path is empty
        if (TextUtils.isEmpty(mLocalPath)) {
            JLogger.e("J-Uploader", "PreSignUploader error, mLocalPath is empty");
            notifyFail();
            return;
        }
        //Check whether mQiNiuCred is null
        if (mPreSignCred == null || TextUtils.isEmpty(mPreSignCred.getUrl())) {
            JLogger.e("J-Uploader", "PreSignUploader error, mPreSignCred is null or empty");

            notifyFail();
            return;
        }
        //Get the file name
        String fileName = FileUtil.getFileName(mLocalPath);
        //Check whether the file name is empty
        if (TextUtils.isEmpty(fileName)) {
            JLogger.e("J-Uploader", "PreSignUploader error, fileName is empty");
            notifyFail();
            return;
        }
        //Start upload
        JThreadPoolExecutor.runInBackground(() -> {
            //Build OkHttpClient
            OkHttpClient client = new OkHttpClient.Builder()
                    .connectTimeout(CONNECT_TIMEOUT, TimeUnit.MILLISECONDS)
                    .readTimeout(READ_TIMEOUT, TimeUnit.MILLISECONDS)
                    .writeTimeout(WRITE_TIMEOUT, TimeUnit.MILLISECONDS)
                    .build();
            //Build RequestBody
            File file = new File(mLocalPath);
            RequestBody requestBody = new RequestBody() {
                @Nullable
                @Override
                public MediaType contentType() {
                    //Do not return ContentType, otherwise Alibaba Cloud upload returns 403
                    return null;
                }

                @Override
                public long contentLength() throws IOException {
                    return file.length();
                }

                @Override
                public void writeTo(@NonNull BufferedSink sink) throws IOException {
                    long fileLength = file.length();
                    long uploaded = 0;
                    try (Source source = Okio.source(file)) {
                        long read;
                        while ((read = source.read(sink.getBuffer(), BUFFER_SIZE)) != -1) {
                            if (mIsCancelled) {
                                return;
                            }
                            uploaded += read;
                            sink.flush();
                            //Calculate upload progress
                            if (fileLength == -1) {
                                notifyProgress(0);
                            } else {
                                double progress = (double) uploaded / fileLength * 100;
                                notifyProgress((int) progress);
                            }
                        }
                    }
                }
            };
            //Build the request
            Request.Builder builder = new Request.Builder()
                    .url(mPreSignCred.getUrl())
                    .put(requestBody);
            if (mOssType == UploadOssType.S3) {
                builder.addHeader("x-amz-acl", "public-read");
            }
            Request request = builder.build();
            //Start the network request
            try {
                currentCall = client.newCall(request);
                Response response = currentCall.execute();
                if (response.isSuccessful()) {
                    String mediaUrl = mPreSignCred.getDownloadUrl();
                    if (TextUtils.isEmpty(mediaUrl)) {
                        mediaUrl = removeQueryFromUrl(mPreSignCred.getUrl());
                    }
                    //Callback upload success
                    notifySuccess(mediaUrl);
                } else {
                    JLogger.e("J-Uploader", "PreSignUploader error, responseCode is " + response.code() + ", responseMessage is " + response.message());
                    //Callback upload failure
                    notifyFail();
                }
            } catch (Exception e) {
                JLogger.e("J-Uploader", "PreSignUploader error, exception is " + e.getMessage());
                e.printStackTrace();
                notifyFail();
            }
        });
    }

    @Override
    public void cancel() {
        if (currentCall != null) {
            currentCall.cancel();
        }
        mIsCancelled = true;
        JLogger.i("J-Uploader", "PreSignUploader canceled");
        notifyCancel();
    }

    //Remove the query part of the URL
    private String removeQueryFromUrl(String url) {
        int index = url.indexOf("?");
        if (index != -1) {
            return url.substring(0, index);
        }
        return url;
    }
}