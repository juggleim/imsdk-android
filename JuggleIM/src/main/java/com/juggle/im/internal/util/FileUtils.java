package com.juggle.im.internal.util;

import android.content.Context;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;

public class FileUtils {
    private static final String TAG = "FileUtils";

    /**
     * Get file data
     *
     * @param path File path
     * @return Get file data
     */
    public static String getStringFromFile(String path) {
        if (TextUtils.isEmpty(path)) {
            Log.e(TAG, "getStringFromFile path should not be null!");
            return null;
        }
        File file = new File(path);
        if (!file.exists()) {
            Log.e(TAG, "getStringFromFile file is not exists,path:" + path);
            return "";
        }

        FileInputStream in = null;
        BufferedReader reader = null;
        StringBuilder content = new StringBuilder();
        try {
            in = new FileInputStream(path);
            reader = new BufferedReader(new InputStreamReader(in));
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line);
            }
        } catch (IOException e) {
            Log.e(TAG, "getStringFromFile IOException", e);
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    Log.e(TAG, "getStringFromFile IOException", e);
                }
            }
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    Log.e(TAG, "getStringFromFile: in close!", e);
                }
            }
        }
        return content.toString();
    }

    /**
     * Gets the file name from the file path
     *
     * @param path File path
     * @return File name
     */
    public static String getFileNameWithPath(String path) {
        if (TextUtils.isEmpty(path)) {
            Log.e(TAG, "getFileNameWithPath path should not be null!");
            return null;
        }
        int start = path.lastIndexOf("/");
        if (start != -1) {
            return path.substring(start + 1);
        } else {
            return null;
        }
    }

    /**
     * Get the media file storage path
     *
     * @param context Context
     * @param dir     Custom directory
     * @return Media file storage path
     */
    public static String getMediaDownloadDir(Context context, String dir, String name) {
        boolean sdCardExist =
                Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
        if (sdCardExist) {
            File parent = context.getExternalCacheDir();
            File dirFile = new File(parent, dir);
            if (makeDir(dirFile)) {
                return new File(dirFile, name).getPath();
            }
        }
        File parent = context.getCacheDir();
        File dirFile = new File(parent, dir);
        if (makeDir(dirFile)) {
            return new File(dirFile, name).getPath();
        }
        return "";
    }

    private static boolean makeDir(File file) {
        if (file.exists()) {
            return true;
        }
        return file.mkdirs();
    }

    /**
     * Save the string to the specified path
     *
     * @param str String to store
     * @param filePath Specified path
     */
    public static void saveFile(String str, String filePath) {
        FileOutputStream outStream = null;
        try {
            File file = new File(filePath);
            outStream = new FileOutputStream(file);
            outStream.write(str.getBytes());
        } catch (Exception e) {
            Log.e(TAG, "saveFile", e);
        } finally {
            if (outStream != null) {
                try {
                    outStream.close();
                } catch (IOException e) {
                    Log.e(TAG, "saveFile: outStream close!", e);
                }
            }
        }
    }

}
