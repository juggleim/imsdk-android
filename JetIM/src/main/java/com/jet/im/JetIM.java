package com.jet.im;

import android.content.Context;
import android.text.TextUtils;

import com.jet.im.interfaces.IConnectionManager;
import com.jet.im.interfaces.IConversationManager;
import com.jet.im.interfaces.IMessageManager;
import com.jet.im.interfaces.IUserInfoManager;
import com.jet.im.internal.ConnectionManager;
import com.jet.im.internal.ConversationManager;
import com.jet.im.internal.MessageManager;
import com.jet.im.internal.UserInfoManager;
import com.jet.im.internal.core.JetIMCore;
import com.jet.im.internal.logger.JLogConfig;
import com.jet.im.internal.util.JLogger;
import com.jet.im.push.PushConfig;
import com.jet.im.push.PushManager;

public class JetIM {

    public static JetIM getInstance() {
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
        PushManager.getInstance().init(context, initConfig.getPushConfig());
        //初始化appKey
        JLogger.i("J-Init", "appKey is " + appKey);
        if (appKey.equals(mCore.getAppKey())) {
            return;
        }
        mCore.setAppKey(appKey);
        mCore.setUserId("");
        mCore.setToken("");
    }

    public void setServer(String serverUrl) {
        mCore.setNaviUrl(serverUrl);
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

    private static class SingletonHolder {
        static final JetIM sInstance = new JetIM();
    }

    private JetIM() {
        JetIMCore core = new JetIMCore();
        mCore = core;
        mMessageManager = new MessageManager(core);
        mConversationManager = new ConversationManager(core);
        mMessageManager.setSendReceiveListener(mConversationManager);
        mConnectionManager = new ConnectionManager(core, mConversationManager, mMessageManager);
        mUserInfoManager = new UserInfoManager(core);
    }

    private final ConnectionManager mConnectionManager;
    private final MessageManager mMessageManager;
    private final ConversationManager mConversationManager;
    private final UserInfoManager mUserInfoManager;
    private final JetIMCore mCore;

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
