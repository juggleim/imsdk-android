package com.juggle.im.internal.uploader;

import com.juggle.im.internal.model.upload.UploadOssType;
import com.juggle.im.internal.model.upload.UploadPreSignCred;
import com.juggle.im.internal.model.upload.UploadQiNiuCred;

/**
 * @author Ye_Guli
 * @create 2024-05-29 9:07
 */
public class UploaderFactory {
    public BaseUploader getUploader(String localPath, IUploader.UploaderCallback uploaderCallback, UploadOssType ossType, UploadQiNiuCred qiNiuCred, UploadPreSignCred preSignCred) {
        BaseUploader uploader = null;
        switch (ossType) {
            case QINIU:
                uploader = new QiNiuUploader(localPath, uploaderCallback, qiNiuCred);
                break;
            case S3:
            case MINIO:
            case OSS:
                uploader = new PreSignUploader(localPath, uploaderCallback, preSignCred);
                break;
            default:
                break;
        }
        return uploader;
    }
}