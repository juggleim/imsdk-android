package com.juggle.im.internal.uploader;

import android.text.TextUtils;

import com.juggle.im.internal.model.upload.UploadQiNiuCred;
import com.juggle.im.internal.util.JLogger;
import com.qiniu.android.storage.UpCompletionHandler;
import com.qiniu.android.storage.UploadManager;
import com.qiniu.android.storage.UploadOptions;

/**
 * @author Ye_Guli
 * @create 2024-05-29 9:12
 */
public class QiNiuUploader extends BaseUploader {
    private final UploadQiNiuCred mQiNiuCred;
    private volatile boolean mIsCancelled = false;

    public QiNiuUploader(String localPath, UploaderCallback uploaderCallback, UploadQiNiuCred qiNiuCred) {
        super(localPath, uploaderCallback);
        this.mQiNiuCred = qiNiuCred;
    }

    @Override
    public void start() {
        //Check whether the file path is empty
        if (TextUtils.isEmpty(mLocalPath)) {
            JLogger.e("J-Uploader", "QiNiuUploader error, mLocalPath is empty");
            notifyFail();
            return;
        }
        //Check whether mQiNiuCred is null
        if (mQiNiuCred == null || TextUtils.isEmpty(mQiNiuCred.getToken()) || TextUtils.isEmpty(mQiNiuCred.getDomain())) {
            JLogger.e("J-Uploader", "QiNiuUploader error, mQiNiuCred is null or empty");
            notifyFail();
            return;
        }
        //Get the file name
        String fileName = FileUtil.getFileName(mLocalPath);
        //Check whether the file name is empty
        if (TextUtils.isEmpty(fileName)) {
            JLogger.e("J-Uploader", "QiNiuUploader error, fileName is empty");
            notifyFail();
            return;
        }
        //Declare the completion callback
        UpCompletionHandler completionHandler = (key, info, response) -> {
            if (!fileName.equals(key)) return;

            if (info.isCancelled()) {
                JLogger.i("J-Uploader", "QiNiuUploader canceled");
                notifyCancel();
                return;
            }
            if (info.isOK()) {
                try {
                    String fileKey = response.getString("key");
                    String url = mQiNiuCred.getDomain() + "/" + fileKey;
                    notifySuccess(url);
                    return;
                } catch (Exception e) {
                    JLogger.e("J-Uploader", "QiNiuUploader error, exception is " + e.getMessage());
                }
            }
            notifyFail();
        };
        //Declare the progress callback
        UploadOptions options = new UploadOptions(null, null, false,
                (key, percent) -> {
                    if (!fileName.equals(key)) return;
                    notifyProgress((int) (percent * 100));
                },
                () -> mIsCancelled);
        //Start upload
        UploadManager uploadManager = new UploadManager();
        uploadManager.put(mLocalPath, fileName, mQiNiuCred.getToken(), completionHandler, options);
    }

    @Override
    public void cancel() {
        mIsCancelled = true;
    }
}