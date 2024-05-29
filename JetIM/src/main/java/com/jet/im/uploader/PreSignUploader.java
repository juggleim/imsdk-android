package com.jet.im.uploader;

import android.text.TextUtils;

import com.jet.im.model.upload.UploadPreSignCred;

/**
 * @author Ye_Guli
 * @create 2024-05-29 9:12
 */
public class PreSignUploader extends BaseUploader {
    private final UploadPreSignCred mPreSignCred;

    public PreSignUploader(String localPath, UploaderCallback uploaderCallback, UploadPreSignCred preSignCred) {
        super(localPath, uploaderCallback);
        this.mPreSignCred = preSignCred;
    }

    @Override
    public void start() {
        //判空文件地址
        if (TextUtils.isEmpty(mLocalPath)) {
            notifyFail();
            return;
        }
        //判空mQiNiuCred
        if (mPreSignCred == null || TextUtils.isEmpty(mPreSignCred.getUrl())) {
            notifyFail();
            return;
        }
        //获取文件名
        String fileName = FileUtil.getFileName(mLocalPath);
        //判空文件名
        if (TextUtils.isEmpty(fileName)) {
            notifyFail();
            return;
        }
        //todo 暂未实现
        notifyFail();
    }

    @Override
    public void cancel() {
        notifyCancel();
    }
}