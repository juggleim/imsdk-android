package com.example.jetimdemo;

import android.app.Application;

import com.jet.im.JetIM;

public class JetIMApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        JetIM.getInstance().setServer("http://8.130.171.185:8083");
        JetIM.getInstance().init(this, "appkey");
    }
}
