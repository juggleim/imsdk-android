package com.juggle.im.model;

public class MomentMedia {
    public enum MomentMediaType {
        IMAGE(0),
        VIDEO(1);

        MomentMediaType(int value) {
            this.mValue = value;
        }
        public int getValue() {
            return mValue;
        }
        public static MomentMediaType setValue(int value) {
            for (MomentMediaType t : MomentMediaType.values()) {
                if (value == t.mValue) {
                    return t;
                }
            }
            return IMAGE;
        }
        private final int mValue;
    }

    public MomentMediaType getType() {
        return mType;
    }

    public void setType(MomentMediaType type) {
        mType = type;
    }

    public String getUrl() {
        return mUrl;
    }

    public void setUrl(String url) {
        mUrl = url;
    }

    private String mUrl;
    private MomentMediaType mType;
}
