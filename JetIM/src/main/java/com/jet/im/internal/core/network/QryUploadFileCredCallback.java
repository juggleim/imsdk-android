package com.jet.im.internal.core.network;

import com.jet.im.internal.model.upload.UploadOssType;
import com.jet.im.internal.model.upload.UploadPreSignCred;
import com.jet.im.internal.model.upload.UploadQiNiuCred;

public abstract class QryUploadFileCredCallback implements IWebSocketCallback {
    public abstract void onSuccess(UploadOssType ossType, UploadQiNiuCred qiNiuCred, UploadPreSignCred preSignCred);

    public abstract void onError(int errorCode);
}
