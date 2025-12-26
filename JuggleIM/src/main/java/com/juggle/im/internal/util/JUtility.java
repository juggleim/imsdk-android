package com.juggle.im.internal.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Base64;

import androidx.annotation.NonNull;

import com.juggle.im.internal.ConstInternal;

import java.lang.reflect.Method;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.Locale;

public class JUtility {
    public static Bitmap generateThumbnail(Bitmap image, int targetWidth, int targetHeight) {
        if (image == null) return null;
        int imageWidth = image.getWidth();
        int imageHeight = image.getHeight();
        float scaleFactor;
        int scaledWidth;
        int scaledHeight;

        if ((float) imageWidth / imageHeight < 2.4 && (float) imageHeight / imageWidth < 2.4) {
            float widthFactor = (float) targetWidth / imageWidth;
            float heightFactor = (float) targetHeight / imageHeight;

            scaleFactor = Math.min(widthFactor, heightFactor);
        } else {
            if ((float) imageWidth / imageHeight > 2.4) {
                scaleFactor = 100 * (float) targetHeight / imageHeight / ConstInternal.THUMBNAIL_HEIGHT;
            } else {
                scaleFactor = 100 * (float) targetWidth / imageWidth / ConstInternal.THUMBNAIL_WIDTH;
            }
        }
        scaledWidth = Math.round(imageWidth * scaleFactor);
        scaledHeight = Math.round(imageHeight * scaleFactor);

        Bitmap scaledBitmap = Bitmap.createScaledBitmap(image, scaledWidth, scaledHeight, true);
        Bitmap newBitmap;

        if ((float) imageWidth / imageHeight > 2.4) {
            newBitmap = Bitmap.createBitmap(scaledBitmap, (scaledWidth - ConstInternal.THUMBNAIL_WIDTH) / 2, 0, ConstInternal.THUMBNAIL_WIDTH, scaledHeight);
        } else if ((float) imageHeight / imageWidth > 2.4) {
            newBitmap = Bitmap.createBitmap(scaledBitmap, 0, (scaledHeight - ConstInternal.THUMBNAIL_HEIGHT) / 2, scaledWidth, ConstInternal.THUMBNAIL_HEIGHT);
        } else {
            newBitmap = scaledBitmap;
        }

        return newBitmap;
    }

    public static String base64EncodedStringFrom(byte[] data) {
        return Base64.encodeToString(data, Base64.NO_WRAP);
    }

    public static byte[] dataWithBase64EncodedString(String string) {
        return Base64.decode(string, Base64.NO_WRAP);
    }

    public static SharedPreferences getSP(@NonNull Context context) {
        return context.getSharedPreferences(SP_NAME, 0);
    }

    public static String getDeviceId(Context context) {
        String deviceId = "";
        SharedPreferences sp = getSP(context);
        if (sp != null) {
            deviceId = sp.getString(UUID, "");
        }
        if (deviceId.isEmpty()) {
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
     * 获取本地 IPv4 地址（非回环地址）
     * @return IPv4 地址，若未找到则返回 null
     */
    public static String getLocalIPv4Address() {
        try {
            // 遍历所有网络接口
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface intf = interfaces.nextElement();
                // 跳过回环接口和未启用的接口
                if (intf.isLoopback() || !intf.isUp()) {
                    continue;
                }

                // 遍历接口的所有 IP 地址
                Enumeration<InetAddress> addresses = intf.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    InetAddress addr = addresses.nextElement();
                    // 过滤 IPv4 且非回环地址
                    if (addr instanceof Inet4Address && !addr.isLoopbackAddress()) {
                        return addr.getHostAddress();
                    }
                }
            }
        } catch (SocketException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String getUUID() {
        return java.util.UUID.randomUUID().toString().replace("-", "");
    }

    public static String getSystemLanguage(Context context) {
        Locale current = context.getResources().getConfiguration().locale;
        return current.getLanguage();
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
