package com.example.jetimdemo;

import android.app.Application;

import com.jet.im.JetIM;
import com.jet.im.internal.logger.JLogConfig;
import com.jet.im.internal.logger.JLogLevel;
import com.jet.im.push.PushConfig;

import java.util.ArrayList;
import java.util.List;

public class JetIMApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        List<String> serverList = new ArrayList<>();
//        serverList.add("https://im-nav.yometalk.com");
//        JetIM.getInstance().setServer(serverList);
        JetIM.InitConfig initConfig = new JetIM.InitConfig.Builder()
                .setPushConfig(new PushConfig.Builder().build())
                .setJLogConfig(new JLogConfig.Builder(getApplicationContext()).setLogConsoleLevel(JLogLevel.JLogLevelVerbose).build())
                .build();
        JetIM.getInstance().init(this, "appkey", initConfig);
    }
}
