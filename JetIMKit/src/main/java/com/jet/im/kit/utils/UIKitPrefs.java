package com.jet.im.kit.utils;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.jet.im.kit.log.Logger;

import java.util.concurrent.Executors;

@SuppressWarnings("unused")
final public class UIKitPrefs {
    @NonNull
    private static final String PREFERENCE_FILE_NAME = "com.jet.im.kit.local_preference";

    @Nullable
    private static SharedPreferences preferences;

    private UIKitPrefs() {}

    public static void init(@NonNull Context context) {
        try {
            // execute IO operations on the executor to avoid strict mode logs
            preferences = Executors.newSingleThreadExecutor().submit(() -> context.getApplicationContext().getSharedPreferences(
                PREFERENCE_FILE_NAME,
                Context.MODE_PRIVATE
            )).get();
        } catch (Throwable e) {
            Logger.w(e);
        }
    }

    public static void clearAll() {
        if (preferences == null) return;
        SharedPreferences.Editor editor = preferences.edit();
        editor.clear().apply();
    }

    public static void remove(@NonNull String key) {
        if (preferences == null) return;
        if (preferences.contains(key)) {
            SharedPreferences.Editor editor = preferences.edit();
            editor.remove(key).apply();
        }
    }

    public static void putString(@NonNull String key, @Nullable String value) {
        if (preferences == null) return;
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(key, value).apply();
    }

    @NonNull
    public static String getString(@NonNull String key) {
        return getString(key, "");
    }

    @NonNull
    public static String getString(@NonNull String key, @Nullable String defValue) {
        if (preferences == null) return defValue == null ? "" : defValue;
        final String value = preferences.getString(key, defValue);
        return value == null ? "" : value;
    }

    public static void putLong(@NonNull String key, long value) {
        if (preferences == null) return;
        SharedPreferences.Editor editor = preferences.edit();
        editor.putLong(key, value).apply();
    }

    public static long getLong(@NonNull String key) {
        return getLong(key, 0L);
    }

    public static long getLong(@NonNull String key, long defValue) {
        if (preferences == null) return defValue;
        return preferences.getLong(key, defValue);
    }

    public static void putInt(@NonNull String key, int value) {
        if (preferences == null) return;
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt(key, value).apply();
    }

    public static int getInt(@NonNull String key) {
        return getInt(key, 0);
    }

    public static int getInt(@NonNull String key, int defValue) {
        if (preferences == null) return defValue;
        return preferences.getInt(key, defValue);
    }

    public static void putBoolean(@NonNull String key, boolean value) {
        if (preferences == null) return;
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean(key, value).apply();
    }

    public static boolean getBoolean(@NonNull String key) {
        return getBoolean(key, false);
    }

    public static boolean getBoolean(@NonNull String key, boolean defValue) {
        if (preferences == null) return defValue;
        return preferences.getBoolean(key, defValue);
    }
}
