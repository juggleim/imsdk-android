package com.jet.im.internal.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.telephony.TelephonyManager;

import androidx.annotation.NonNull;

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

    private static final String SP_NAME = "j_im_core";
    private static final String UUID = "UUID";
}
