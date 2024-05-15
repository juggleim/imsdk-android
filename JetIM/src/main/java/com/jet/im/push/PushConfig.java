package com.jet.im.push;

public class PushConfig {
    private XMConfig xmConfig;
    private HWConfig hwConfig;
    private VIVOConfig vivoConfig;
    private OPPOConfig oppoConfig;
    private JGConfig jgConfig;

    public void setXMConfig(String appId, String appKey) {
        this.xmConfig = new XMConfig(appId, appKey);
    }

    public void setHWConfig(String appId) {
        this.hwConfig = new HWConfig(appId);
    }

    public void setVIVOConfig() {
        this.vivoConfig = new VIVOConfig();
    }

    public void setOPPOConfig(String appKey, String appSecret) {
        this.oppoConfig = new OPPOConfig(appKey, appSecret);
    }

    public void setJGConfig() {
        this.jgConfig = new JGConfig();
    }

    public XMConfig getXMConfig() {
        return xmConfig;
    }

    public HWConfig getHWConfig() {
        return hwConfig;
    }

    public VIVOConfig getVIVOConfig() {
        return vivoConfig;
    }

    public OPPOConfig getOPPOConfig() {
        return oppoConfig;
    }

    public JGConfig getJGConfig() {
        return jgConfig;
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

    public static class VIVOConfig {
    }

    public static class JGConfig {
    }


    public static class OPPOConfig {
        private final String appKey;
        private final String appSecret;

        public OPPOConfig(String appKey, String appSecret) {
            this.appKey = appKey;
            this.appSecret = appSecret;
        }

        public String getAppKey() {
            return appKey;
        }

        public String getAppSecret() {
            return appSecret;
        }
    }
}

