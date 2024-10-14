package com.juggle.im.internal;

import android.text.TextUtils;

import com.juggle.im.interfaces.IMessageUploadProvider;
import com.juggle.im.internal.core.JIMCore;
import com.juggle.im.internal.core.network.QryUploadFileCredCallback;
import com.juggle.im.internal.logger.IJLog;
import com.juggle.im.internal.model.upload.UploadFileType;
import com.juggle.im.internal.model.upload.UploadOssType;
import com.juggle.im.internal.model.upload.UploadPreSignCred;
import com.juggle.im.internal.model.upload.UploadQiNiuCred;
import com.juggle.im.internal.uploader.FileUtil;
import com.juggle.im.internal.uploader.IUploader;
import com.juggle.im.internal.uploader.UploaderFactory;
import com.juggle.im.internal.util.JLogger;
import com.juggle.im.model.MediaMessageContent;
import com.juggle.im.model.Message;
import com.juggle.im.model.messages.FileMessage;
import com.juggle.im.model.messages.ImageMessage;
import com.juggle.im.model.messages.SnapshotPackedVideoMessage;
import com.juggle.im.model.messages.ThumbnailPackedImageMessage;
import com.juggle.im.model.messages.VideoMessage;
import com.juggle.im.model.messages.VoiceMessage;

/**
 * @author Ye_Guli
 * @create 2024-05-28 16:39
 */
public class UploadManager implements IMessageUploadProvider {
    public UploadManager(JIMCore core) {
        this.mCore = core;
    }

    private final JIMCore mCore;

    public void uploadLog(String filePath, String messageId, IJLog.Callback callback) {
        if (mCore.getWebSocket() == null) {
            JLogger.e("J-Uploader", "upload log fail, webSocket is null");
            if (callback != null) {
                callback.onError(-1, "upload log fail, webSocket is null");
            }
            return;
        }
        if (TextUtils.isEmpty(filePath)) {
            JLogger.e("J-Uploader", "upload log fail, log zip file not exist");
            notifyLogResult(LOG_NOT_EXIST, messageId, null);
            callback.onError(-1, "upload log fail, log zip file not exist");
            return;
        }
        UploadFileType uploadFileType = UploadFileType.LOG;
        requestUploadFileCred(uploadFileType, filePath, new QryUploadFileCredCallback() {
            @Override
            public void onSuccess(UploadOssType ossType, UploadQiNiuCred qiNiuCred, UploadPreSignCred preSignCred) {
                uploadFile(filePath, ossType, qiNiuCred, preSignCred, new IUploader.UploaderCallback() {
                    @Override
                    public void onProgress(int progress) {
                    }

                    @Override
                    public void onSuccess(String url) {
                        notifyLogResult(LOG_UPLOAD_SUCCESS, messageId, url);
                        if (callback != null) {
                            callback.onSuccess();
                        }
                    }

                    @Override
                    public void onError() {
                        notifyLogResult(LOG_UPLOAD_FAIL, messageId, null);
                        if (callback != null) {
                            callback.onError(-1, "upload log fail, upload error");
                        }
                    }

                    @Override
                    public void onCancel() {
                    }
                });
            }

            @Override
            public void onError(int errorCode) {
                notifyLogResult(LOG_UPLOAD_FAIL, messageId, null);
                if (callback != null) {
                    callback.onError(errorCode, "upload log fail, request cred error");
                }
            }
        });
    }

    @Override
    public void uploadMessage(Message message, UploadCallback uploadCallback) {
        //判空WebSocket
        if (mCore.getWebSocket() == null) {
            JLogger.e("J-Uploader", "uploadMessage fail, webSocket is null, message= " + message.getClientMsgNo());
            uploadCallback.onError();
            return;
        }
        //判空content
        if (message.getContent() == null || !(message.getContent() instanceof MediaMessageContent)) {
            JLogger.e("J-Uploader", "uploadMessage fail, message content is null, message= " + message.getClientMsgNo());
            uploadCallback.onError();
            return;
        }
        //获取content
        MediaMessageContent content = (MediaMessageContent) message.getContent();
        //判空localPath
        if (TextUtils.isEmpty(content.getLocalPath())) {
            JLogger.e("J-Uploader", "uploadMessage fail, local path is null, message= " + message.getClientMsgNo());
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
            JLogger.e("J-Uploader", "uploadMessage fail, need pre upload but pre upload local path is null, message= " + message.getClientMsgNo());
            uploadCallback.onError();
            return;
        }
        //有缩略图的情况下先上传缩略图
        if (needPreUpload) {
            doUploadMessage(message, UploadFileType.IMAGE, preUploadLocalPath, true, new PreUploadCallback(uploadFileType, uploadCallback));
            return;
        }
        //没有缩略图的情况下直接上传
        doUploadMessage(message, uploadFileType, content.getLocalPath(), false, uploadCallback);
    }

    private void requestUploadFileCred(UploadFileType fileType, String filePath, QryUploadFileCredCallback callback) {
        //获取文件后缀
        String ext = FileUtil.getFileExtension(filePath);
        //判空文件后缀
        if (TextUtils.isEmpty(ext)) {
            JLogger.e("J-Uploader", "requestUploadFileCred fail, ext is null, filePath = " + filePath);
            if (callback != null) {
                callback.onError(ConstInternal.ErrorCode.INVALID_PARAM);
            }
            return;
        }
        //判空WebSocket
        if (mCore.getWebSocket() == null) {
            JLogger.e("J-Uploader", "requestUploadFileCred fail, webSocket is null, path = " + filePath);
            if (callback != null) {
                callback.onError(ConstInternal.ErrorCode.CONNECTION_UNAVAILABLE);
            }
            return;
        }
        //调用接口获取文件上传凭证
        mCore.getWebSocket().getUploadFileCred(mCore.getUserId(), fileType, ext, new QryUploadFileCredCallback() {
            @Override
            public void onSuccess(UploadOssType ossType, UploadQiNiuCred qiNiuCred, UploadPreSignCred preSignCred) {
                if (callback != null) {
                    callback.onSuccess(ossType, qiNiuCred, preSignCred);
                }
            }

            @Override
            public void onError(int errorCode) {
                JLogger.e("J-Uploader", "getUploadFileCred failed, filePath = " + filePath + ", errorCode= " + errorCode);
                if (callback != null) {
                    callback.onError(errorCode);
                }
            }
        });
    }

    private void doUploadMessage(Message message, UploadFileType fileType, String localPath, boolean isPreUpload, UploadCallback uploadCallback) {
        requestUploadFileCred(fileType, localPath, new QryUploadFileCredCallback() {
            @Override
            public void onSuccess(UploadOssType ossType, UploadQiNiuCred qiNiuCred, UploadPreSignCred preSignCred) {
                doRealUpload(message, uploadCallback, ossType, qiNiuCred, preSignCred, localPath, isPreUpload);
            }

            @Override
            public void onError(int errorCode) {
                JLogger.e("J-Uploader", "getUploadFileCred failed, localPath= " + localPath + ", errorCode= " + errorCode);
                uploadCallback.onError();
            }
        });
    }

    private void uploadFile(String localPath, UploadOssType ossType, UploadQiNiuCred qiNiuCred, UploadPreSignCred preSignCred, IUploader.UploaderCallback callback) {
        IUploader uploader = new UploaderFactory().getUploader(localPath, callback, ossType, qiNiuCred, preSignCred);
        if (uploader == null) {
            JLogger.e("J-Uploader", "upload file failed, uploader is null, localPath= " + localPath);
            if (callback != null) {
                callback.onError();
            }
            return;
        }
        uploader.start();
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
                MediaMessageContent content = (MediaMessageContent) message.getContent();
                if (!isPreUpload) {
                    content.setUrl(url);
                    uploadCallback.onSuccess(message);
                    return;
                }
                if (message.getContent() instanceof ImageMessage) {
                    ((ImageMessage) message.getContent()).setThumbnailUrl(url);
                } else if (message.getContent() instanceof VideoMessage) {
                    ((VideoMessage) message.getContent()).setSnapshotUrl(url);
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
            JLogger.e("J-Uploader", "doRealUpload failed, uploader is null, localPath= " + localPath);
            uploadCallback.onError();
            return;
        }
        //开始上传
        uploader.start();
    }

    private void notifyLogResult(int result, String messageId, String url) {
        if (mCore.getWebSocket() == null) {
            return;
        }
        mCore.getWebSocket().uploadLogStatus(result, mCore.getUserId(), messageId, url);
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
                doUploadMessage(message, mUploadFileType, content.getLocalPath(), false, this);
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

    private static final int LOG_UPLOAD_SUCCESS = 3;
    private static final int LOG_UPLOAD_FAIL = 4;
    private static final int LOG_NOT_EXIST = 5;
}