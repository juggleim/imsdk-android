package com.jet.im.interfaces;

import com.jet.im.model.Message;

/**
 * @author Ye_Guli
 * @create 2024-05-27 17:04
 */
public interface IMessageUploadProvider {
    void uploadMessage(Message message, ProgressCallback progressBlock, SuccessCallback successBlock, ErrorCallback errorBlock, CancelCallback cancelBlock);

    interface ProgressCallback {
        void onProgress(int progress);
    }

    interface SuccessCallback {
        void onSuccess(Message message);
    }

    interface ErrorCallback {
        void onError();
    }

    interface CancelCallback {
        void onCancel();
    }
}