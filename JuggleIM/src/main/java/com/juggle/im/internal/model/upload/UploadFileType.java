package com.juggle.im.internal.model.upload;

/**
 * @author Ye_Guli
 * @create 2024-05-28 16:00
 */
public enum UploadFileType {
    DEFAULT(0),
    IMAGE(1),
    AUDIO(2),
    VIDEO(3),
    FILE(4);

    UploadFileType(int value) {
        this.mValue = value;
    }

    public int getValue() {
        return mValue;
    }

    private final int mValue;
}
