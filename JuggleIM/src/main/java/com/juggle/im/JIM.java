package com.juggle.im;

import android.content.Context;
import android.os.Handler;
import android.text.TextUtils;

import com.juggle.im.interfaces.IConnectionManager;
import com.juggle.im.interfaces.IConversationManager;
import com.juggle.im.interfaces.IMessageManager;
import com.juggle.im.interfaces.IUserInfoManager;
import com.juggle.im.internal.ConnectionManager;
import com.juggle.im.internal.ConversationManager;
import com.juggle.im.internal.MessageManager;
import com.juggle.im.internal.UploadManager;
import com.juggle.im.internal.UserInfoManager;
import com.juggle.im.internal.core.JIMCore;
import com.juggle.im.internal.logger.JLogConfig;
import com.juggle.im.internal.util.JLogger;
import com.juggle.im.internal.util.JUtility;
import com.juggle.im.push.PushConfig;
import com.juggle.im.push.PushManager;

import java.util.List;

public class JIM {

    public static JIM getInstance() {
        return SingletonHolder.sInstance;
    }

    public void init(Context context, String appKey) {
        init(context, appKey, new InitConfig.Builder().build());
    }

    public void init(Context context, String appKey, InitConfig initConfig) {
        if (context == null) {
            throw new IllegalArgumentException("context is null");
        }
        if (TextUtils.isEmpty(appKey)) {
            throw new IllegalArgumentException("app key is empty");
        }
        if (initConfig == null) {
            throw new IllegalArgumentException("initConfig is null");
        }
        if (initConfig.getJLogConfig() == null) {
            initConfig.setJLogConfig(new JLogConfig.Builder(context).build());
        }
        if (initConfig.getPushConfig() == null) {
            initConfig.setPushConfig(new PushConfig.Builder().build());
        }
        //保存context
        mCore.setContext(context);
        //初始化日志
        JLogger.getInstance().init(initConfig.getJLogConfig());
        //初始化push
        PushManager.getInstance().init(initConfig.getPushConfig());
        //初始化appKey
        JLogger.i("J-Init", "appKey is " + appKey);
        if (appKey.equals(mCore.getAppKey())) {
            return;
        }
        mCore.setAppKey(appKey);
        mCore.setUserId("");
        mCore.setToken("");
    }

    public void setServer(List<String> serverUrls) {
        mCore.setNaviUrl(serverUrls);
    }

    public void setCallbackHandler(Handler callbackHandler) {
        mCore.setCallbackHandler(callbackHandler);
    }

    public IConnectionManager getConnectionManager() {
        return mConnectionManager;
    }

    public IMessageManager getMessageManager() {
        return mMessageManager;
    }

    public IConversationManager getConversationManager() {
        return mConversationManager;
    }

    public IUserInfoManager getUserInfoManager() {
        return mUserInfoManager;
    }

    public String getDeviceId(Context context) {
        return JUtility.getDeviceId(context);
    }
    private static class SingletonHolder {
        static final JIM sInstance = new JIM();
    }

    private JIM() {
        JIMCore core = new JIMCore();
        mCore = core;
        mUserInfoManager = new UserInfoManager(core);
        mMessageManager = new MessageManager(core, mUserInfoManager);
        mConversationManager = new ConversationManager(core, mUserInfoManager, mMessageManager);
        mMessageManager.setSendReceiveListener(mConversationManager);
        mConnectionManager = new ConnectionManager(core, mConversationManager, mMessageManager, mUserInfoManager);
        mUploadManager = new UploadManager(core);
        mMessageManager.setDefaultMessageUploadProvider(mUploadManager);
    }

    private final ConnectionManager mConnectionManager;
    private final MessageManager mMessageManager;
    private final ConversationManager mConversationManager;
    private final UserInfoManager mUserInfoManager;
    private final UploadManager mUploadManager;
    private final JIMCore mCore;

    public static class InitConfig {
        private JLogConfig mJLogConfig;
        private PushConfig mPushConfig;

        public InitConfig(Builder builder) {
            this.mJLogConfig = builder.mJLogConfig;
            this.mPushConfig = builder.mPushConfig;
        }

        public void setJLogConfig(JLogConfig jLogConfig) {
            this.mJLogConfig = jLogConfig;
        }

        public void setPushConfig(PushConfig pushConfig) {
            this.mPushConfig = pushConfig;
        }

        public PushConfig getPushConfig() {
            return mPushConfig;
        }

        public JLogConfig getJLogConfig() {
            return mJLogConfig;
        }

        public static class Builder {
            private JLogConfig mJLogConfig;
            private PushConfig mPushConfig;

            public Builder() {
            }

            public Builder setJLogConfig(JLogConfig mJLogConfig) {
                this.mJLogConfig = mJLogConfig;
                return this;
            }

            public Builder setPushConfig(PushConfig mPushConfig) {
                this.mPushConfig = mPushConfig;
                return this;
            }

            public InitConfig build() {
                return new InitConfig(this);
            }
        }
    }
}
