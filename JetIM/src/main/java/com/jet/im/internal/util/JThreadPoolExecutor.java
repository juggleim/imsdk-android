package com.jet.im.internal.util;

import android.os.Handler;
import android.os.Looper;

import com.jet.im.utils.LoggerUtils;

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

    private static ThreadPoolExecutor pool = createThreadPoolExecutor();

    private static ThreadPoolExecutor createThreadPoolExecutor() {
        if (pool == null) {
            pool = new ThreadPoolExecutor(CORE_POOL_SIZE, MAXIMUM_POOL_SIZE, KEEP_ALIVE_TIME, TimeUnit.SECONDS,
                    new LinkedBlockingQueue<Runnable>(WAIT_COUNT),
                    new CThreadFactory("JThreadPool", Thread.NORM_PRIORITY - 2),
                    new CHandlerException());
        }
        return pool;
    }

    public static class CThreadFactory implements ThreadFactory {
        private AtomicInteger counter = new AtomicInteger(1);
        private String prefix = "";
        private int priority = Thread.NORM_PRIORITY;

        public CThreadFactory(String prefix, int priority) {
            this.prefix = prefix;
            this.priority = priority;
        }

        public CThreadFactory(String prefix) {
            this.prefix = prefix;
        }

        public Thread newThread(Runnable r) {
            Thread executor = new Thread(r, prefix + " #" + counter.getAndIncrement());
            executor.setDaemon(true);
            executor.setPriority(priority);
            return executor;
        }
    }

    private static class CHandlerException extends ThreadPoolExecutor.AbortPolicy {

        @Override
        public void rejectedExecution(Runnable r, ThreadPoolExecutor e) {
            LoggerUtils.d(TAG + ", rejectedExecution:" + r);
            LoggerUtils.e(TAG + ", " + logAllThreadStackTrace());
            if (!pool.isShutdown()) {
                pool.shutdown();
                pool = null;
            }

            pool = createThreadPoolExecutor();
        }
    }

    public static void runInBackground(Runnable runnable) {
        if (pool == null) {
            createThreadPoolExecutor();
        }
        pool.execute(runnable);
    }

    private static Thread mainThread;
    private static Handler mainHandler;

    static {
        Looper mainLooper = Looper.getMainLooper();
        mainThread = mainLooper.getThread();
        mainHandler = new Handler(mainLooper);
    }

    public static boolean isOnMainThread() {
        return mainThread == Thread.currentThread();
    }

    public static void runOnMainThread(Runnable r) {
        if (isOnMainThread()) {
            r.run();
        } else {
            mainHandler.post(r);
        }
    }

    public static void runOnMainThread(Runnable r, long delayMillis) {
        if (delayMillis <= 0) {
            runOnMainThread(r);
        } else {
            mainHandler.postDelayed(r, delayMillis);
        }
    }

    private static HashMap<Runnable, Runnable> mapToMainHandler = new HashMap<>();

    public static void runInBackground(final Runnable runnable, long delayMillis) {
        if (delayMillis <= 0) {
            runInBackground(runnable);
        } else {
            Runnable mainRunnable = () -> {
                mapToMainHandler.remove(runnable);
                pool.execute(runnable);
            };

            //该runnable仍在mapToMainHandler中，表示它并未被执行，需要将其先从mainHandler中移除
            if (mapToMainHandler.containsKey(runnable)) {
                removeCallbackInBackground(runnable);
            }

            mapToMainHandler.put(runnable, mainRunnable);
            mainHandler.postDelayed(mainRunnable, delayMillis);
        }
    }

    public static void removeCallbackOnMainThread(Runnable r) {
        mainHandler.removeCallbacks(r);
    }

    public static void removeCallbackInBackground(Runnable runnable) {
        Runnable mainRunnable = mapToMainHandler.get(runnable);
        if (mainRunnable != null) {
            mainHandler.removeCallbacks(mainRunnable);
            pool.remove(mainRunnable);
        } else
            pool.remove(runnable);
    }

    public static void logStatus() {
        StringBuilder sb = new StringBuilder();
        sb.append("getActiveCount=");
        sb.append(pool.getActiveCount());
        sb.append("\ngetTaskCount=");
        sb.append(pool.getTaskCount());
        sb.append("\ngetCompletedTaskCount=");
        sb.append(pool.getCompletedTaskCount());
        LoggerUtils.d(TAG + ", " + sb);
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
