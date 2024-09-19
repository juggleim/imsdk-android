package com.juggle.chat.utils;

import android.content.Context;
import android.widget.Toast;

import com.juggle.chat.BaseApplication;

public class ToastUtils {
    private static Context mContext;

    public static void init(Context context) {
        mContext = context.getApplicationContext();
    }

    public static void show(String content) {
        Toast.makeText(mContext, content, Toast.LENGTH_SHORT).show();
    }
}
