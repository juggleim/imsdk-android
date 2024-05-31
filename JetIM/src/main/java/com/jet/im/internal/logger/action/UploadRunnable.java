package com.jet.im.internal.logger.action;

import android.text.TextUtils;

import java.io.File;

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
            finish();
            return;
        }
        if (TextUtils.isEmpty(mUploadAction.mUploadLocalPath)) {
            finish();
            return;
        }
        File file = new File(mUploadAction.mUploadLocalPath);
        doRealUpload(file);
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

    public abstract void doRealUpload(File logFile);

    interface OnUploadCallBackListener {
        void onCallBack(int statusCode);
    }
}