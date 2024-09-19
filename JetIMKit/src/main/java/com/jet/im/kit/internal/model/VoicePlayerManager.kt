package com.jet.im.kit.internal.model

import android.content.Context
import androidx.annotation.AnyThread
import androidx.annotation.UiThread
import com.jet.im.kit.log.Logger
import com.juggle.im.model.messages.VoiceMessage
import java.io.File
import java.util.concurrent.ConcurrentHashMap

internal object VoicePlayerManager {

    private val cache: MutableMap<String, VoicePlayer> = ConcurrentHashMap()
    private var currentPlayer: VoicePlayer? = null

    @UiThread
    @Synchronized
    @JvmStatic
    fun play(
        context: Context,
        id: String,
        key: String,
        voiceMessage: VoiceMessage,
        onUpdateListener: VoicePlayer.OnUpdateListener,
        onProgressUpdateListener: VoicePlayer.OnProgressUpdateListener
    ) {
        swap(key).apply {
            play(
                context,
                id,
                voiceMessage,
                voiceMessage.duration,
                onUpdateListener,
                onProgressUpdateListener
            )
        }
    }

    @UiThread
    @Synchronized
    @JvmStatic
    fun play(
        context: Context,
        key: String,
        file: File,
        duration: Int,
        onUpdateListener: VoicePlayer.OnUpdateListener,
        onProgressUpdateListener: VoicePlayer.OnProgressUpdateListener
    ) {
        swap(key).apply {
            play(context, file, duration, onUpdateListener, onProgressUpdateListener)
        }
    }

    @Synchronized
    private fun swap(key: String): VoicePlayer {
        if (currentPlayer?.key == key) {
            return requireNotNull(currentPlayer)
        }
        // reset previous player
        currentPlayer?.pause()

        // set the new player
        if (!cache.contains(key)) {
            cache[key] = VoicePlayer(key)
        }
        currentPlayer = cache[key]
        return requireNotNull(currentPlayer)
    }

    @UiThread
    @Synchronized
    @JvmStatic
    fun pause() {
        Logger.i("VoicePlayerManager::pause")
        currentPlayer?.pause()
    }

    @AnyThread
    @Synchronized
    @JvmStatic
    fun dispose(key: String) {
        Logger.i("VoicePlayerManager::dispose, key=$key")
        val player = cache.remove(key)
        player?.dispose()
        if (player == currentPlayer) currentPlayer = null
    }

    @AnyThread
    @Synchronized
    @JvmStatic
    fun disposeAll() {
        Logger.i("VoicePlayerManager::disposeAll")
        cache.forEach {
            it.value.dispose()
        }
        currentPlayer = null
        cache.clear()
    }

    @AnyThread
    @Synchronized
    @JvmStatic
    fun getSeekTo(key: String): Int {
        Logger.i("VoicePlayerManager::getSeekTo, key=$key")
        return try {
            cache[key]?.getSeekTo() ?: 0
        } catch (e: Throwable) {
            0
        }
    }

    @AnyThread
    @Synchronized
    @JvmStatic
    fun getStatus(key: String): VoicePlayer.Status? {
        Logger.i("VoicePlayerManager::getStatus, key=$key")
        return cache[key]?.status
    }

    @AnyThread
    @Synchronized
    @JvmStatic
    fun getCurrentKey(): String? {
        Logger.i("VoicePlayerManager::getCurrentKey")
        return currentPlayer?.key
    }

    @AnyThread
    @Synchronized
    @JvmStatic
    fun addOnUpdateListener(key: String, onUpdateListener: VoicePlayer.OnUpdateListener) {
        Logger.i("VoicePlayerManager::addOnUpdateListener, key=$key")
        cache[key]?.addOnUpdateListener(onUpdateListener)
    }

    @AnyThread
    @Synchronized
    @JvmStatic
    fun addOnProgressUpdateListener(
        key: String,
        onProgressUpdateListener: VoicePlayer.OnProgressUpdateListener
    ) {
        Logger.i("VoicePlayerManager::addOnProgressUpdateListener, key=$key")
        cache[key]?.addOnProgressUpdateListener(onProgressUpdateListener)
    }

    @AnyThread
    @Synchronized
    @JvmStatic
    fun removeOnUpdateListener(key: String, onUpdateListener: VoicePlayer.OnUpdateListener) {
        cache[key]?.removeOnUpdateListener(onUpdateListener)
    }

    @AnyThread
    @Synchronized
    @JvmStatic
    fun removeOnProgressListener(
        key: String,
        onProgressUpdateListener: VoicePlayer.OnProgressUpdateListener
    ) {
        cache[key]?.removeOnProgressListener(onProgressUpdateListener)
    }
}
