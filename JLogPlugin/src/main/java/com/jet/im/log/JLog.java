package com.jet.im.log;

import com.jet.im.log.action.ActionManager;

import java.util.Arrays;

/**
 * @author Ye_Guli
 * @create 2024-05-22 17:01
 */
public class JLog implements IJLog {
    private ActionManager mActionManager;

    @Override
    public void setLogConfig(JLogConfig config) {
        if (mActionManager == null) {
            mActionManager = ActionManager.instance(config);
        } else {
            mActionManager.setJLogConfig(config);
        }
    }

    @Override
    public void removeExpiredLogs() {
        if (mActionManager == null) return;

        mActionManager.addRemoveAction();
    }

    @Override
    public void uploadLog(long startTime, long endTime, Callback callback) {
        if (mActionManager == null) return;

        mActionManager.addUploadAction(startTime, endTime, callback);
    }

    @Override
    public void write(JLogLevel level, String tag, String... keys) {
        if (mActionManager == null) return;
        if (level == null || level.getCode() > mActionManager.getJLogConfig().getLogWriteLevel().getCode())
            return;
        if (keys == null || keys.length == 0) return;

        mActionManager.addWriteAction(level, tag, Arrays.asList(keys));
    }
}