package com.juggle.im.internal.core.db;

import android.database.Cursor;

import com.juggle.im.internal.model.E2EEInfo;

public class E2EESql {
    static final String SQL_CREATE_PUBLIC_KEY_TABLE = "CREATE TABLE IF NOT EXISTS public_key ("
            + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
            + "user_id VARCHAR (64),"
            + "device_id VARCHAR (64),"
            + "public_key BLOB"
            + ")";
    static final String SQL_CREATE_PUBLIC_KEY_INDEX = "CREATE UNIQUE INDEX IF NOT EXISTS idx_public_key ON public_key(user_id, device_id)";
    static final String SQL_GET_E2EE_INFO = "SELECT * FROM public_key WHERE user_id = ?";
    static final String SQL_UPDATE_E2EE_INFO = "INSERT OR REPLACE INTO public_key (user_id, device_id, public_key) VALUES (?, ?, ?)";

    static E2EEInfo e2EEInfoWithCursor(Cursor cursor) {
        E2EEInfo info = new E2EEInfo();
        info.setUserId(CursorHelper.readString(cursor, "user_id"));
        info.setDeviceId(CursorHelper.readString(cursor, "device_id"));
        info.setPubKey(CursorHelper.readBytes(cursor, "public_key"));
        return info;
    }

    static String bytesToHex(byte[] bytes) {
        if (bytes == null || bytes.length == 0) {
            return "";
        }
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            // 转两位十六进制，不足补0
            String hex = Integer.toHexString(0xFF & b);
            if (hex.length() == 1) {
                sb.append('0');
            }
            sb.append(hex);
        }
        return sb.toString();
    }

    static byte[] hexToBytes(String hexStr) {
        if (hexStr == null || hexStr.length() % 2 != 0) {
            throw new IllegalArgumentException("非法十六进制字符串，长度必须为偶数");
        }
        int len = hexStr.length() / 2;
        byte[] result = new byte[len];
        for (int i = 0; i < len; i++) {
            // 截取两位
            String sub = hexStr.substring(i * 2, i * 2 + 2);
            // 十六进制转数字
            result[i] = (byte) Integer.parseInt(sub, 16);
        }
        return result;
    }
}
