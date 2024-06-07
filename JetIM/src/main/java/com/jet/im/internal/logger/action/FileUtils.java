package com.jet.im.internal.logger.action;

import android.os.StatFs;
import android.text.TextUtils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * @author Ye_Guli
 * @create 2024-05-23 14:26
 */
class FileUtils {
    //检查当前日志目录总大小是否超过使用限制
    static boolean isCanWriteSDCard(String path) {
        boolean item = false;
        try {
            StatFs stat = new StatFs(path);
            long blockSize = stat.getBlockSizeLong();
            long availableBlocks = stat.getAvailableBlocksLong();
            long total = availableBlocks * blockSize;
            if (total > Constants.DEFAULT_MAX_USE_SIZE) {
                item = true;
            }
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
        return item;
    }

    //删除过期的日志
    static void deleteExpiredLog(String path, long deleteTime) {
        File dir = new File(path);
        if (dir.isDirectory()) {
            String[] files = dir.list();
            if (files != null) {
                for (String item : files) {
                    try {
                        if (TextUtils.isEmpty(item)) continue;
                        //检查是否是日志文件，如果不是则跳过
                        if (!item.endsWith(Constants.LOG_FILE_SUFFIX)) continue;
                        String[] longStrArray = item.split("\\.");
                        if (longStrArray.length > 0) {//小于时间就删除
                            long longItem = Long.parseLong(longStrArray[0]);
                            if (longItem <= deleteTime) {
                                new File(path, item).delete(); //删除文件
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    //准备日志文件
    static void prepareLogFile(String path, long currentHour) {
        try {
            File dir = new File(path);
            if (!dir.exists()) {
                dir.mkdirs();
            }
            String logFileName = currentHour + Constants.LOG_FILE_SUFFIX;
            File logFile = new File(dir, logFileName);
            if (!logFile.exists()) {
                logFile.createNewFile();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    static void writLog2File(String path, long currentHour, WriteAction action) {
        try {
            //构建日志文件名
            String logFileName = currentHour + Constants.LOG_FILE_SUFFIX;
            File logFile = new File(path, logFileName);
            //如果文件不存在，则创建文件
            if (!logFile.exists()) {
                logFile.createNewFile();
            }
            //将内容追加到文件末尾
            try (FileWriter fw = new FileWriter(logFile, true);
                 BufferedWriter bw = new BufferedWriter(fw)) {
                bw.write(action.toString());
                bw.newLine(); // 换行
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //将所有处于时间范围内的日志文件打成zip包
    static String zipUploadLogFiles(String path, long startTime, long endTime) {
        try {
            //创建压缩文件名
            SimpleDateFormat dateFormat = new SimpleDateFormat(Constants.LOG_TIMESTAMP_FORMAT, Locale.US);
            String startLogFileName = dateFormat.format(new Date(startTime));
            String endLogFileName = dateFormat.format(new Date(endTime));
            String zipFileName = startLogFileName + "-" + endLogFileName + Constants.ZIP_FILE_SUFFIX;
            //创建压缩文件路径
            File zipFile = new File(path, zipFileName);
            //如果文件已存在，则删除原文件
            if (zipFile.exists()) {
                zipFile.delete();
            }
            //创建ZipOutputStream
            try (FileOutputStream fos = new FileOutputStream(zipFile);
                 BufferedOutputStream bos = new BufferedOutputStream(fos);
                 ZipOutputStream zos = new ZipOutputStream(bos)) {
                //获取日志文件夹路径
                File logFolder = new File(path);
                if (!logFolder.exists()) {
                    return null; //日志文件夹不存在，返回空
                }
                //记录是否有符合条件的日志
                boolean hasLog = false;
                //遍历日志文件夹下的所有文件，将符合时间范围的文件添加到压缩包中
                File[] logFiles = logFolder.listFiles();
                if (logFiles != null) {
                    for (File logFile : logFiles) {
                        //检查是否是日志文件，如果不是则跳过
                        if (!logFile.getName().endsWith(Constants.LOG_FILE_SUFFIX)) continue;
                        //获取日志文件名称并转换为时间戳
                        long logFileTime = getTimestampFromFileName(logFile.getName());
                        if (logFileTime > 0 && logFileTime >= startTime && logFileTime <= endTime) {
                            //创建ZipEntry
                            ZipEntry zipEntry = new ZipEntry(logFile.getName());
                            zos.putNextEntry(zipEntry);
                            //将文件内容写入压缩包
                            try (FileInputStream fis = new FileInputStream(logFile);
                                 BufferedInputStream bis = new BufferedInputStream(fis)) {
                                byte[] buffer = new byte[1024];
                                int bytesRead;
                                while ((bytesRead = bis.read(buffer)) != -1) {
                                    zos.write(buffer, 0, bytesRead);
                                }
                            }
                            //关闭当前ZipEntry
                            zos.closeEntry();
                            //更新hasLog
                            if(!hasLog) hasLog = true;
                        }
                    }
                }
                //如果没有符合条件的日志，删除文件并返回null
                if(!hasLog){
                    if (zipFile.exists()) {
                        zipFile.delete();
                    }
                    return null;
                }
            }
            //返回压缩文件路径
            return zipFile.getAbsolutePath();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    //将日志文件名转换为时间戳
    static long getTimestampFromFileName(String fileName) {
        String fileNameNoExtend = getFileNameNoExtend(fileName);
        try {
            return Long.parseLong(fileNameNoExtend);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1;
    }

    //获取不带扩展名的文件名
    static String getFileNameNoExtend(String filename) {
        if ((filename != null) && (filename.length() > 0)) {
            int dot = filename.lastIndexOf('.');
            if (dot > -1) {
                return filename.substring(0, dot);
            }
        }
        return filename;
    }
}