package com.juggle.im.internal.logger.action;

import android.text.TextUtils;

/**
 * @author Ye_Guli
 * @create 2024-05-23 11:05
 */
abstract class UploadRunnable implements Runnable {
    public static final int SENDING = 10001;
    public static final int FINISH = 10002;

    protected UploadAction mUploadAction;
    private OnUploadCallBackListener mCallBackListener;

    @Override
    public void run() {
        if (mUploadAction == null || !mUploadAction.isValid()) {
            notifyUploadActionCallbackFail(-1, "upload param invalid");
            finish();
            return;
        }
        doRealUpload(mUploadAction.mUploadLocalPath);
    }

    void setUploadAction(UploadAction action) {
        mUploadAction = action;
    }

    void setCallBackListener(OnUploadCallBackListener callBackListener) {
        mCallBackListener = callBackListener;
    }

    protected void finish() {
        if (mCallBackListener != null) {
            mCallBackListener.onCallBack(FINISH);
        }
    }

    protected void notifyUploadActionCallbackSuccess() {
        if (mUploadAction.mCallback != null) {
            mUploadAction.mCallback.onSuccess();
        }
    }
    protected void notifyUploadActionCallbackFail(int code, String msg) {
        if (mUploadAction.mCallback != null) {
            mUploadAction.mCallback.onError(code, msg);
        }
    }

    public abstract void doRealUpload(String filePath);

    interface OnUploadCallBackListener {
        void onCallBack(int statusCode);
    }
}