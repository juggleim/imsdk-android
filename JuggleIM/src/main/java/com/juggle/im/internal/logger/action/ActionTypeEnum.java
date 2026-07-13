package com.juggle.im.internal.logger.action;

import androidx.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * @author Ye_Guli
 * @create 2024-05-23 9:38
 */
class ActionTypeEnum {
    static final int TYPE_WRITE = 0;//Write logs
    static final int TYPE_UPLOAD = 1;//Upload logs
    static final int TYPE_REMOVE_EXPIRED = 2;//Remove expired logs

    @IntDef({TYPE_WRITE, TYPE_UPLOAD, TYPE_REMOVE_EXPIRED})
    @Retention(RetentionPolicy.SOURCE)
    public @interface ActionType {
    }
}