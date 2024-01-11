package com.example.jetimdemo;

import android.app.Application;

import com.jet.im.JetIM;

public class JetIMApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        JetIM.getInstance().init(this, "appKey");
    }
}
