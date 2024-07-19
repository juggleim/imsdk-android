package com.juggle.im.push;

public class PushConfig {
    private XMConfig xmConfig;
    private HWConfig hwConfig;
    private VIVOConfig vivoConfig;
    private OPPOConfig oppoConfig;
    private JGConfig jgConfig;

    public PushConfig(Builder builder) {
        this.xmConfig = builder.xmConfig;
        this.hwConfig = builder.hwConfig;
        this.vivoConfig = builder.vivoConfig;
        this.oppoConfig = builder.oppoConfig;
        this.jgConfig = builder.jgConfig;
    }

    public void setXmConfig(XMConfig xmConfig) {
        this.xmConfig = xmConfig;
    }

    public void setHwConfig(HWConfig hwConfig) {
        this.hwConfig = hwConfig;
    }

    public void setVivoConfig(VIVOConfig vivoConfig) {
        this.vivoConfig = vivoConfig;
    }

    public void setOppoConfig(OPPOConfig oppoConfig) {
        this.oppoConfig = oppoConfig;
    }

    public void setJgConfig(JGConfig jgConfig) {
        this.jgConfig = jgConfig;
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

    public static class Builder {
        private XMConfig xmConfig;
        private HWConfig hwConfig;
        private VIVOConfig vivoConfig;
        private OPPOConfig oppoConfig;
        private JGConfig jgConfig;

        public Builder() {
        }

        public Builder setXmConfig(String appId, String appKey) {
            this.xmConfig = new XMConfig(appId, appKey);
            return this;
        }

        public Builder setHwConfig(String appId) {
            this.hwConfig = new HWConfig(appId);
            return this;
        }

        public Builder setVivoConfig() {
            this.vivoConfig = new VIVOConfig();
            return this;
        }

        public Builder setOppoConfig(String appKey, String appSecret) {
            this.oppoConfig = new OPPOConfig(appKey, appSecret);
            return this;
        }

        public Builder setJgConfig() {
            this.jgConfig = new JGConfig();
            return this;
        }

        public PushConfig build() {
            return new PushConfig(this);
        }
    }
}

