package com.example.demo

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.multidex.MultiDexApplication
import com.example.demo.common.consts.InitState
import com.example.demo.common.consts.StringSet
import com.example.demo.common.preferences.PreferenceUtils
import com.example.demo.utils.SSLHelper
import com.example.demo.utils.ToastUtils
import com.juggle.im.JIM
import com.jet.im.kit.SendbirdUIKit
import com.jet.im.kit.adapter.SendbirdUIKitAdapter
import com.jet.im.kit.consts.ReplyType
import com.jet.im.kit.consts.ThreadReplySelectType
import com.jet.im.kit.consts.TypingIndicatorType
import com.jet.im.kit.interfaces.CustomParamsHandler
import com.jet.im.kit.interfaces.UserInfo
import com.jet.im.kit.model.configurations.UIKitConfig
import com.sendbird.android.exception.SendbirdException
import com.sendbird.android.handler.InitResultHandler
import com.sendbird.android.params.OpenChannelCreateParams
import javax.net.ssl.HostnameVerifier
import javax.net.ssl.HttpsURLConnection
import javax.net.ssl.SSLSession


private const val APP_ID = "FEA2129A-EA73-4EB9-9E0B-EC738E7EB768"
internal const val enableAiChatBotSample = false
internal const val enableNotificationSample = false

class BaseApplication : MultiDexApplication() {
    companion object {
        internal val initState = MutableLiveData(InitState.NONE)

        /**
         * Returns the state of the result from initialization of Sendbird UIKit.
         *
         * @return the [InitState] instance
         */
        fun initStateChanges(): LiveData<InitState> {
            return initState
        }

        private fun initUIKit(context: Context) {
            SendbirdUIKit.init(object : SendbirdUIKitAdapter {
                override fun getAppId(): String = PreferenceUtils.appId.ifEmpty { APP_ID }

                override fun getAccessToken(): String? = null

                override fun getUserInfo(): UserInfo = object : UserInfo {
                    override fun getUserId(): String = PreferenceUtils.userId
                    override fun getNickname(): String = PreferenceUtils.nickname
                    override fun getProfileUrl(): String = PreferenceUtils.profileUrl
                }

                override fun getInitResultHandler(): InitResultHandler =
                    object : InitResultHandler {
                        override fun onMigrationStarted() {
                            initState.value = InitState.MIGRATING
                        }

                        override fun onInitFailed(e: SendbirdException) {
                            initState.value = InitState.FAILED
                        }

                        override fun onInitSucceed() {
                            initState.value = InitState.SUCCEED
                        }
                    }
            }, context)

            // set theme mode
            SendbirdUIKit.setDefaultThemeMode(PreferenceUtils.themeMode)
            // set logger
            SendbirdUIKit.setLogLevel(SendbirdUIKit.LogLevel.ALL)
        }

        /**
         * In a sample app, different contextual settings are used in a single app.
         * These are only used in the sample, because if the app kills and resurrects due to low memory, the last used sample settings should be preserved.
         */
        fun setupConfigurations() {
            // set whether to use user profile
            UIKitConfig.common.enableUsingDefaultUserProfile = true
            // set whether to use typing indicators in channel list
            UIKitConfig.groupChannelListConfig.enableTypingIndicator = true
            // set whether to use read/delivery receipt in channel list
            UIKitConfig.groupChannelListConfig.enableMessageReceiptStatus = true
            // set whether to use user mention
            UIKitConfig.groupChannelConfig.enableMention = true
            // set reply type
            UIKitConfig.groupChannelConfig.replyType = ReplyType.NONE
            UIKitConfig.groupChannelConfig.threadReplySelectType = ThreadReplySelectType.THREAD
            // set whether to use voice message
            UIKitConfig.groupChannelConfig.enableVoiceMessage = true
            // set typing indicator types
            UIKitConfig.groupChannelConfig.typingIndicatorTypes =
                setOf(TypingIndicatorType.BUBBLE, TypingIndicatorType.TEXT)
            // set whether to use feedback
            UIKitConfig.groupChannelConfig.enableFeedback = true
            // set custom params
            SendbirdUIKit.setCustomParamsHandler(object : CustomParamsHandler {
                override fun onBeforeCreateOpenChannel(params: OpenChannelCreateParams) {
                    // You can set OpenChannelCreateParams globally before creating a open channel.
                    params.customType = StringSet.SB_COMMUNITY_TYPE
                }
            })
        }
    }

    override fun onCreate() {
        super.onCreate()
        ToastUtils.init(applicationContext)
        PreferenceUtils.init(applicationContext)
        // initialize SendbirdUIKit
        val navi = ArrayList<String>()
        navi.add("https://nav.juggleim.com")
        JIM.getInstance().setServer(navi)
        initUIKit(this)
        JIM.getInstance().init(this, "nsw3sue72begyv7y")
        // setup uikit configurations
        HttpsURLConnection.setDefaultSSLSocketFactory(SSLHelper.getTrustAllSSLSocketFactory())
        HttpsURLConnection.setDefaultHostnameVerifier(object :HostnameVerifier{
            override fun verify(hostname: String?, session: SSLSession?): Boolean {
                return true
            }
        })
        setupConfigurations()
    }
}
