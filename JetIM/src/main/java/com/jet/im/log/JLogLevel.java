package com.jet.im.log;

/**
 * @author Ye_Guli
 * @create 2024-05-22 9:48
 */
public enum JLogLevel {
    JLogLevelNone(0),
    JLogLevelFatal(1),
    JLogLevelError(2),
    JLogLevelWarning(3),
    JLogLevelInfo(4),
    JLogLevelDebug(5),
    JLogLevelVerbose(6);
    private final int code;

    JLogLevel(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
