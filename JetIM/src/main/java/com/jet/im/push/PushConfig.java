package com.jet.im.push;

public class PushConfig {
    private XMConfig xmConfig;
    private HWConfig hwConfig;

    public XMConfig getXMConfig() {
        return xmConfig;
    }

    public void setXMConfig(String appId, String appKey) {
        this.xmConfig = new XMConfig(appId, appKey);
    }

    public void setHWConfig(String appId) {
        this.hwConfig = new HWConfig(appId);
    }

    public HWConfig getHWConfig() {
        return hwConfig;
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

    public static class HWConfig {
        private final String appId;

        public HWConfig(String appId) {
            this.appId = appId;
        }

        public String getAppId() {
            return appId;
        }
    }
}

