package com.jet.im.internal.util;

import android.util.Log;

import com.jet.im.log.JLogLevel;
import com.jet.im.log.JLogManager;

public class JLogger {
    public static void e(String msg) {
        if (JLogManager.getInstance().isDebugModel()) {
            Log.e(TAG, msg);
        }
        JLogManager.getInstance().write(JLogLevel.JLogLevelError, TAG, msg);
    }

    public static void w(String msg) {
        if (JLogManager.getInstance().isDebugModel()) {
            Log.w(TAG, msg);
        }
        JLogManager.getInstance().write(JLogLevel.JLogLevelWarning, TAG, msg);
    }

    public static void i(String msg) {
        if (JLogManager.getInstance().isDebugModel()) {
            Log.i(TAG, msg);
        }
        JLogManager.getInstance().write(JLogLevel.JLogLevelInfo, TAG, msg);
    }

    public static void d(String msg) {
        if (JLogManager.getInstance().isDebugModel()) {
            Log.d(TAG, msg);
        }
        JLogManager.getInstance().write(JLogLevel.JLogLevelDebug, TAG, msg);
    }

    public static void v(String msg) {
        if (JLogManager.getInstance().isDebugModel()) {
            Log.v(TAG, msg);
        }
        JLogManager.getInstance().write(JLogLevel.JLogLevelVerbose, TAG, msg);
    }

    private static final String TAG = "JLogger";
}
