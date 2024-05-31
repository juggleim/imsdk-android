package com.jet.im.internal.logger;

/**
 * @author Ye_Guli
 * @create 2024-05-22 9:38
 */
public interface IJLog {
    void setLogConfig(JLogConfig config);

    void removeExpiredLogs();

    void uploadLog(long startTime, long endTime, Callback callback);

    void write(JLogLevel level, String tag, String... keys);

    interface Callback {
        void onSuccess();

        void onError(int code, String msg);
    }
}