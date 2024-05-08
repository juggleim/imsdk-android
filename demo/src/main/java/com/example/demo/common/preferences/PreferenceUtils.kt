package com.example.demo.common.preferences

import android.content.Context
import com.jet.im.kit.SendbirdUIKit.ThemeMode
import com.example.demo.BaseApplication
import com.example.demo.common.consts.SampleType

/**
 * This provides methods to manage preferences data.
 */
internal object PreferenceUtils {
    private const val PREFERENCE_KEY_APP_ID = "PREFERENCE_KEY_APP_ID"
    private const val PREFERENCE_KEY_USER_ID = "PREFERENCE_KEY_USER_ID"
    private const val PREFERENCE_KEY_NICKNAME = "PREFERENCE_KEY_NICKNAME"
    private const val PREFERENCE_KEY_PROFILE_URL = "PREFERENCE_KEY_PROFILE_URL"
    private const val PREFERENCE_KEY_THEME_MODE = "PREFERENCE_KEY_THEME_MODE"
    private const val PREFERENCE_KEY_DO_NOT_DISTURB = "PREFERENCE_KEY_DO_NOT_DISTURB"
    private const val PREFERENCE_KEY_LATEST_USED_SAMPLE = "PREFERENCE_KEY_LATEST_USED_SAMPLE"
    private const val PREFERENCE_KEY_NOTIFICATION_USE_AUTHENTICATE = "PREFERENCE_KEY_NOTIFICATION_USE_AUTHENTICATE"
    private const val PREFERENCE_KEY_NOTIFICATION_USE_FEED_CHANNEL_ONLY = "PREFERENCE_KEY_NOTIFICATION_USE_FEED_CHANNEL_ONLY"

    private lateinit var pref: Preference
    fun init(context: Context) {
        pref = Preference(context, "sendbird-sample")
    }

    var appId: String
        get() = pref.getString(PREFERENCE_KEY_APP_ID) ?: ""
        set(value) = pref.putString(PREFERENCE_KEY_APP_ID, value)

    var userId: String
        get() = pref.getString(PREFERENCE_KEY_USER_ID) ?: ""
        set(value) = pref.putString(PREFERENCE_KEY_USER_ID, value)

    var nickname: String
        get() = pref.getString(PREFERENCE_KEY_NICKNAME) ?: ""
        set(nickname) = pref.putString(PREFERENCE_KEY_NICKNAME, nickname)

    var profileUrl: String
        get() = pref.getString(PREFERENCE_KEY_PROFILE_URL) ?: ""
        set(profileUrl) = pref.putString(PREFERENCE_KEY_PROFILE_URL, profileUrl)

    var themeMode: ThemeMode
        get() = pref.getString(PREFERENCE_KEY_THEME_MODE)?.let {
            ThemeMode.valueOf(it)
        } ?: ThemeMode.Light
        set(themeMode) = pref.putString(PREFERENCE_KEY_THEME_MODE, themeMode.name)

    var doNotDisturb: Boolean
        get() = pref.getBoolean(PREFERENCE_KEY_DO_NOT_DISTURB, false)
        set(doNotDisturb) = pref.putBoolean(PREFERENCE_KEY_DO_NOT_DISTURB, doNotDisturb)

//    var isUsingAuthenticate: Boolean
//        get() = pref.getBoolean(PREFERENCE_KEY_NOTIFICATION_USE_AUTHENTICATE, false)
//        set(useAuth) = pref.putBoolean(PREFERENCE_KEY_NOTIFICATION_USE_AUTHENTICATE, useAuth)

    var isUsingFeedChannelOnly: Boolean
        get() = pref.getBoolean(PREFERENCE_KEY_NOTIFICATION_USE_FEED_CHANNEL_ONLY, false)
        set(useFeedOnly) = pref.putBoolean(PREFERENCE_KEY_NOTIFICATION_USE_FEED_CHANNEL_ONLY, useFeedOnly)

    var selectedSampleType: SampleType?
        get() {
            pref.getString(PREFERENCE_KEY_LATEST_USED_SAMPLE)?.let {
                return SampleType.valueOf(it)
            } ?: return null
        }
        set(value) {
            if (value == null) {
                pref.remove(PREFERENCE_KEY_LATEST_USED_SAMPLE)
            } else {
                pref.putString(PREFERENCE_KEY_LATEST_USED_SAMPLE, value.name)
            }
            BaseApplication.setupConfigurations()
        }

    fun clearAll() = pref.clear()
}
