package com.jet.im.uploader;

import com.jet.im.model.upload.UploadOssType;
import com.jet.im.model.upload.UploadPreSignCred;
import com.jet.im.model.upload.UploadQiNiuCred;

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