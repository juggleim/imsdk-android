package com.jet.im.internal.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.telephony.TelephonyManager;
import android.text.TextUtils;

import androidx.annotation.NonNull;

import java.lang.reflect.Method;

public class JUtility {
    public static SharedPreferences getSP(@NonNull Context context) {
        return context.getSharedPreferences(SP_NAME, 0);
    }
    public static String getDeviceId(Context context) {
        String deviceId = "";
        SharedPreferences sp = getSP(context);
        if (sp != null) {
            deviceId = sp.getString(UUID, "");
        }
        if (deviceId.length() == 0) {
            deviceId = java.util.UUID.randomUUID().toString().replace("-", "");
            if (sp != null) {
                sp.edit().putString(UUID, deviceId).apply();
            }
        }
        return deviceId;
    }

    public static String getNetworkType(Context context) {
        String network = "";
        ConnectivityManager m = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (m == null) {
            return network;
        }
        NetworkInfo info = m.getActiveNetworkInfo();
        if (info != null) {
            network = info.getTypeName();
        }
        return network;
    }

    public static String getCarrier(Context context) {
        String carrier = "";
        TelephonyManager m = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        if (m == null) {
            return carrier;
        }
        carrier = m.getNetworkOperator();
        return carrier;
    }

    /**
     * 获取设备制造厂商
     *
     * @return 设备厂商
     */
    public static String getDeviceManufacturer() {
        String manufacturer = Build.MANUFACTURER.replace("-", "_");
        if (!TextUtils.isEmpty(manufacturer)) {
            if ("vivo".equals(manufacturer)) {
                manufacturer = manufacturer.toUpperCase();
            }
            return manufacturer;
        } else {
            String propName = "ro.miui.ui.version.name";
            String res = getProp(propName);
            if (!TextUtils.isEmpty(res)) {
                return "Xiaomi";
            } else {
                return "";
            }
        }
    }


    /**
     * 获取系统属性
     *
     * @param propName 指定系统属性 key
     * @return 系统属性 value
     */
    private static String getProp(String propName) {
        Class<?> classType;
        String buildVersion = null;
        try {
            classType = Class.forName("android.os.SystemProperties");
            Method getMethod = classType.getDeclaredMethod("get", new Class<?>[]{String.class});
            buildVersion = (String) getMethod.invoke(classType, new Object[]{propName});
        } catch (Exception e) {
            e.printStackTrace();
        }
        return buildVersion;
    }

    private static final String SP_NAME = "j_im_core";
    private static final String UUID = "UUID";
}
