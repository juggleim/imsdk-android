package com.juggle.im;

import android.content.Context;
import android.os.Handler;
import android.text.TextUtils;

import com.juggle.im.call.ICallManager;
import com.juggle.im.call.internal.CallManager;
import com.juggle.im.interfaces.IChatroomManager;
import com.juggle.im.interfaces.IConnectionManager;
import com.juggle.im.interfaces.IConversationManager;
import com.juggle.im.interfaces.IMessageManager;
import com.juggle.im.interfaces.IMomentManager;
import com.juggle.im.interfaces.IUserInfoManager;
import com.juggle.im.internal.ChatroomManager;
import com.juggle.im.internal.MomentManager;
import com.juggle.im.internal.connect.ConnectionManager;
import com.juggle.im.internal.ConstInternal;
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
        mConnectionManager.init();
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

    public String getSDKVersion() {
        return ConstInternal.SDK_VERSION;
    }

    public void setServerUrls(List<String> serverUrls) {
        mCore.setServers(serverUrls);
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

    public IChatroomManager getChatroomManager() {
        return mChatroomManager;
    }

    public IUserInfoManager getUserInfoManager() {
        return mUserInfoManager;
    }

    public ICallManager getCallManager() {
        return mCallManager;
    }

    public IMomentManager getMomentManager() {
        return mMomentManager;
    }

    public String getCurrentUserId() {
        return mCore.getUserId();
    }

    public String getDeviceId(Context context) {
        return JUtility.getDeviceId(context);
    }

    public long getTimeDifference() {
        return mCore.getTimeDifference();
    }

    private static class SingletonHolder {
        static final JIM sInstance = new JIM();
    }

    private JIM() {
        JIMCore core = new JIMCore();
        mCore = core;
        JLogger.getInstance().setCore(core);
        mUserInfoManager = new UserInfoManager(core);
        mChatroomManager = new ChatroomManager(core);
        mMomentManager = new MomentManager(core);
        mCallManager = new CallManager(core, mUserInfoManager);
        mMessageManager = new MessageManager(core, mUserInfoManager, mChatroomManager, mCallManager);
        mConversationManager = new ConversationManager(core, mUserInfoManager, mMessageManager);
        mMessageManager.setSendReceiveListener(mConversationManager);
        mConnectionManager = new ConnectionManager(core, mConversationManager, mMessageManager, mUserInfoManager, mChatroomManager, mCallManager);
        UploadManager uploadManager = new UploadManager(core);
        mMessageManager.setDefaultMessageUploadProvider(uploadManager);
    }

    private final ConnectionManager mConnectionManager;
    private final MessageManager mMessageManager;
    private final ChatroomManager mChatroomManager;
    private final ConversationManager mConversationManager;
    private final UserInfoManager mUserInfoManager;
    private final CallManager mCallManager;
    private final MomentManager mMomentManager;
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
