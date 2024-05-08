package com.jet.im.kit.log;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;



import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

public class Logger {
    private Logger() {
    }

    private static final int LOG_SEGMENT_SIZE = 2000;
    // logLevel should be initialized before loggerConfig initialization
    private static int logLevel = LoggerConfig.DEV;
    @NonNull
    private static LoggerConfig loggerConfig = initLogConfig();

    public static void setLogLevel(int logLevel) {
        Logger.logLevel = logLevel;
        loggerConfig = initLogConfig();
    }

    @NonNull
    private static LoggerConfig initLogConfig() {
        int printLogLevel = LoggerConfig.DEV;
        if (logLevel == LoggerConfig.DEV) {
//            if (!BuildConfig.DEBUG) {
//                printLogLevel = LoggerConfig.WARN;
//            }
        } else {
            printLogLevel = logLevel;
        }

        LoggerConfig.Builder builder = new LoggerConfig.Builder();
        builder = builder.setDefaultTag(Tag.DEFAULT).setStackPrefix(Tag.DEFAULT.tag()).setPrintLoggerLevel(printLogLevel);

        Set<String> set = new HashSet<>();
        set.add(Logger.class.getName());
        builder.setIgnoreSet(set);
        return builder.build();
    }

    private static int printLog(@NonNull Tag tag, int logLevel, @NonNull String msg) {
        boolean withStack = true;
        String message = loggerConfig.getMessage(withStack, msg);
        if (!loggerConfig.isPrintLoggable(logLevel)) {
            return 0;
        }

        String tagMsg = tag.tag();
        int messageLen = message.length();
        int curIdx = 0, depth = 0;

        int totalPrintLen = 0;
        while (curIdx < messageLen) {
            int remainLen = messageLen - curIdx;
            if (remainLen > LOG_SEGMENT_SIZE) {
                remainLen = LOG_SEGMENT_SIZE;
            }

            totalPrintLen += printLogPartially(logLevel, tagMsg, message.substring(curIdx, curIdx + remainLen), depth++);
            curIdx += remainLen;
        }
        return totalPrintLen;
    }

    private static int printLogPartially(int logLevel, @NonNull String tagMsg, @NonNull String msg, int depth) {
        int msgLen = msg.length();

        int writtenLen = 0;
        String prefix = "";
        if (depth > 0) {
            prefix = String.format(Locale.US, "Cont(%d) ", depth);
        }

        final String printMsg;
        if (msgLen > LOG_SEGMENT_SIZE) {
            printMsg = msg.substring(0, LOG_SEGMENT_SIZE);
        } else {
            printMsg = msg;
        }

        switch (logLevel) {
            case LoggerConfig.DEV:
            case LoggerConfig.DEBUG:
                writtenLen = Log.d(tagMsg, prefix + printMsg);
                break;

            case LoggerConfig.VERBOSE:
                writtenLen = Log.v(tagMsg, prefix + printMsg);
                break;

            case LoggerConfig.INFO:
                writtenLen = Log.i(tagMsg, prefix + printMsg);
                break;

            case LoggerConfig.WARN:
                writtenLen = Log.w(tagMsg, prefix + printMsg);
                break;

            case LoggerConfig.ERROR:
                writtenLen = Log.e(tagMsg, prefix + printMsg);
                break;
        }

        return writtenLen;
    }


    @NonNull
    public static String getCallerTraceInfo(@SuppressWarnings("rawtypes")
                                            @NonNull Class klass) {
        if (!loggerConfig.isPrintLoggable(LoggerConfig.DEBUG)) {
            return "unknown caller";
        }
        StackTraceElement[] stacks = Thread.currentThread().getStackTrace();
        String threadName = Thread.currentThread().getName();
        StackTraceElement stack = null;
        String className;
        String callerClassName = klass.getName();
        boolean found = false;
        for (StackTraceElement stack1 : stacks) {
            className = stack1.getClassName();
            if (className.startsWith(callerClassName)) {
                found = true;
            } else if (found) {
                stack = stack1;
                break;
            }
        }

        if (stack == null) {
            return "";
        }

        String klassName = stack.getClassName();
        String method = stack.getMethodName();
        int line = stack.getLineNumber();
        return String.format(Locale.US, "{%s}-[%s.%s():%d]", threadName, klassName, method, line);
    }

    @NonNull
    private static String getStackTraceString(@Nullable Throwable tr) {
        if (tr == null) return "";
        return Log.getStackTraceString(tr);
    }

    public static int vt(@NonNull Tag tag, @Nullable Throwable tr) {
        return vt(tag, getStackTraceString(tr));
    }

    public static int vt(@NonNull Tag tag, @NonNull String msg, @Nullable Throwable tr) {
        return vt(tag, "%s\n%s", msg, getStackTraceString(tr));
    }

    private static int vt(@NonNull Tag tag, @NonNull String format, @NonNull Object... args) {
        if (loggerConfig.isPrintLoggable(LoggerConfig.VERBOSE)) {
            String message;
            if (args.length > 0) {
                message = String.format(format, args);
            } else {
                message = format;
            }
            return printLog(tag, LoggerConfig.VERBOSE, message);
        } else {
            return 0;
        }
    }

    public static int vt(@NonNull Tag tag, @NonNull String msg) {
        return printLog(tag, LoggerConfig.VERBOSE, msg);
    }

    // verbose with default tag
    public static int v(@NonNull String format, @NonNull Object... args) {
        return vt(loggerConfig.getDefaultTag(), format, args);
    }

    public static int v(@Nullable Throwable tr) {
        return vt(loggerConfig.getDefaultTag(), tr);
    }

    public static int v(@NonNull String msg, @Nullable Throwable tr) {
        return vt(loggerConfig.getDefaultTag(), msg, tr);
    }

    public static int dt(@NonNull Tag tag, @Nullable Throwable tr) {
        return dt(tag, getStackTraceString(tr));
    }

    public static int dt(@NonNull Tag tag, @NonNull String msg, @Nullable Throwable tr) {
        return dt(tag, "%s\n%s", msg, getStackTraceString(tr));
    }

    private static int dt(@NonNull Tag tag, @NonNull String format, @NonNull Object... args) {
        if (loggerConfig.isPrintLoggable(LoggerConfig.DEBUG)) {
            String message;
            if (args.length > 0) {
                message = String.format(format, args);
            } else {
                message = format;
            }
            return printLog(tag, LoggerConfig.DEBUG, message);
        } else {
            return 0;
        }
    }

    public static int dt(@NonNull Tag tag, @NonNull String msg) {
        return printLog(tag, LoggerConfig.DEBUG, msg);
    }

    public static int d(@NonNull String format, @NonNull Object... args) {
        return dt(loggerConfig.getDefaultTag(), format, args);
    }

    public static int d(@NonNull String msg) {
        return dt(loggerConfig.getDefaultTag(), msg);
    }

    public static int d(@Nullable Throwable tr) {
        return dt(loggerConfig.getDefaultTag(), tr);
    }

    public static int d(@NonNull String msg, @Nullable Throwable tr) {
        return dt(loggerConfig.getDefaultTag(), msg, tr);
    }

    public static int it(@NonNull Tag tag, @Nullable Throwable tr) {
        return it(tag, getStackTraceString(tr));
    }

    public static int it(@NonNull Tag tag, @NonNull String msg, @Nullable Throwable tr) {
        return it(tag, "%s\n%s", msg, getStackTraceString(tr));
    }

    private static int it(@NonNull Tag tag, @NonNull String format, @NonNull Object... args) {
        if (loggerConfig.isPrintLoggable(LoggerConfig.INFO)) {
            String message;
            if (args.length > 0) {
                message = String.format(format, args);
            } else {
                message = format;
            }
            return printLog(tag, LoggerConfig.INFO, message);
        } else {
            return 0;
        }
    }

    public static int it(@NonNull Tag tag, @NonNull String msg) {
        return printLog(tag, LoggerConfig.INFO, msg);
    }

    public static int i(@NonNull String format, @NonNull Object... args) {
        return it(loggerConfig.getDefaultTag(), format, args);
    }

    public static int i(@Nullable Throwable tr) {
        return it(loggerConfig.getDefaultTag(), tr);
    }

    public static int i(@NonNull String msg, @Nullable Throwable tr) {
        return it(loggerConfig.getDefaultTag(), msg, tr);
    }

    public static int wt(@NonNull Tag tag, @Nullable Throwable tr) {
        return wt(tag, getStackTraceString(tr));
    }

    public static int wt(@NonNull Tag tag, @NonNull String msg, @Nullable Throwable tr) {
        return wt(tag, "%s\n%s", msg, getStackTraceString(tr));
    }

    private static int wt(@NonNull Tag tag, @NonNull String format, @NonNull Object... args) {
        if (loggerConfig.isPrintLoggable(LoggerConfig.WARN)) {
            String message = String.format(format, args);
            return printLog(tag, LoggerConfig.WARN, message);
        } else {
            return 0;
        }
    }

    public static int wt(@NonNull Tag tag, @NonNull String msg) {
        return printLog(tag, LoggerConfig.WARN, msg);
    }

    public static int w(@NonNull String format, @NonNull Object... args) {
        return wt(loggerConfig.getDefaultTag(), format, args);
    }

    public static int w(@NonNull String msg) {
        return wt(loggerConfig.getDefaultTag(), msg);
    }

    public static int w(@Nullable Throwable tr) {
        return wt(loggerConfig.getDefaultTag(), tr);
    }

    public static int w(@NonNull String msg, @Nullable Throwable tr) {
        return wt(loggerConfig.getDefaultTag(), msg, tr);
    }

    public static int et(@NonNull Tag tag, @Nullable Throwable tr) {
        return et(tag, getStackTraceString(tr));
    }

    public static int et(@NonNull Tag tag, @NonNull String msg, @Nullable Throwable tr) {
        return et(tag, "%s\n%s", msg, getStackTraceString(tr));
    }

    private static int et(@NonNull Tag tag, @NonNull String format, @NonNull Object... args) {
        if (loggerConfig.isPrintLoggable(LoggerConfig.ERROR)) {
            String message;
            if (args.length > 0) {
                message = String.format(format, args);
            } else {
                message = format;
            }
            return printLog(tag, LoggerConfig.ERROR, message);
        } else {
            return 0;
        }
    }

    public static int et(@NonNull Tag tag, @NonNull String msg) {
        return printLog(tag, LoggerConfig.ERROR, msg);
    }

    public static int e(@NonNull String format, @NonNull Object... args) {
        return et(loggerConfig.getDefaultTag(), format, args);
    }

    public static int e(@NonNull String msg) {
        return et(loggerConfig.getDefaultTag(), msg);
    }

    public static int e(@Nullable Throwable tr) {
        return et(loggerConfig.getDefaultTag(), tr);
    }

    public static int e(@NonNull String msg, @Nullable Throwable tr) {
        return et(loggerConfig.getDefaultTag(), msg, tr);
    }

    public static int devt(@NonNull Tag tag, @Nullable Throwable tr) {
        return devt(tag, getStackTraceString(tr));
    }

    public static int devt(@NonNull Tag tag, @NonNull String msg, @Nullable Throwable tr) {
        return devt(tag, "%s\n%s", msg, getStackTraceString(tr));
    }

    private static int devt(@NonNull Tag tag, @NonNull String format, @NonNull Object... args) {
        if (loggerConfig.isPrintLoggable(LoggerConfig.DEV)) {
            String message;
            if (args.length > 0) {
                message = String.format(format, args);
            } else {
                message = format;
            }
            return printLog(tag, LoggerConfig.DEV, message);
        } else {
            return 0;
        }
    }

    public static int devt(@NonNull Tag tag, @NonNull String msg) {
        return printLog(tag, LoggerConfig.DEV, msg);
    }

    public static int dev(@NonNull String format, @NonNull Object... args) {
        return devt(loggerConfig.getDefaultTag(), format, args);
    }

    public static int dev(@NonNull String msg) {
        return devt(loggerConfig.getDefaultTag(), msg);
    }

    public static int dev(@Nullable Throwable tr) {
        return devt(loggerConfig.getDefaultTag(), tr);
    }

    public static int dev(@NonNull String msg, @Nullable Throwable tr) {
        return devt(loggerConfig.getDefaultTag(), msg, tr);
    }
}
