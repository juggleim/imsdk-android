package com.jet.im.log.action;

import android.os.Looper;

import com.jet.im.log.IJLog;
import com.jet.im.log.JLogConfig;
import com.jet.im.log.JLogLevel;

import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * @author Ye_Guli
 * @create 2024-05-23 10:03
 */
public class ActionManager {
    private static ActionManager mActionManager;

    public static ActionManager instance(JLogConfig config) {
        if (mActionManager == null) {
            synchronized (ActionManager.class) {
                if (mActionManager == null) {
                    mActionManager = new ActionManager(config);
                }
            }
        }
        return mActionManager;
    }

    private ActionManager(JLogConfig config) {
        if (config == null) {
            throw new NullPointerException("JLogConfig  is invalid");
        }
        this.jLogConfig = config;
        init();
    }

    private JLogConfig jLogConfig;
    private ActionThread mActionThread;
    private final ConcurrentLinkedQueue<IAction> mActionCacheQueue = new ConcurrentLinkedQueue<>();//缓存任务队列

    private void init() {
        if (mActionThread == null) {
            mActionThread = new ActionThread(jLogConfig.getLogFileDir(), jLogConfig.getExpiredTime(), mActionCacheQueue);
            mActionThread.setName("logger-thread");
            mActionThread.start();
        }
    }

    public JLogConfig getJLogConfig() {
        return jLogConfig;
    }

    public void setJLogConfig(JLogConfig jLogConfig) {
        this.jLogConfig = jLogConfig;
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

    public void addUploadAction(long startTime, long endTime, IJLog.Callback callback) {
        UploadAction action = new UploadAction.Builder()
                .setStartTime(startTime)
                .setEndTime(endTime)
                .setCallback(callback)
                .setUploadRunnable(new UploadDefaultRunnable())
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