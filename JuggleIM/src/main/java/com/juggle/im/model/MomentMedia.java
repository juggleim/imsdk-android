package com.juggle.im.model;

import org.json.JSONException;
import org.json.JSONObject;

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

    public JSONObject toJson() {
        JSONObject json = new JSONObject();

        try {
            if (mUrl != null) {
                json.put("url", mUrl);
            }
            if (mSnapshotUrl != null) {
                json.put("snapshot_url", mSnapshotUrl);
            }
            if (mType == MomentMediaType.IMAGE) {
                json.put("type", "image");
            } else if (mType == MomentMediaType.VIDEO) {
                json.put("type", "video");
            }
            json.put("height", mHeight);
            json.put("width", mWidth);
            json.put("duration", mDuration);
        } catch (JSONException ignored) {

        }
        return json;
    }

    public static MomentMedia fromJson(JSONObject json) {
        if (json == null) {
            return null;
        }

        MomentMedia media = new MomentMedia();
        media.mUrl = json.optString("url");
        media.mSnapshotUrl = json.optString("snapshot_url");
        String typeStr = json.optString("type");
        if ("image".equals(typeStr)) {
            media.mType = MomentMediaType.IMAGE;
        } else if ("video".equals(typeStr)) {
            media.mType = MomentMediaType.VIDEO;
        } else {
            int typeInt = json.optInt("type", -1);
            media.mType = (typeInt == 0) ? MomentMediaType.IMAGE : (typeInt == 1) ? MomentMediaType.VIDEO : null;
        }

        media.mHeight = json.optInt("height");
        media.mWidth = json.optInt("width");
        media.mDuration = json.optInt("duration");

        return media;
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

    public String getSnapshotUrl() {
        return mSnapshotUrl;
    }

    public void setSnapshotUrl(String snapshotUrl) {
        mSnapshotUrl = snapshotUrl;
    }

    public int getHeight() {
        return mHeight;
    }

    public void setHeight(int height) {
        mHeight = height;
    }

    public int getWidth() {
        return mWidth;
    }

    public void setWidth(int width) {
        mWidth = width;
    }

    public int getDuration() {
        return mDuration;
    }

    public void setDuration(int duration) {
        mDuration = duration;
    }

    private String mUrl;
    private MomentMediaType mType;
    private String mSnapshotUrl;
    private int mHeight;
    private int mWidth;
    private int mDuration;
}
