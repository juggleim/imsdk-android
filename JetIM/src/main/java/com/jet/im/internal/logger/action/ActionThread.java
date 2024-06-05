package com.jet.im.internal.logger.action;

import android.text.TextUtils;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

/**
 * @author Ye_Guli
 * @create 2024-05-23 11:05
 */
class ActionThread extends Thread {
    private final Object sync = new Object();
    private final Object uploadSync = new Object();
    private volatile boolean mIsRun = true;
    private volatile boolean mIsWorking;

    //当前时间的整小时时间戳
    private long mCurrentHour;
    //上次检查sdcard的时间
    private long mLastCheckSDCardTime;
    //日志保存目录
    private String mPath;
    //日志过期时间
    private long mExpiredTime;
    //新日志文件创建间隔
    private long mLogFileCreateInterval;
    //上传结果StatusCode
    private int mUploadStatusCode;
    //action缓存队列
    private final ConcurrentLinkedQueue<IAction> mActionCacheQueue;
    //上传缓存队列
    private final ConcurrentLinkedQueue<IAction> mUploadActionCacheQueue = new ConcurrentLinkedQueue<>();
    //上传线程池
    private ExecutorService mSingleThreadExecutor;

    ActionThread(String path, long expiredTime, long logFileCreateInterval, ConcurrentLinkedQueue<IAction> cacheLogQueue) {
        this.mActionCacheQueue = cacheLogQueue;
        this.mPath = path;
        this.mExpiredTime = expiredTime;
        this.mLogFileCreateInterval = logFileCreateInterval;
    }

    void updateConfig(String path, long expiredTime, long logFileCreateInterval) {
        this.mPath = path;
        this.mExpiredTime = expiredTime;
        this.mLogFileCreateInterval = logFileCreateInterval;
    }

    void notifyRun() {
        if (!mIsWorking) {
            synchronized (sync) {
                sync.notify();
            }
        }
    }

    void quit() {
        mIsRun = false;
        if (!mIsWorking) {
            synchronized (sync) {
                sync.notify();
            }
        }
    }

    @Override
    public void run() {
        super.run();
        while (mIsRun) {
            synchronized (sync) {
                mIsWorking = true;
                try {
                    IAction action = mActionCacheQueue.poll();
                    if (action == null) {
                        mIsWorking = false;
                        sync.wait();
                        mIsWorking = true;
                    } else {
                        doAction(action);
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    mIsWorking = false;
                }
            }
        }
    }

    private void doAction(IAction action) {
        if (action == null || !action.isValid()) {
            return;
        }
        switch (action.getType()) {
            case ActionTypeEnum.TYPE_REMOVE_EXPIRED:
                doRemoveExpiredLog((RemoveExpiredAction) action);
                break;
            case ActionTypeEnum.TYPE_WRITE:
                doWriteLog2File((WriteAction) action);
                break;
            case ActionTypeEnum.TYPE_UPLOAD:
                UploadAction uploadAction = (UploadAction) action;
                if (uploadAction.mUploadRunnable != null) {
                    synchronized (uploadSync) {
                        if (mUploadStatusCode == UploadRunnable.SENDING) {
                            mUploadActionCacheQueue.add(uploadAction);
                        } else {
                            doUploadLog2Net(uploadAction);
                        }
                    }
                }
                break;
        }
    }

    //清除过期的日志文件
    private void doRemoveExpiredLog(RemoveExpiredAction action) {
        FileUtils.deleteExpiredLog(mPath, action.mDeleteTime);
    }

    //写日志
    private void doWriteLog2File(WriteAction action) {
        //判断是否需要创建新的日志文件
        if (!TimeUtils.needCreateLogFile(mCurrentHour, mLogFileCreateInterval)) {
            //清除过期的日志文件
            long tempCurrentHour = TimeUtils.getCurrentHour();
            long deleteTime = tempCurrentHour - mExpiredTime;
            FileUtils.deleteExpiredLog(mPath, deleteTime);
            //更新当前整小时时间戳
            mCurrentHour = TimeUtils.getCurrentHour();
            FileUtils.prepareLogFile(mPath, mCurrentHour);
        }
        //每隔1分钟检查一次当前日志目录总大小是否超过使用限制，如果大于限制，则不允许再次写入
        long currentTime = System.currentTimeMillis();
        boolean isCanWriteSDCard = true;
        if (currentTime - mLastCheckSDCardTime > Constants.MINUTE) {
            isCanWriteSDCard = FileUtils.isCanWriteSDCard(mPath);
        }
        mLastCheckSDCardTime = System.currentTimeMillis();
        if (!isCanWriteSDCard) {
            return;
        }
        //将日志写入文件
        FileUtils.writLog2File(mPath, mCurrentHour, action);
    }

    //上传日志
    private void doUploadLog2Net(UploadAction action) {
        if (TextUtils.isEmpty(mPath) || action == null || !action.isValid()) {
            return;
        }
        boolean success = prepareUploadLogFile(action);
        if (!success) {
            return;
        }
        action.mUploadRunnable.setUploadAction(action);
        action.mUploadRunnable.setCallBackListener(new UploadRunnable.OnUploadCallBackListener() {
            @Override
            public void onCallBack(int statusCode) {
                synchronized (uploadSync) {
                    mUploadStatusCode = statusCode;
                    if (statusCode == UploadRunnable.FINISH) {
                        mActionCacheQueue.addAll(mUploadActionCacheQueue);
                        mUploadActionCacheQueue.clear();
                        notifyRun();
                    }
                }
            }
        });
        mUploadStatusCode = UploadRunnable.SENDING;
        if (mSingleThreadExecutor == null) {
            mSingleThreadExecutor = Executors.newSingleThreadExecutor(new ThreadFactory() {
                @Override
                public Thread newThread(Runnable r) {
                    Thread t = new Thread(Thread.currentThread().getThreadGroup(), r,
                            "logger-thread-upload", 0);
                    if (t.isDaemon()) {
                        t.setDaemon(false);
                    }
                    if (t.getPriority() != Thread.NORM_PRIORITY) {
                        t.setPriority(Thread.NORM_PRIORITY);
                    }
                    return t;
                }
            });
        }
        mSingleThreadExecutor.execute(action.mUploadRunnable);
    }

    //发送日志前的预处理操作
    private boolean prepareUploadLogFile(UploadAction action) {
        //如果需要上传的endTime小于过期时间，直接不处理
        if (action.mEndTime < TimeUtils.getCurrentHour() - mExpiredTime) {
            action.mUploadLocalPath = "";
            return false;
        }
        String zipLogFiles = FileUtils.zipUploadLogFiles(mPath, action.mStartTime, action.mEndTime);
        if (TextUtils.isEmpty(zipLogFiles)) {
            action.mUploadLocalPath = "";
            return false;
        }
        action.mUploadLocalPath = zipLogFiles;
        return true;
    }
}