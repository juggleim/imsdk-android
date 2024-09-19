package com.jet.im.kit.utils;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.webkit.MimeTypeMap;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.content.FileProvider;

import com.sendbird.android.message.FileMessage;
import com.jet.im.kit.consts.StringSet;
import com.jet.im.kit.log.Logger;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.FileChannel;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

@SuppressWarnings({"unused", "UnusedReturnValue"})
public class FileUtils {
    @SuppressWarnings("ResultOfMethodCallIgnored")
    @NonNull
    public static File getDocumentCacheDir(@NonNull Context context) {
        File dir = new File(context.getCacheDir(), "documents");
        if (!dir.exists()) {
            dir.mkdirs();
        }

        return dir;
    }

    @NonNull
    public static File createDownloadFile(@NonNull String fileName) {
        String imageFileName = "Downloaded_file_" + System.currentTimeMillis() + "_" + fileName;
        File dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        return new File(dir, imageFileName);
    }

    @NonNull
    public static File getDownloadFileFromMessage(@NonNull FileMessage message) {
        String newFileName = "Downloaded_file_" + message.getMessageId() + "_" + message.getName();
        File dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        return new File(dir, newFileName);
    }

    public static boolean hasDownloadFileFromMessage(@NonNull FileMessage message) {
        File file = getDownloadFileFromMessage(message);
        return file.exists() && file.length() > 0;
    }

    @NonNull
    public static Uri fileToUri(@NonNull Context context, @NonNull File file) {
        return FileProvider.getUriForFile(context, context.getPackageName() + ".sendbird.uikit.provider", file);
    }

    @NonNull
    public static String extractExtension(@NonNull Context context, @NonNull Uri uri) {
        String extension = "temp";

        final String scheme = uri.getScheme();
        if (scheme != null && scheme.equals(ContentResolver.SCHEME_CONTENT)) {
            final String type = context.getContentResolver().getType(uri);
            if (type != null) {
                extension = extractExtension(type);
            }
        } else {
            extension = MimeTypeMap.getFileExtensionFromUrl(uri.toString());
        }

        if (TextUtils.isEmpty(extension)) extension = "temp";
        return extension;
    }

    @Nullable
    public static String extractExtension(@NonNull String mimeType) {
        final MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(mimeType);
    }

    @NonNull
    public static Bitmap.CompressFormat extractBitmapFormat(@NonNull String mimeType) {
        Bitmap.CompressFormat format;
        if (mimeType.endsWith(StringSet.png)) {
            format = Bitmap.CompressFormat.PNG;
        } else if (mimeType.endsWith(StringSet.webp)) {
            format = Bitmap.CompressFormat.WEBP;
        } else {
            format = Bitmap.CompressFormat.JPEG;
        }
        return format;
    }

    @NonNull
    private static String copyFromUri(@NonNull Context context, @NonNull Uri uri, @NonNull File dstFile) {
        if (!dstFile.exists() || dstFile.length() <= 0) {
            InputStream inputStream;
            try {
                inputStream = context.getContentResolver().openInputStream(uri);
                OutputStream outputStream = new FileOutputStream(dstFile);
                copy(inputStream, outputStream);
            } catch (Exception e) {
                Logger.e(e);
            }
        }
        return dstFile.getAbsolutePath();
    }

    @NonNull
    public static String uriToPath(@NonNull Context context, @NonNull Uri uri) {
        String tempFileName = "Temp_" + System.currentTimeMillis() + "." + extractExtension(context, uri);
        File dstFile = createCachedDirFile(context, tempFileName);
        return copyFromUri(context, uri, dstFile);
    }

    @NonNull
    public static File uriToFile(@NonNull Context context, @NonNull Uri uri) {
        return new File(uriToPath(context, uri));
    }

    public static void saveFile(@NonNull Context context, @NonNull File src,
                                @NonNull String type, @NonNull String fileName) throws Exception {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            Uri newFileUri = createDownloadFileUri(context, type, fileName);
            assert newFileUri != null;
            saveFile(context, src, newFileUri);
        } else {
            File newFile = createDownloadFile(fileName);
            saveFile(context, src, newFile);
        }
    }

    @NonNull
    public static File saveFile(@NonNull Context context, @NonNull File src, @NonNull File dest) throws Exception {
        FileInputStream input = new FileInputStream(src);
        FileOutputStream output = new FileOutputStream(dest);
        copy(input, output);
        galleryAddPic(context, Uri.fromFile(dest));
        return dest;
    }

    private static void saveFile(@NonNull Context context, @NonNull File src, @NonNull Uri dest) throws Exception {
        FileInputStream input = new FileInputStream(src);
        ParcelFileDescriptor pfd = context.getContentResolver().openFileDescriptor(dest, "w");
        assert pfd != null;
        FileOutputStream output = new FileOutputStream(pfd.getFileDescriptor());
        copy(input, output);
        galleryAddPic(context, dest);
    }

    @NonNull
    public static File bitmapToFile(@NonNull Bitmap image, @NonNull File dest, @NonNull Bitmap.CompressFormat format) throws IOException {
        return bitmapToFile(image, dest, 100, format);
    }

    @NonNull
    public static File bitmapToFile(@NonNull Bitmap image, @NonNull File dest, int quality, @NonNull Bitmap.CompressFormat format) throws IOException {
        try (OutputStream outputStream = new FileOutputStream(dest, false)) {
            Logger.d("++ Create bitmap to file, quality=%s, format=%s", quality, format);
            image.compress(format, quality, outputStream);
        }
        return dest;
    }

    private static void galleryAddPic(@NonNull Context context, @NonNull Uri uri) {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        mediaScanIntent.setData(uri);
        context.sendBroadcast(mediaScanIntent);
    }

    private static int copy(@Nullable InputStream input, @NonNull OutputStream output) throws Exception {
        int BUFFER_SIZE = 1024 * 2;
        byte[] buffer = new byte[BUFFER_SIZE];

        BufferedInputStream in = new BufferedInputStream(input, BUFFER_SIZE);
        BufferedOutputStream out = new BufferedOutputStream(output, BUFFER_SIZE);
        int count = 0, n;
        try {
            while ((n = in.read(buffer, 0, BUFFER_SIZE)) != -1) {
                out.write(buffer, 0, n);
                count += n;
            }
            out.flush();
        } finally {
            out.close();
            in.close();
        }
        return count;
    }

    @Nullable
    public static Uri createImageFileUri(@NonNull Context context) {
        ContentResolver contentResolver = context.getContentResolver();
        ContentValues cv = new ContentValues();
        String fileName = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        cv.put(MediaStore.Images.Media.TITLE, fileName);
        return contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, cv);
    }

    @Nullable
    public static Uri createVideoFileUri(@NonNull Context context) {
        ContentResolver contentResolver = context.getContentResolver();
        ContentValues cv = new ContentValues();
        String fileName = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        cv.put(MediaStore.Video.Media.TITLE, fileName);
        return contentResolver.insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, cv);
    }

    @Nullable
    @RequiresApi(api = Build.VERSION_CODES.Q)
    public static Uri createDownloadFileUri(@NonNull Context context,
                                            @NonNull String mimeType,
                                            @NonNull String fileName) {
        ContentResolver contentResolver = context.getContentResolver();
        ContentValues cv = new ContentValues();
        String downloadedFileName = "Downloaded_file_" + System.currentTimeMillis() + "_" + fileName;
        cv.put(MediaStore.Downloads.TITLE, downloadedFileName);
        cv.put(MediaStore.Downloads.DISPLAY_NAME, downloadedFileName);
        cv.put(MediaStore.Downloads.MIME_TYPE, mimeType);
        return contentResolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, cv);
    }

    private static boolean deleteRecursive(@NonNull File fileOrDirectory) {
        if (fileOrDirectory.isDirectory()) {
            File[] list = fileOrDirectory.listFiles();
            if (list != null) {
                for (File child : list) {
                    deleteRecursive(child);
                }
            }
        }
        return fileOrDirectory.delete();
    }

    public static void removeDeletableDir(@NonNull Context context) {
        File dir = getDeletableDir(context);
        deleteRecursive(dir);
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    @NonNull
    public static File getDeletableDir(@NonNull Context context) {
        File dir = context.getCacheDir();
        File file = new File(dir, "deletable");
        if (!file.exists()) {
            file.mkdir();
        }
        return file;
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    @NonNull
    public static File getChannelFileCacheDir(@NonNull Context context, @NonNull String channelUrl) {
        File dir = context.getCacheDir();
        File file = new File(dir, channelUrl);
        if (!file.exists()) {
            file.mkdir();
        }
        return file;
    }

    @NonNull
    public static File getVoiceFile(@NonNull Context context, @NonNull FileMessage message) {
        return FileUtils.createChannelCacheFile(
            context.getApplicationContext(),
            message.getChannelUrl(),
            MessageUtils.getVoiceFilename(message)
        );
    }

    @NonNull
    public static File createDeletableFile(@NonNull Context context, @NonNull String fileName) {
        return new File(getDeletableDir(context), fileName);
    }

    @NonNull
    public static File createCachedDirFile(@NonNull Context context, @NonNull String fileName) {
        File dir = context.getCacheDir();
        return new File(dir, fileName);
    }

    @NonNull
    public static File createChannelCacheFile(@NonNull Context context, @NonNull String channelUrl, @NonNull String fileName) {
        return new File(getChannelFileCacheDir(context, channelUrl), fileName);
    }

    public static void copyFile(@NonNull File src, @NonNull File dst) throws IOException {
        try (FileChannel inChannel = new FileInputStream(src).getChannel(); FileChannel outChannel = new FileOutputStream(dst).getChannel()) {
            inChannel.transferTo(0, inChannel.size(), outChannel);
        }
    }

    @NonNull
    public static String getReadableFileSize(long length) {
        if (length <= 0) {
            return "0 B";
        }
        final String[] units = new String[]{"B", "KB", "MB", "GB", "TB"};
        int digitGroups = (int) (Math.log10(length) / Math.log10(1024));
        return new DecimalFormat("#,##0.#").format(length / Math.pow(1024, digitGroups)) + " " + units[digitGroups];
    }
}
