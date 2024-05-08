package com.jet.im.kit.vm;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.sendbird.android.exception.SendbirdException;
import com.sendbird.android.message.FileMessage;
import com.sendbird.android.message.Thumbnail;
import com.jet.im.kit.interfaces.OnResultHandler;
import com.jet.im.kit.internal.model.GlideCachedUrlLoader;
import com.jet.im.kit.internal.tasks.JobResultTask;
import com.jet.im.kit.internal.tasks.TaskQueue;
import com.jet.im.kit.log.Logger;
import com.jet.im.kit.utils.FileUtils;
import com.jet.im.kit.utils.MessageUtils;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;

public class FileDownloader {
    private FileDownloader() {}

    private static class FileDownloadHolder {
        static final FileDownloader INSTANCE = new FileDownloader();
    }

    public static FileDownloader getInstance() {
        return FileDownloadHolder.INSTANCE;
    }

    private final Set<String> downloadingFileSet = new HashSet<>();

    @SuppressWarnings("ResultOfMethodCallIgnored")
    @Nullable
    public File downloadVoiceFileToCache(@NonNull Context context, @NonNull FileMessage message) throws ExecutionException, InterruptedException, IOException {
        final File destFile = FileUtils.getVoiceFile(context, message);
        return downloadToCache(context, message, destFile);
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    @Nullable
    public File downloadToCache(@NonNull Context context, @NonNull FileMessage message) throws ExecutionException, InterruptedException, IOException {
        final File destFile = FileUtils.createCachedDirFile(context, System.currentTimeMillis() + "_" + message.getName());
        return downloadToCache(context, message, destFile);
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    @Nullable
    public File downloadToCache(@NonNull Context context, @NonNull FileMessage message, @NonNull final File destFile) throws ExecutionException, InterruptedException, IOException {
        final String url = message.getUrl();
        final String plainUrl = message.getPlainUrl();

        if (isFileValid(destFile, message)) {
            Logger.dev("__ return exist file");
            return destFile;
        } else {
            destFile.delete();
        }

        if (downloadingFileSet.contains(url)) {
            return null;
        }
        try {
            downloadingFileSet.add(url);

            File glideFile = GlideCachedUrlLoader.load(Glide.with(context).asFile(), url, String.valueOf(plainUrl.hashCode())).submit().get();
            Logger.dev("__ file size : " + glideFile.length());
            Logger.d("__ destFile path : " + glideFile.getAbsolutePath());
            // if glide returns cached file, it can return the failed or different file.
            if (isFileValid(glideFile, message)) {
                FileUtils.copyFile(glideFile, destFile);
                Logger.dev("__ return exist file");
                return destFile;
            } else {
                glideFile.delete();
            }

            glideFile = GlideCachedUrlLoader.load(Glide.with(context).asFile(), url, String.valueOf(plainUrl.hashCode())).submit().get();
            if (isFileValid(glideFile, message)) {
                FileUtils.copyFile(glideFile, destFile);
                return destFile;
            }
        } finally {
            downloadingFileSet.remove(url);
        }
        return null;
    }

    private boolean isFileValid(@Nullable File file, @NonNull FileMessage fileMessage) {
        return file != null && file.exists() && file.length() == fileMessage.getSize();
    }

    public boolean isDownloading(@NonNull String url) {
        return downloadingFileSet.contains(url);
    }

    public void saveFile(@NonNull Context context, @NonNull String url,
                         @NonNull String type, @NonNull String filename) throws Exception {
        if (downloadingFileSet.contains(url)) {
            return;
        }

        try {
            downloadingFileSet.add(url);
            final RequestManager glide = Glide.with(context);
            File file = glide.asFile().load(url).submit().get();
            FileUtils.saveFile(context, file, type, filename);
        } finally {
            downloadingFileSet.remove(url);
        }
    }

    @SuppressWarnings("UnusedReturnValue")
    public static boolean downloadFile(@NonNull Context context, @NonNull FileMessage message, @NonNull OnResultHandler<File> handler) {
        boolean isDownloading = FileDownloader.getInstance().isDownloading(message.getUrl());
        Logger.d("++ request download file url=%s", message.getUrl());
        Logger.d("++ isDownloading=%s", isDownloading);
        if (isDownloading) {
            Logger.d("-- [%s] already request download.", message.getUrl());
            return false;
        }
        TaskQueue.addTask(new JobResultTask<File>() {
            @Override
            public File call() throws ExecutionException, InterruptedException, IOException {
                if (MessageUtils.isVoiceMessage(message)) {
                    return FileDownloader.getInstance().downloadVoiceFileToCache(context, message);
                } else {
                    return FileDownloader.getInstance().downloadToCache(context, message);
                }
            }

            @Override
            public void onResultForUiThread(@Nullable File file, @Nullable SendbirdException e) {
                if (e != null || file == null) {
                    Logger.e(e);
                    handler.onError(e);
                    return;
                }

                Logger.d("++ file download Complete file path : " + file.getAbsolutePath());
                handler.onResult(file);
            }
        });
        return true;
    }

    public static void downloadThumbnail(@NonNull Context context, @NonNull FileMessage message) {
        List<Thumbnail> thumbnails = message.getThumbnails();
        Thumbnail thumbnail = null;
        if (thumbnails.size() > 0) {
            thumbnail = thumbnails.get(0);
        }
        String url = message.getUrl();
        if (thumbnail != null) {
            Logger.dev("++ thumbnail width : %s, thumbnail height : %s", thumbnail.getRealWidth(), thumbnail.getRealHeight());
            url = thumbnail.getUrl();
        }
        Glide.with(context).asFile().load(url).submit();
    }
}
