package com.jet.im.internal;

import android.text.TextUtils;

import com.jet.im.interfaces.IMessageUploadProvider;
import com.jet.im.internal.core.JetIMCore;
import com.jet.im.internal.core.network.QryUploadFileCredCallback;
import com.jet.im.internal.util.JLogger;
import com.jet.im.model.MediaMessageContent;
import com.jet.im.model.Message;
import com.jet.im.model.upload.UploadOssType;
import com.jet.im.model.upload.UploadPreSignCred;
import com.jet.im.model.upload.UploadQiNiuCred;

import java.io.File;

/**
 * @author Ye_Guli
 * @create 2024-05-28 16:39
 */
public class UploadManager implements IMessageUploadProvider {
    public UploadManager(JetIMCore core) {
        this.mCore = core;
    }

    private final JetIMCore mCore;

    @Override
    public void uploadMessage(Message message, UploadCallback uploadCallback) {
        //判空WebSocket
        if (mCore.getWebSocket() == null) {
            uploadCallback.onError();
            return;
        }
        //判空content
        if (message.getContent() == null || !(message.getContent() instanceof MediaMessageContent)) {
            uploadCallback.onError();
            return;
        }
        //获取content
        MediaMessageContent content = (MediaMessageContent) message.getContent();
        //判空localPath
        if (TextUtils.isEmpty(content.getLocalPath())) {
            uploadCallback.onError();
            return;
        }
        //获取文件后缀
        String ext = getFileExtension(content.getLocalPath());
        //判空文件后缀
        if (TextUtils.isEmpty(ext)) {
            uploadCallback.onError();
            return;
        }
        //调用接口获取文件上传凭证
        mCore.getWebSocket().getUploadFileCred(mCore.getUserId(), content.getUploadFileType(), ext, new QryUploadFileCredCallback() {
            @Override
            public void onSuccess(UploadOssType ossType, UploadQiNiuCred qiNiuCred, UploadPreSignCred preSignCred) {
                JLogger.d("getUploadFileCred success, ossType= " + ossType + ", qiNiuCred= " + qiNiuCred.toString() + ", preSignCred= " + preSignCred.toString());
                //fixme 这里是为了测试接口调用，不管成功失败，都先按上传失败处理
                uploadCallback.onError();
            }

            @Override
            public void onError(int errorCode) {
                JLogger.e("getUploadFileCred failed, errorCode= " + errorCode);
                uploadCallback.onError();
            }
        });
    }

    private String getFileExtension(String filePath) {
        if (TextUtils.isEmpty(filePath)) return "";
        int lastPoi = filePath.lastIndexOf('.');
        int lastSep = filePath.lastIndexOf(File.separator);
        if (lastPoi == -1 || lastSep >= lastPoi) return "";
        return filePath.substring(lastPoi + 1);
    }
}