package com.jet.im.log.action;

import androidx.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * @author Ye_Guli
 * @create 2024-05-23 9:38
 */
class ActionTypeEnum {
    static final int TYPE_WRITE = 0;//写日志
    static final int TYPE_UPLOAD = 1;//上传日志
    static final int TYPE_REMOVE_EXPIRED = 2;//移除过期日志

    @IntDef({TYPE_WRITE, TYPE_UPLOAD, TYPE_REMOVE_EXPIRED})
    @Retention(RetentionPolicy.SOURCE)
    public @interface ActionType {
    }
}