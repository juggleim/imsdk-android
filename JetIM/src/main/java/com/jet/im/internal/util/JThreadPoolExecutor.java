package com.jet.im.internal.util;

import android.os.Handler;
import android.os.Looper;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class JThreadPoolExecutor {

    private static final String TAG = JThreadPoolExecutor.class.getSimpleName();
    private static final int CPU_COUNT = Runtime.getRuntime().availableProcessors();
    private static final int CORE_POOL_SIZE = Math.max(2, Math.min(CPU_COUNT - 1, 4));
    private static final int MAXIMUM_POOL_SIZE = CPU_COUNT * 2 + 1;
    private static final long KEEP_ALIVE_TIME = 30L;
    private static final int WAIT_COUNT = 128;

    private static ThreadPoolExecutor mPool = createThreadPoolExecutor();

    private static ThreadPoolExecutor createThreadPoolExecutor() {
        if (mPool == null) {
            mPool = new ThreadPoolExecutor(CORE_POOL_SIZE, MAXIMUM_POOL_SIZE, KEEP_ALIVE_TIME, TimeUnit.SECONDS,
                    new LinkedBlockingQueue<Runnable>(WAIT_COUNT),
                    new CThreadFactory("JThreadPool", Thread.NORM_PRIORITY - 2),
                    new CHandlerException());
        }
        return mPool;
    }

    public static class CThreadFactory implements ThreadFactory {
        private AtomicInteger mCounter = new AtomicInteger(1);
        private String mPrefix = "";
        private int mPriority = Thread.NORM_PRIORITY;

        public CThreadFactory(String prefix, int priority) {
            this.mPrefix = prefix;
            this.mPriority = priority;
        }

        public CThreadFactory(String prefix) {
            this.mPrefix = prefix;
        }

        public Thread newThread(Runnable r) {
            Thread executor = new Thread(r, mPrefix + " #" + mCounter.getAndIncrement());
            executor.setDaemon(true);
            executor.setPriority(mPriority);
            return executor;
        }
    }

    private static class CHandlerException extends ThreadPoolExecutor.AbortPolicy {

        @Override
        public void rejectedExecution(Runnable r, ThreadPoolExecutor e) {
            JLogger.d(TAG + ", rejectedExecution:" + r);
            JLogger.e(TAG + ", " + logAllThreadStackTrace());
            if (!mPool.isShutdown()) {
                mPool.shutdown();
                mPool = null;
            }

            mPool = createThreadPoolExecutor();
        }
    }

    public static void runInBackground(Runnable runnable) {
        if (mPool == null) {
            createThreadPoolExecutor();
        }
        mPool.execute(runnable);
    }

    private static final Thread mMainThread;
    private static final Handler mMainHandler;

    static {
        Looper mainLooper = Looper.getMainLooper();
        mMainThread = mainLooper.getThread();
        mMainHandler = new Handler(mainLooper);
    }

    public static boolean isOnMainThread() {
        return mMainThread == Thread.currentThread();
    }

    public static void runOnMainThread(Runnable r) {
        if (isOnMainThread()) {
            r.run();
        } else {
            mMainHandler.post(r);
        }
    }

    public static void runOnMainThread(Runnable r, long delayMillis) {
        if (delayMillis <= 0) {
            runOnMainThread(r);
        } else {
            mMainHandler.postDelayed(r, delayMillis);
        }
    }

    private static HashMap<Runnable, Runnable> mapToMainHandler = new HashMap<>();

    public static void runInBackground(final Runnable runnable, long delayMillis) {
        if (delayMillis <= 0) {
            runInBackground(runnable);
        } else {
            Runnable mainRunnable = () -> {
                mapToMainHandler.remove(runnable);
                mPool.execute(runnable);
            };

            //该runnable仍在mapToMainHandler中，表示它并未被执行，需要将其先从mainHandler中移除
            if (mapToMainHandler.containsKey(runnable)) {
                removeCallbackInBackground(runnable);
            }

            mapToMainHandler.put(runnable, mainRunnable);
            mMainHandler.postDelayed(mainRunnable, delayMillis);
        }
    }

    public static void removeCallbackOnMainThread(Runnable r) {
        mMainHandler.removeCallbacks(r);
    }

    public static void removeCallbackInBackground(Runnable runnable) {
        Runnable mainRunnable = mapToMainHandler.get(runnable);
        if (mainRunnable != null) {
            mMainHandler.removeCallbacks(mainRunnable);
            mPool.remove(mainRunnable);
        } else
            mPool.remove(runnable);
    }

    public static void logStatus() {
        StringBuilder sb = new StringBuilder();
        sb.append("getActiveCount=");
        sb.append(mPool.getActiveCount());
        sb.append("\ngetTaskCount=");
        sb.append(mPool.getTaskCount());
        sb.append("\ngetCompletedTaskCount=");
        sb.append(mPool.getCompletedTaskCount());
        JLogger.d(TAG + ", " + sb);
    }

    public static StringBuilder logAllThreadStackTrace() {
        StringBuilder builder = new StringBuilder();
        Map<Thread, StackTraceElement[]> liveThreads = Thread.getAllStackTraces();
        for (Thread key : liveThreads.keySet()) {
            builder.append("Thread ").append(key.getName()).append("\n");
            StackTraceElement[] trace = liveThreads.get(key);
            for (StackTraceElement stackTraceElement : trace != null ? trace : new StackTraceElement[0]) {
                builder.append("\tat ").append(stackTraceElement).append("\n");
            }
        }
        return builder;
    }
}
