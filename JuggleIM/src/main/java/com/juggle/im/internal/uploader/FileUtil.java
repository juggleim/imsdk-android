package com.juggle.im.internal.uploader;

import android.text.TextUtils;

import java.io.File;

/**
 * @author Ye_Guli
 * @create 2024-05-29 10:13
 */
public class FileUtil {
   public static String getFileExtension(String filePath) {
      if (TextUtils.isEmpty(filePath)) return "";
      int lastPoi = filePath.lastIndexOf('.');
      int lastSep = filePath.lastIndexOf(File.separator);
      if (lastPoi == -1 || lastSep >= lastPoi) return "";
      return filePath.substring(lastPoi + 1);
   }

   public static String getFileName(final String filePath) {
      if (TextUtils.isEmpty(filePath)) return "";
      int lastSep = filePath.lastIndexOf(File.separator);
      return lastSep == -1 ? filePath : filePath.substring(lastSep + 1);
   }
}