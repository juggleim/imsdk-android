package com.jet.im.kit.vm;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.Observer;

import com.sendbird.android.message.BaseMessage;
import com.sendbird.android.message.FileMessage;
import com.sendbird.android.message.MultipleFilesMessage;
import com.jet.im.kit.internal.extensions.MessageExtensionsKt;
import com.jet.im.kit.internal.tasks.JobTask;
import com.jet.im.kit.internal.tasks.TaskQueue;
import com.jet.im.kit.model.FileInfo;
import com.jet.im.kit.utils.FileUtils;
import com.jet.im.kit.utils.MessageUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class PendingMessageRepository {

    private PendingMessageRepository() {}

    private static class PendingMessageManagerHolder {
        static final PendingMessageRepository INSTANCE = new PendingMessageRepository();
    }

    @NonNull
    public static PendingMessageRepository getInstance() {
        return PendingMessageManagerHolder.INSTANCE;
    }

    private final Map<String, List<BaseMessage>> pendingMessageMap = new ConcurrentHashMap<>();
    private final Map<String, FileInfo> cachedFileInfos = new ConcurrentHashMap<>();
    private final List<Observer<BaseMessage>> pendingMessageStatusChanged = new ArrayList<>();

    boolean addPendingMessageStatusChanged(@NonNull Observer<BaseMessage> subscriber) {
        return pendingMessageStatusChanged.add(subscriber);
    }

    boolean removePendingMessageStatusObserver(@NonNull Observer<BaseMessage> subscriber) {
        return pendingMessageStatusChanged.remove(subscriber);
    }

    private synchronized void notifyPendingMessageStatusChanged(@NonNull BaseMessage message) {
        for (Observer<BaseMessage> subscriber : pendingMessageStatusChanged) {
            subscriber.onChanged(message);
        }
    }

    @Nullable
    public FileInfo getFileInfo(@NonNull String requestId) {
        return cachedFileInfos.get(requestId);
    }

    @Nullable
    public FileInfo getFileInfo(@NonNull BaseMessage message) {
        return getFileInfo(message.getRequestId());
    }

    public void addFileInfos(@NonNull MultipleFilesMessage message, @NonNull List<FileInfo> fileInfos) {
        int index = 0;
        for (FileInfo fileInfo : fileInfos) {
            cachedFileInfos.put(MessageExtensionsKt.getCacheKey(message, index++), fileInfo);
        }
    }

    public void addFileInfo(@NonNull FileMessage message, @NonNull FileInfo fileInfo) {
        cachedFileInfos.put(message.getRequestId(), fileInfo);
        if (MessageUtils.isVoiceMessage(message) && fileInfo.getCacheDir() != null) {
            TaskQueue.addTask(new JobTask<File>() {
                @NonNull
                @Override
                protected File call() throws Exception {
                    final String filename = MessageUtils.getVoiceFilename(message);
                    final File destFile = new File(fileInfo.getCacheDir(), filename);
                    // As cachedFileInfos is cleared after the message is sent,
                    // the voice file is copied to play it from the cache dir.
                    FileUtils.copyFile(fileInfo.getFile(), destFile);
                    return destFile;
                }
            });
        }
    }

    void clearAllFileInfo(@NonNull List<BaseMessage> messages) {
        for (BaseMessage message : messages) {
            if (message instanceof FileMessage) {
                PendingMessageRepository.getInstance().clearFileInfo((FileMessage) message);
            }
        }
    }

    boolean clearFileInfo(@NonNull FileMessage message) {
        boolean isRemoved = false;
        final FileInfo fileInfo = getFileInfo(message);

        // Do not remove fileInfo in map. If you remove it in map, image will be blinked
        if (fileInfo != null) {
            fileInfo.clear();
            isRemoved = true;
        }
        return isRemoved;
    }

    void addPendingMessage(@NonNull String channelUrl, @NonNull BaseMessage message) {
        List<BaseMessage> pendingMessages = pendingMessageMap.get(channelUrl);
        if (pendingMessages == null) {
            pendingMessages = new ArrayList<>();
        }
        pendingMessages.add(0, message);
        pendingMessageMap.put(channelUrl, pendingMessages);
        notifyPendingMessageStatusChanged(message);

    }

    void updatePendingMessage(@NonNull String channelUrl, @Nullable BaseMessage message) {
        if (message == null) return;

        final List<BaseMessage> pendingMessages = pendingMessageMap.get(channelUrl);
        if (pendingMessages != null) {
            BaseMessage msg;
            for (int i = pendingMessages.size() - 1; i >= 0; i--) {
                msg = pendingMessages.get(i);
                if (msg.getRequestId().equals(message.getRequestId())) {
                    pendingMessages.set(i, message);
                    break;
                }
            }
            pendingMessageMap.put(channelUrl, pendingMessages);
        }
        notifyPendingMessageStatusChanged(message);
    }

    boolean removePendingMessage(@NonNull String channelUrl, @NonNull BaseMessage message) {
        final List<BaseMessage> pendingMessages = pendingMessageMap.get(channelUrl);
        final String reqId = message.getRequestId();
        boolean isRemoved = false;

        if (message instanceof FileMessage) {
            clearFileInfo((FileMessage) message);
        }

        if (pendingMessages != null) {
            // because this is temp message so it must compare by request id not using equals function.
            for (BaseMessage pendingMessage : pendingMessages) {
                if (message.getClass() != pendingMessage.getClass()) {
                    continue;
                }
                String pendingMessageReqId = pendingMessage.getRequestId();
                if (reqId.equals(pendingMessageReqId)) {
                    isRemoved = pendingMessages.remove(pendingMessage);
                    break;
                }
            }
            pendingMessageMap.put(channelUrl, pendingMessages);
        }
        if (isRemoved) {
            notifyPendingMessageStatusChanged(message);
        }
        return isRemoved;
    }

    @NonNull
    List<BaseMessage> getPendingMessageList(@NonNull String channelUrl) {
        List<BaseMessage> pendingList = pendingMessageMap.get(channelUrl);
        return pendingList == null ? new ArrayList<>() : pendingList;
    }
}
