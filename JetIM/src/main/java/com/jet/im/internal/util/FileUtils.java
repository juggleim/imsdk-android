package com.jet.im.internal.util;

import android.content.Context;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;

public class FileUtils {
    private static final String TAG = "FileUtils";

    /**
     * 获取文件数据
     *
     * @param path 文件路径
     * @return 获取文件数据
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
     * 根据文件路径获取文件名
     *
     * @param path 文件路径
     * @return 文件名
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
     * 获取媒体文件存储路径
     *
     * @param context 上下文
     * @param dir     自定义目录
     * @return 媒体文件存储路径
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
     * 把字符串存到指定路径下
     *
     * @param str 要存储的字符串
     * @param filePath 指定路径
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
