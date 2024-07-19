package com.juggle.im.internal.core.network;

import com.juggle.im.internal.model.upload.UploadOssType;
import com.juggle.im.internal.model.upload.UploadPreSignCred;
import com.juggle.im.internal.model.upload.UploadQiNiuCred;

public abstract class QryUploadFileCredCallback implements IWebSocketCallback {
    public abstract void onSuccess(UploadOssType ossType, UploadQiNiuCred qiNiuCred, UploadPreSignCred preSignCred);

    public abstract void onError(int errorCode);
}
