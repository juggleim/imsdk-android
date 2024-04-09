package com.example.jetimdemo;

import android.app.Application;

import com.jet.im.JetIM;
import com.jet.im.push.PushConfig;

public class JetIMApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
//        JetIM.getInstance().setServer("https://nav.gxjipei.com");
        PushConfig pushConfig = new PushConfig();
        JetIM.getInstance().init(this, "appkey");
    }
}
