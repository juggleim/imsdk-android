package com.jet.im.kit.internal.singleton

import android.content.Context
import android.content.SharedPreferences

internal class BaseSharedPreference(
    context: Context,
    fileName: String,
    mode: Int = Context.MODE_PRIVATE
) {

    private val preferences: SharedPreferences = context.applicationContext.getSharedPreferences(
        fileName,
        mode
    )

    fun clearAll() {
        preferences.edit().clear().apply()
    }

    fun loadAll(predicate: (String) -> Boolean, onEach: (String, Any?) -> Unit) {
        val all = preferences.all

        all.entries
            .filter { predicate(it.key) }
            .forEach { onEach(it.key, it.value) }
    }

    fun remove(key: String) {
        if (key in preferences) {
            preferences.edit().remove(key).apply()
        }
    }

    fun putString(key: String, value: String) {
        preferences.edit().putString(key, value).apply()
    }

    fun getString(key: String): String? = preferences.getString(key, null)

    fun optString(key: String, default: String = ""): String = preferences.getString(key, default) ?: default

    fun putLong(key: String, value: Long) {
        preferences.edit().putLong(key, value).apply()
    }

    fun getLong(key: String): Long = preferences.getLong(key, 0L)

    fun contains(key: String): Boolean = preferences.contains(key)
}
