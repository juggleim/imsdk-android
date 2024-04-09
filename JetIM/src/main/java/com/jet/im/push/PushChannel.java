package com.jet.im.push;

import android.text.TextUtils;

import java.util.ArrayList;
import java.util.List;

public enum PushChannel {
    HUAWEI(2,"HW", "huawei"),
    XIAOMI(3,"MI", "xiaomi"),
    GOOGLE(4,"FCM", "google"),
    MEIZU(5,"MEIZU", "meizu"),
    VIVO(6,"VIVO", "vivo"),
    OPPO(7,"OPPO", "oppo|realme|oneplus"),
    HONOR(8,"HONOR", "honor");
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
     * 添加该pushType支持的设备厂商名称. 在初始化 SDK 之前调用
     *
     * @param osList 设备厂商名称; 使用{@code Build.MANUFACTURER}获取设备厂商名称.
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
