package com.jet.im.log;

/**
 * @author Ye_Guli
 * @create 2024-05-22 9:48
 */
public enum JLogLevel {
    JLogLevelNone(0, "None"),
    JLogLevelFatal(1, "Fatal"),
    JLogLevelError(2, "Error"),
    JLogLevelWarning(3, "Warning"),
    JLogLevelInfo(4, "Info"),
    JLogLevelDebug(5, "Debug"),
    JLogLevelVerbose(6, "Verbose");
    private final int code;
    private final String name;

    JLogLevel(int code, String name) {
        this.code = code;
        this.name = name;
    }

    public int getCode() {
        return code;
    }

    public String getName() {
        return name;
    }
}
