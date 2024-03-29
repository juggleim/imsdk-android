package com.jet.im.utils;

import android.os.Build;
import android.text.TextUtils;

import java.lang.reflect.Method;


public class DeviceUtils {
    private static final String TAG = "DeviceUtils";
    private static final String DEVICE_UUID = "DEVICE_UUID";
    private static final String DEVICE_UNIQUE_ID = "DEVICE_UNIQUE_ID";

    private static volatile boolean ALLOW_GET_MCC_MNC = false;
    private static String MCC_MNC = "";

    private DeviceUtils() {
        // default implementation ignored
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
    
}
