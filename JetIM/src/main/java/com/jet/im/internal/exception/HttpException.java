package com.jet.im.internal.exception;

public class HttpException extends Exception {
    private static final String msg = "http response code is error,code is:";
    private int errorCode;

    public HttpException(int code) {
        super(msg + code);
        errorCode = code;
    }

    public int getErrorCode() {
        return errorCode;
    }
}
