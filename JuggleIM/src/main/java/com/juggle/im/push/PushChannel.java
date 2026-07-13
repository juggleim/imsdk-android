package com.juggle.im.push;

import android.text.TextUtils;

import java.util.ArrayList;
import java.util.List;

public enum PushChannel {
    HUAWEI(2, "HW", "huawei"),
    XIAOMI(3, "MI", "xiaomi"),
    OPPO(4, "OPPO", "oppo|realme|oneplus"),
    VIVO(5, "VIVO", "vivo"),
    GOOGLE(6, "FCM", "google"),
    JIGUANG(7, "JIGUANG", "jiguang"),
    MEIZU(8, "MEIZU", "meizu"),
    HONOR(9, "HONOR", "honor");

    private final String name;
    private String os;
    private int code;

    PushChannel(int code, String name, String os) {
        this.code = code;
        this.name = name;
        this.os = os;
    }

    public String getName() {
        return this.name;
    }

    public String getOs() {
        return os;
    }

    public int getCode() {
        return code;
    }

    /**
     * Adds device manufacturer names supported by this pushType. Call before initializing the SDK.
     *
     * @param osList device manufacturer names; use {@code Build.MANUFACTURER} to get the device manufacturer name.
     */
    public void appendOs(String... osList) {
        if (osList == null || osList.length == 0) {
            return;
        }

        List<String> osListFiltered = new ArrayList<>();
        for (String os : osList) {
            if (TextUtils.isEmpty(os)) {
                continue;
            }
            osListFiltered.add(os);
        }

        if (osListFiltered.isEmpty()) {
            return;
        }

        os = os + "|" + TextUtils.join("|", osListFiltered);
    }
}
