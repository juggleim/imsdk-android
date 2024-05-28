package com.jet.im.internal.core.network;

import com.jet.im.model.upload.UploadOssType;
import com.jet.im.model.upload.UploadPreSignCred;
import com.jet.im.model.upload.UploadQiNiuCred;

public abstract class QryUploadFileCredCallback implements IWebSocketCallback {
    public abstract void onSuccess(UploadOssType ossType, UploadQiNiuCred qiNiuCred, UploadPreSignCred preSignCred);

    public abstract void onError(int errorCode);
}
