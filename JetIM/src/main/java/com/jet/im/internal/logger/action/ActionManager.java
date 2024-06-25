package com.jet.im.internal.logger.action;

import android.os.Looper;

import com.jet.im.internal.logger.IJLog;
import com.jet.im.internal.logger.JLogConfig;
import com.jet.im.internal.logger.JLogLevel;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * @author Ye_Guli
 * @create 2024-05-23 10:03
 */
public class ActionManager {
    public static ActionManager getInstance() {
        return ActionManager.SingletonHolder.sInstance;
    }

    private static class SingletonHolder {
        static final ActionManager sInstance = new ActionManager();
    }

    private ActionManager() {
    }

    private JLogConfig jLogConfig;
    private ActionThread mActionThread;
    private final ConcurrentLinkedQueue<IAction> mActionCacheQueue = new ConcurrentLinkedQueue<>();//缓存任务队列

    public JLogConfig getJLogConfig() {
        return jLogConfig;
    }

    public void setJLogConfig(JLogConfig config) {
        this.jLogConfig = config;
        init();
    }

    private void init() {
        if (mActionThread == null) {
            mActionThread = new ActionThread(jLogConfig.getLogFileDir(), jLogConfig.getExpiredTime(), jLogConfig.getLogFileCreateInterval(), mActionCacheQueue);
            mActionThread.setName("logger-thread");
            mActionThread.start();
        } else {
            mActionThread.updateConfig(jLogConfig.getLogFileDir(), jLogConfig.getExpiredTime(), jLogConfig.getLogFileCreateInterval());
        }
    }

    public void addWriteAction(JLogLevel level, String tag, List<String> logs) {
        WriteAction action = new WriteAction.Builder()
                .setLevel(level)
                .setTag(tag)
                .setLogs(logs)
                .setLogTime(System.currentTimeMillis())
                .setThreadInfo(Thread.currentThread().toString())
                .setIsMainThread(Looper.getMainLooper() == Looper.myLooper())
                .build();
        if (mActionCacheQueue.size() < Constants.DEFAULT_QUEUE) {
            mActionCacheQueue.add(action);
            if (mActionThread != null) {
                mActionThread.notifyRun();
            }
        }
    }

    public void addUploadAction(long startTime, long endTime, String url, Map<String, String> headers, IJLog.Callback callback) {
        UploadDefaultRunnable runnable = new UploadDefaultRunnable();
        runnable.setUploadUrl(url);
        runnable.setRequestHeader(headers);
        UploadAction action = new UploadAction.Builder()
                .setStartTime(startTime)
                .setEndTime(endTime)
                .setCallback(callback)
                .setUploadRunnable(runnable)
                .build();
        mActionCacheQueue.add(action);
        if (mActionThread != null) {
            mActionThread.notifyRun();
        }
    }

    public void addRemoveAction() {
        RemoveExpiredAction action = new RemoveExpiredAction.Builder()
                .setDeleteTime(System.currentTimeMillis() - jLogConfig.getExpiredTime())
                .build();
        mActionCacheQueue.add(action);
        if (mActionThread != null) {
            mActionThread.notifyRun();
        }
    }
}