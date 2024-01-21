package com.jet.im.internal.core.db;

import android.database.Cursor;

class CursorHelper {
    public static String readString(Cursor cursor, String key) {
        try {
            int index = cursor.getColumnIndexOrThrow(key);
            return cursor.getString(index);
        } catch (IllegalArgumentException e) {
            return "";
        }
    }

    public static int readInt(Cursor cursor, String key) {
        try {
            return cursor.getInt(cursor.getColumnIndexOrThrow(key));
        } catch (IllegalArgumentException e) {
            return 0;
        }
    }

    public static long readLong(Cursor cursor, String key) {
        try {
            return cursor.getLong(cursor.getColumnIndexOrThrow(key));
        } catch (IllegalArgumentException e) {
            return 0L;
        }

    }
}
