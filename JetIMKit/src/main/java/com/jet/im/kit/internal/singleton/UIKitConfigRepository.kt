package com.jet.im.kit.internal.singleton

import android.content.Context
import androidx.annotation.VisibleForTesting
import com.sendbird.android.config.UIKitConfigInfo
import com.sendbird.android.exception.SendbirdException
import com.jet.im.kit.internal.contracts.SendbirdChatContract
import com.jet.im.kit.model.configurations.Configurations
import com.jet.im.kit.model.configurations.UIKitConfig
import com.jet.im.kit.model.configurations.UIKitConfigurations
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicReference

@VisibleForTesting
internal const val PREFERENCE_FILE_NAME_CONFIGURATION = "com.jet.im.kit.configurations"

internal class UIKitConfigRepository constructor(
    context: Context,
    appId: String,
) {
    @get:VisibleForTesting
    var lastUpdatedAt: Long = 0L
        private set
    private val isFirstRequestConfig = AtomicBoolean(true)
    private lateinit var preferences: BaseSharedPreference

    @get:VisibleForTesting
    val prefKeyConfigurations = "PREFERENCE_KEY_CONFIGURATION_$appId"

    init {
        // execute IO operations on the executor to avoid strict mode logs
        Executors.newSingleThreadExecutor().submit {
            preferences = BaseSharedPreference(
                context.applicationContext,
                PREFERENCE_FILE_NAME_CONFIGURATION
            )
            val config = preferences.getString(prefKeyConfigurations)?.let {
                Configurations.from(it)
            } ?: Configurations()
            UIKitConfig.uikitConfig.merge(config.uikitConfig)
            this.lastUpdatedAt = config.lastUpdatedAt
        }.get()
    }

    @VisibleForTesting
    fun saveToCache(config: String) {
        preferences.putString(prefKeyConfigurations, config)
    }

    fun clearAll() {
        preferences.clearAll()
    }
}
