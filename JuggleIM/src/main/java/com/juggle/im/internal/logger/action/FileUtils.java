package com.juggle.im.internal.logger.action;

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
    //Check whether the current log directory total size exceeds the usage limit
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

    //Delete expired logs
    static void deleteExpiredLog(String path, long deleteTime) {
        File dir = new File(path);
        if (dir.isDirectory()) {
            String[] files = dir.list();
            if (files != null) {
                for (String item : files) {
                    try {
                        if (TextUtils.isEmpty(item)) continue;
                        //Check whether it is a log file; skip it if not
                        if (!item.endsWith(Constants.LOG_FILE_SUFFIX)) continue;
                        String[] longStrArray = item.split("\\.");
                        if (longStrArray.length > 0) {//Delete if it is earlier than the time
                            long longItem = Long.parseLong(longStrArray[0]);
                            if (longItem <= deleteTime) {
                                new File(path, item).delete(); //Delete file
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    //Prepare the log file
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
            //Build the log file name
            String logFileName = currentHour + Constants.LOG_FILE_SUFFIX;
            File logFile = new File(path, logFileName);
            //Create the file if it does not exist
            if (!logFile.exists()) {
                logFile.createNewFile();
            }
            //Append content to the end of the file
            try (FileWriter fw = new FileWriter(logFile, true);
                 BufferedWriter bw = new BufferedWriter(fw)) {
                bw.write(action.toString());
                bw.newLine(); // New line
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //Package all log files within the time range into a zip
    static String zipUploadLogFiles(String path, long startTime, long endTime) {
        try {
            //Create the compressed file name
            SimpleDateFormat dateFormat = new SimpleDateFormat(Constants.LOG_TIMESTAMP_FORMAT, Locale.US);
            String startLogFileName = dateFormat.format(new Date(startTime));
            String endLogFileName = dateFormat.format(new Date(endTime));
            String zipFileName = startLogFileName + "-" + endLogFileName + Constants.ZIP_FILE_SUFFIX;
            //Create the compressed file path
            File zipFile = new File(path, zipFileName);
            //If the file already exists, delete the original file
            if (zipFile.exists()) {
                zipFile.delete();
            }
            //Create ZipOutputStream
            try (FileOutputStream fos = new FileOutputStream(zipFile);
                 BufferedOutputStream bos = new BufferedOutputStream(fos);
                 ZipOutputStream zos = new ZipOutputStream(bos)) {
                //Get the log folder path
                File logFolder = new File(path);
                if (!logFolder.exists()) {
                    return null; //Log folder does not exist; return null
                }
                //Record whether any matching logs exist
                boolean hasLog = false;
                //Iterate over all files in the log folder and add files within the time range to the zip
                File[] logFiles = logFolder.listFiles();
                if (logFiles != null) {
                    for (File logFile : logFiles) {
                        //Check whether it is a log file; skip it if not
                        if (!logFile.getName().endsWith(Constants.LOG_FILE_SUFFIX)) continue;
                        //Get the log file name and convert it to a timestamp
                        long logFileTime = getTimestampFromFileName(logFile.getName());
                        if (logFileTime > 0 && logFileTime >= startTime && logFileTime <= endTime) {
                            //Create ZipEntry
                            ZipEntry zipEntry = new ZipEntry(logFile.getName());
                            zos.putNextEntry(zipEntry);
                            //Write file contents to the zip
                            try (FileInputStream fis = new FileInputStream(logFile);
                                 BufferedInputStream bis = new BufferedInputStream(fis)) {
                                byte[] buffer = new byte[1024];
                                int bytesRead;
                                while ((bytesRead = bis.read(buffer)) != -1) {
                                    zos.write(buffer, 0, bytesRead);
                                }
                            }
                            //Close the current ZipEntry
                            zos.closeEntry();
                            //Update hasLog
                            if(!hasLog) hasLog = true;
                        }
                    }
                }
                // If no matching logs exist, delete the file and return null
                if(!hasLog){
                    if (zipFile.exists()) {
                        zipFile.delete();
                    }
                    return null;
                }
            }
            //Return the compressed file path
            return zipFile.getAbsolutePath();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    //Convert the log file name to a timestamp
    static long getTimestampFromFileName(String fileName) {
        String fileNameNoExtend = getFileNameNoExtend(fileName);
        try {
            return Long.parseLong(fileNameNoExtend);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1;
    }

    //Get the file name without extension
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