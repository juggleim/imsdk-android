package com.jet.im.kit.internal.singleton

import android.content.Context
import androidx.annotation.WorkerThread
import com.sendbird.android.SendbirdChat
import com.sendbird.android.exception.SendbirdException
import com.jet.im.kit.internal.model.notifications.NotificationChannelSettings
import com.jet.im.kit.log.Logger
import java.util.concurrent.CountDownLatch
import java.util.concurrent.atomic.AtomicReference

private const val NOTIFICATION_CHANNEL_SETTINGS = "GLOBAL_NOTIFICATION_CHANNEL_THEME"
private const val LAST_UPDATED_CHANNEL_SETTINGS_AT = "LAST_UPDATED_CHANNEL_SETTINGS_AT"
private const val PREFERENCE_FILE_NAME = "com.sendbird.notifications.channel_settings"

internal class NotificationChannelRepository(context: Context) {
    private val preferences = BaseSharedPreference(context.applicationContext, PREFERENCE_FILE_NAME)

    private var currentUpdatedAt: Long = 0L
        get() {
            return if (field > 0) {
                field
            } else {
                field = preferences.getLong(LAST_UPDATED_CHANNEL_SETTINGS_AT)
                field
            }
        }
        private set(value) {
            if (field != value) {
                field = value
                preferences.putLong(LAST_UPDATED_CHANNEL_SETTINGS_AT, value)
            }
        }
    var settings: NotificationChannelSettings? = null
        private set

    init {
        preferences.getString(NOTIFICATION_CHANNEL_SETTINGS)?.let {
            this.settings = NotificationChannelSettings.fromJson(it)
        }
    }

    fun needToUpdate(latestUpdatedAt: Long): Boolean {
        return currentUpdatedAt < latestUpdatedAt || settings == null
    }

    @WorkerThread
    @Throws(Exception::class)
    fun requestSettings(): NotificationChannelSettings {
        val latch = CountDownLatch(1)
        var error: SendbirdException? = null
        val result: AtomicReference<NotificationChannelSettings> = AtomicReference()
        SendbirdChat.getGlobalNotificationChannelSetting { globalNotificationChannelSetting, e ->
            error = e
            try {
                globalNotificationChannelSetting?.let {
                    Logger.i("++ request response Application theme settings : ${it.jsonPayload}")
                    result.set(NotificationChannelSettings.fromJson(it.jsonPayload))
                }
            } catch (parsingError: Throwable) {
                error = SendbirdException("notification channel settings response data is not valid", parsingError)
            } finally {
                latch.countDown()
            }
        }
        latch.await()
        error?.let { throw it }

        return result.get().also {
            Logger.d("++ currentUpdatedAt=$currentUpdatedAt, response.updatedAt=${it.updatedAt}")
            if (it.updatedAt > 0) currentUpdatedAt = it.updatedAt
            this.settings = it
            preferences.putString(NOTIFICATION_CHANNEL_SETTINGS, it.toString())
        }
    }

    fun clearAll() {
        Logger.d("NotificationChannelRepository::clearAll()")
        currentUpdatedAt = 0L
        settings = null
        preferences.clearAll()
    }
}
