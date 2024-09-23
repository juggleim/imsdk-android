package com.jet.im.kit.log;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.text.SimpleDateFormat;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

class LoggerConfig {

    private int printLoggerLevel;

    @NonNull
    private Tag defaultTag = Tag.DEFAULT;

    @Nullable
    private String stackPrefix;

    @NonNull
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss.SSS", Locale.US);

    @NonNull
    private Set<String> ignoreStackSet = new HashSet<>();

    static final int DEV = 1;
    static final int VERBOSE = Log.VERBOSE;
    static final int DEBUG = Log.DEBUG;
    static final int INFO = Log.INFO;
    static final int WARN = Log.WARN;
    static final int ERROR = Log.ERROR;
    static final int ASSERT = Log.ASSERT;

    @Nullable
    private String getTraceInfo() {
        StackTraceElement[] stacks = Thread.currentThread().getStackTrace();
        return getTraceInfo(stacks);
    }

    @Nullable
    private String getTraceInfo(@NonNull StackTraceElement[] stacks) {
        StackTraceElement stack = null;
        String loggerName = this.getClass().getCanonicalName();
        String className;
        boolean startTracking = false;
        for (StackTraceElement stack1 : stacks) {
            className = stack1.getClassName();
            if (className.equalsIgnoreCase(loggerName)) {
                startTracking = true;
            }
            if (!startTracking) {
                continue;
            }

            if (className.startsWith(loggerName) || ignoreStackSet.contains(className)) {
                continue;
            }

            stack = stack1;
            break;
        }

        if (stack == null) {
            return null;
        }

        String[] klass = stack.getClassName().split("\\.");
        String method = stack.getMethodName();
        int line = stack.getLineNumber();
        return String.format(Locale.US, "[%s %s:%s():%d]", dateFormat.format(System.currentTimeMillis()), klass[klass.length - 1], method, line);
    }

    @NonNull
    Tag getDefaultTag() {
        return defaultTag;
    }

    @NonNull
    private String getMessageWithTrace(@NonNull String message) {
        String traceInfo = getTraceInfo();
        return String.format("%s %s", traceInfo == null ? "" : traceInfo, message);
    }

    boolean isPrintLoggable(int level) {
        return (level >= printLoggerLevel);
    }

    static class Builder {

        private int printLoggerLevel = DEV;

        @NonNull
        private Tag defaultTag = Tag.DEFAULT;

        @Nullable
        private String stackPrefix;

        @NonNull
        private Set<String> ignoreStackSet = new HashSet<>();

        @NonNull
        Builder setDefaultTag(@NonNull Tag tag) {
            defaultTag = tag;
            return this;
        }

        @NonNull
        Builder setPrintLoggerLevel(int level) {
            printLoggerLevel = level;
            return this;
        }

        @SuppressWarnings("UnusedReturnValue")
        @NonNull
        Builder setIgnoreSet(@NonNull Set<String> set) {
            ignoreStackSet = set;
            return this;
        }

        @NonNull
        Builder setStackPrefix(@NonNull String prefix) {
            stackPrefix = prefix;
            return this;
        }

        @NonNull
        LoggerConfig build() {
            LoggerConfig loggerConfig = new LoggerConfig();
            loggerConfig.defaultTag = defaultTag;
            loggerConfig.printLoggerLevel = printLoggerLevel;
            loggerConfig.stackPrefix = stackPrefix;
            loggerConfig.ignoreStackSet = ignoreStackSet;
            return loggerConfig;
        }
    }


    @NonNull
    String getMessage(boolean withStack, @NonNull String msg) {
        return withStack ? getMessageWithTrace(msg) : msg;
    }
}
