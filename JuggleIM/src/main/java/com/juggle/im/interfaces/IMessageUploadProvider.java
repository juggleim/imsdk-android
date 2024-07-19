package com.juggle.im.interfaces;

import com.juggle.im.model.Message;

/**
 * @author Ye_Guli
 * @create 2024-05-27 17:04
 */
public interface IMessageUploadProvider {
    void uploadMessage(Message message, UploadCallback uploadCallback);

    interface UploadCallback {
        void onProgress(int progress);

        void onSuccess(Message message);

        void onError();

        void onCancel();
    }
}