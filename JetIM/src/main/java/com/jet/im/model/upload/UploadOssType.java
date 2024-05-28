package com.jet.im.model.upload;

/**
 * @author Ye_Guli
 * @create 2024-05-28 16:08
 */
public enum UploadOssType {
    DEFAULT(0),
    QINIU(1),
    S3(2),
    MINIO(3),
    OSS(4);

    public static UploadOssType setValue(int value) {
        for (UploadOssType d : UploadOssType.values()) {
            if (value == d.mValue) {
                return d;
            }
        }
        return DEFAULT;
    }

    UploadOssType(int value) {
        this.mValue = value;
    }

    public int getValue() {
        return mValue;
    }

    private final int mValue;
}