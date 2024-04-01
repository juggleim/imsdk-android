package com.jet.im.push;

public class PushConfig {
    private XMConfig xmConfig;

    public XMConfig getXmConfig() {
        return xmConfig;
    }

    public void setXmConfig(String appId, String appKey) {
        this.xmConfig = new XMConfig(appId, appKey);
    }

    public static class XMConfig {
        private final String appId;
        private final String appKey;

        public XMConfig(String appId, String appKey) {
            this.appId = appId;
            this.appKey = appKey;
        }

        public String getAppId() {
            return appId;
        }

        public String getAppKey() {
            return appKey;
        }
    }
}

