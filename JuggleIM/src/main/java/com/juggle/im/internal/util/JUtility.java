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
import java.nio.charset.StandardCharsets;
import java.util.Enumeration;
import java.util.Locale;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

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
     * Get the device manufacturer
     *
     * @return Device manufacturer
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
     * Get the local IPv4 address (non-loopback)
     * @return IPv4 address, or null if not found
     */
    public static String getLocalIPv4Address() {
        try {
            // Iterate over all network interfaces
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface intf = interfaces.nextElement();
                // Skip loopback and disabled interfaces
                if (intf.isLoopback() || !intf.isUp()) {
                    continue;
                }

                // Iterate over all IP addresses on the interface
                Enumeration<InetAddress> addresses = intf.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    InetAddress addr = addresses.nextElement();
                    // Filter for IPv4 non-loopback addresses
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
     * Get a system property
     *
     * @param propName Specified system property key
     * @return System property value
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

    public static String createSignature(String nonce, String timestamp, String signKey) {
        // 1. Null handling: convert null to an empty string (matching OC nonce ?: @"")
        nonce = (nonce == null) ? "" : nonce;
        timestamp = (timestamp == null) ? "" : timestamp;
        signKey = (signKey == null) ? "" : signKey;

        // 2. Build the string: nonce + timestamp + signKey (exactly matching OC concatenation rules)
        String raw = nonce + timestamp + signKey;

        // 3. Encode as UTF-8 bytes (matching OC dataUsingEncoding:NSUTF8StringEncoding)
        byte[] rawData = raw.getBytes(StandardCharsets.UTF_8);
        byte[] keyData = signKey.getBytes(StandardCharsets.UTF_8);

        // 4. Initialize HMAC-SHA256 encryption (matching OC CCHmac)
        Mac mac;
        try {
            mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKey = new SecretKeySpec(keyData, "HmacSHA256");
            mac.init(secretKey);
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }

        byte[] digest = mac.doFinal(rawData);

        // 5. Convert to a lowercase hexadecimal string (matching OC %02x format)
        StringBuilder signature = new StringBuilder();
        for (byte b : digest) {
            // Two lowercase hex digits, padding with 0 when needed (exactly matching OC output)
            signature.append(String.format("%02x", b));
        }

        return signature.toString();
    }

    public static String maskAppKey(String string) {
        if (string == null || string.isEmpty()) {
            return "";
        }

        StringBuilder result = new StringBuilder(string);
        int length = result.length();

        if (length > 3) {
            result.replace(0, 4, "****");
        }

        if (length > 11) {
            result.replace(8, 12, "****");
        }

        return result.toString();
    }

    public static String maskToken(String string) {
        if (string == null || string.isEmpty()) {
            return "";
        }

        int length = string.length();
        StringBuilder result = new StringBuilder(string);

        for (int i = 0; i < length; i++) {
            boolean needKeep = false;

            if (i >= length - 10) {
                needKeep = true;
            }
            else if (i >= length - 30 && i <= length - 21) {
                needKeep = true;
            }

            if (!needKeep) {
                result.setCharAt(i, '*');
            }
        }

        return result.toString();
    }

    private static final String SP_NAME = "j_im_core";
    private static final String UUID = "UUID";
}
