package com.jet.im.internal;

import android.text.TextUtils;

import com.jet.im.interfaces.IMessageUploadProvider;
import com.jet.im.internal.core.JetIMCore;
import com.jet.im.internal.core.network.QryUploadFileCredCallback;
import com.jet.im.internal.model.upload.UploadFileType;
import com.jet.im.internal.model.upload.UploadOssType;
import com.jet.im.internal.model.upload.UploadPreSignCred;
import com.jet.im.internal.model.upload.UploadQiNiuCred;
import com.jet.im.internal.uploader.FileUtil;
import com.jet.im.internal.uploader.IUploader;
import com.jet.im.internal.uploader.UploaderFactory;
import com.jet.im.internal.util.JLogger;
import com.jet.im.model.MediaMessageContent;
import com.jet.im.model.Message;
import com.jet.im.model.messages.FileMessage;
import com.jet.im.model.messages.ImageMessage;
import com.jet.im.model.messages.SnapshotPackedVideoMessage;
import com.jet.im.model.messages.ThumbnailPackedImageMessage;
import com.jet.im.model.messages.VideoMessage;
import com.jet.im.model.messages.VoiceMessage;

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
            JLogger.d("uploadMessage fail, webSocket is null, message= " + message.getClientMsgNo());
            uploadCallback.onError();
            return;
        }
        //判空content
        if (message.getContent() == null || !(message.getContent() instanceof MediaMessageContent)) {
            JLogger.d("uploadMessage fail, message content is null, message= " + message.getClientMsgNo());
            uploadCallback.onError();
            return;
        }
        //获取content
        MediaMessageContent content = (MediaMessageContent) message.getContent();
        //判空localPath
        if (TextUtils.isEmpty(content.getLocalPath())) {
            JLogger.d("uploadMessage fail, local path is null, message= " + message.getClientMsgNo());
            uploadCallback.onError();
            return;
        }
        //获取localPath上传类型
        UploadFileType uploadFileType;
        if (content instanceof ImageMessage || content instanceof ThumbnailPackedImageMessage) {
            uploadFileType = UploadFileType.IMAGE;
        } else if (content instanceof VideoMessage || content instanceof SnapshotPackedVideoMessage) {
            uploadFileType = UploadFileType.VIDEO;
        } else if (content instanceof FileMessage) {
            uploadFileType = UploadFileType.FILE;
        } else if (content instanceof VoiceMessage) {
            uploadFileType = UploadFileType.AUDIO;
        } else {
            uploadFileType = UploadFileType.DEFAULT;
        }
        //获取封面或缩略图
        boolean needPreUpload = false;
        String preUploadLocalPath = "";
        if (content instanceof ImageMessage) {
            needPreUpload = true;
            preUploadLocalPath = ((ImageMessage) content).getThumbnailLocalPath();
        } else if (content instanceof VideoMessage) {
            needPreUpload = true;
            preUploadLocalPath = ((VideoMessage) content).getSnapshotLocalPath();
        }
        //判空封面或缩略图
        if (needPreUpload && TextUtils.isEmpty(preUploadLocalPath)) {
            JLogger.d("uploadMessage fail, need pre upload but pre upload local path is null, message= " + message.getClientMsgNo());
            uploadCallback.onError();
            return;
        }
        //有缩略图的情况下先上传缩略图
        if (needPreUpload) {
            doRequestUploadFileCred(message, UploadFileType.IMAGE, preUploadLocalPath, true, new PreUploadCallback(uploadFileType, uploadCallback));
            return;
        }
        //没有缩略图的情况下直接上传
        doRequestUploadFileCred(message, uploadFileType, content.getLocalPath(), false, uploadCallback);
    }

    private void doRequestUploadFileCred(Message message, UploadFileType fileType, String localPath, boolean isPreUpload, UploadCallback uploadCallback) {
        //获取文件后缀
        String ext = FileUtil.getFileExtension(localPath);
        //判空文件后缀
        if (TextUtils.isEmpty(ext)) {
            JLogger.d("doRequestUploadFileCred fail, ext is null, localPath= " + localPath);
            uploadCallback.onError();
            return;
        }
        //调用接口获取文件上传凭证
        mCore.getWebSocket().getUploadFileCred(mCore.getUserId(), fileType, ext, new QryUploadFileCredCallback() {
            @Override
            public void onSuccess(UploadOssType ossType, UploadQiNiuCred qiNiuCred, UploadPreSignCred preSignCred) {
                JLogger.d("getUploadFileCred success, localPath= " + localPath + ", ossType= " + ossType + ", qiNiuCred= " + qiNiuCred.toString() + ", preSignCred= " + preSignCred.toString());
                doRealUpload(message, uploadCallback, ossType, qiNiuCred, preSignCred, localPath, isPreUpload);
            }

            @Override
            public void onError(int errorCode) {
                JLogger.d("getUploadFileCred failed, localPath= " + localPath + ", errorCode= " + errorCode);
                uploadCallback.onError();
            }
        });
    }

    private void doRealUpload(Message message, UploadCallback uploadCallback, UploadOssType ossType, UploadQiNiuCred qiNiuCred, UploadPreSignCred preSignCred, String localPath, boolean isPreUpload) {
        //声明回调
        IUploader.UploaderCallback callback = new IUploader.UploaderCallback() {
            @Override
            public void onProgress(int progress) {
                uploadCallback.onProgress(progress);
            }

            @Override
            public void onSuccess(String url) {
                if (message.getContent() instanceof ImageMessage) {
                    if (isPreUpload) {
                        ((ImageMessage) message.getContent()).setThumbnailUrl(url);
                    } else {
                        ((ImageMessage) message.getContent()).setUrl(url);
                    }
                } else if (message.getContent() instanceof VideoMessage) {
                    if (isPreUpload) {
                        ((VideoMessage) message.getContent()).setSnapshotUrl(url);
                    } else {
                        ((VideoMessage) message.getContent()).setUrl(url);
                    }
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
            JLogger.d("doRealUpload failed, uploader is null, localPath= " + localPath);
            uploadCallback.onError();
            return;
        }
        //开始上传
        uploader.start();
    }

    class PreUploadCallback implements UploadCallback {
        private volatile boolean mIsPreUpload;
        private final UploadFileType mUploadFileType;
        private final UploadCallback mInternalUploadCallback;

        public PreUploadCallback(UploadFileType uploadFileType, UploadCallback uploadCallback) {
            this.mIsPreUpload = true;
            this.mUploadFileType = uploadFileType;
            this.mInternalUploadCallback = uploadCallback;
        }

        @Override
        public void onProgress(int progress) {
            float preProgressPercent = 0.2f;
            int realProgress;
            if (mIsPreUpload) {
                realProgress = (int) (progress * preProgressPercent);
            } else {
                realProgress = (int) (100 * preProgressPercent + progress * (1 - preProgressPercent));
            }
            mInternalUploadCallback.onProgress(realProgress);
        }

        @Override
        public void onSuccess(Message message) {
            if (mIsPreUpload) {
                //缩略图上传成功，继续上传localPath
                mIsPreUpload = false;
                MediaMessageContent content = (MediaMessageContent) message.getContent();
                doRequestUploadFileCred(message, mUploadFileType, content.getLocalPath(), false, this);
            } else {
                mInternalUploadCallback.onSuccess(message);
            }
        }

        @Override
        public void onError() {
            mInternalUploadCallback.onError();
        }

        @Override
        public void onCancel() {
            mInternalUploadCallback.onCancel();
        }
    }
}