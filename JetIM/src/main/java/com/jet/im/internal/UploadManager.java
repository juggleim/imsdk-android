package com.jet.im.internal;

import android.text.TextUtils;

import com.jet.im.interfaces.IMessageUploadProvider;
import com.jet.im.internal.core.JetIMCore;
import com.jet.im.internal.core.network.QryUploadFileCredCallback;
import com.jet.im.internal.util.JLogger;
import com.jet.im.model.MediaMessageContent;
import com.jet.im.model.Message;
import com.jet.im.model.messages.FileMessage;
import com.jet.im.model.messages.ImageMessage;
import com.jet.im.model.messages.VideoMessage;
import com.jet.im.model.messages.VoiceMessage;
import com.jet.im.model.upload.UploadOssType;
import com.jet.im.model.upload.UploadPreSignCred;
import com.jet.im.model.upload.UploadQiNiuCred;
import com.jet.im.uploader.IUploader;
import com.jet.im.uploader.UploaderFactory;
import com.jet.im.uploader.FileUtil;

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
        String ext = FileUtil.getFileExtension(content.getLocalPath());
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
                doRealUpload(message, uploadCallback, ossType, qiNiuCred, preSignCred, content.getLocalPath());
            }

            @Override
            public void onError(int errorCode) {
                JLogger.e("getUploadFileCred failed, errorCode= " + errorCode);
                uploadCallback.onError();
            }
        });
    }

    private void doRealUpload(Message message, UploadCallback uploadCallback, UploadOssType ossType, UploadQiNiuCred qiNiuCred, UploadPreSignCred preSignCred, String localPath) {
        //声明回调
        IUploader.UploaderCallback callback = new IUploader.UploaderCallback() {
            @Override
            public void onProgress(int progress) {
                uploadCallback.onProgress(progress);
            }

            @Override
            public void onSuccess(String url) {
                if (message.getContent() instanceof ImageMessage) {
                    ((ImageMessage) message.getContent()).setUrl(url);
                    ((ImageMessage) message.getContent()).setThumbnailUrl(url);
                } else if (message.getContent() instanceof VideoMessage) {
                    ((VideoMessage) message.getContent()).setUrl(url);
                    ((VideoMessage) message.getContent()).setSnapshotUrl("");
                } else if (message.getContent() instanceof VoiceMessage) {
                    ((VoiceMessage) message.getContent()).setUrl(url);
                } else if (message.getContent() instanceof FileMessage) {
                    ((FileMessage) message.getContent()).setUrl(url);
                }
                uploadCallback.onSuccess(message);
            }

            @Override
            public void onError() {
                uploadCallback.onError();
            }

            @Override
            public void onCancel() {
                uploadCallback.onCancel();
            }
        };
        //获取Uploader
        IUploader uploader = new UploaderFactory().getUploader(localPath, callback, ossType, qiNiuCred, preSignCred);
        if (uploader == null) {
            uploadCallback.onError();
            return;
        }
        //开始上传
        uploader.start();
    }
}