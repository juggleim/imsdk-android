package com.example.demo.utils;

import android.content.Context;
import android.widget.Toast;

import com.example.demo.BaseApplication;

public class ToastUtils {
    private static Context mContext;

    public static void init(Context context) {
        mContext = context.getApplicationContext();
    }

    public static void show(String content) {
        Toast.makeText(mContext, content, Toast.LENGTH_SHORT).show();
    }
}
