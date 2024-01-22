package com.jet.im.model.messages;

import android.text.TextUtils;

import com.jet.im.model.MessageContent;
import com.jet.im.utils.LoggerUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;

public class FileMessage extends MessageContent {
    public FileMessage() {
        mContentType = "jg:file";
    }

    @Override
    public byte[] encode() {
        JSONObject jsonObject = new JSONObject();
        try {
            if (!TextUtils.isEmpty(mName)) {
                jsonObject.put(NAME, mName);
            }
            if (!TextUtils.isEmpty(mUrl)) {
                jsonObject.put(URL, mUrl);
            }
            jsonObject.put(SIZE, mSize);
            if (!TextUtils.isEmpty(mType)) {
                jsonObject.put(TYPE, mType);
            }
        } catch (JSONException e) {
            LoggerUtils.e("FileMessage JSONException " + e.getMessage());
        }
        return jsonObject.toString().getBytes(StandardCharsets.UTF_8);
    }

    @Override
    public void decode(byte[] data) {
        if (data == null) {
            LoggerUtils.e("FileMessage decode data is null");
            return;
        }
        String jsonStr = new String(data, StandardCharsets.UTF_8);

        try {
            JSONObject jsonObject = new JSONObject(jsonStr);
            if (jsonObject.has(URL)) {
                mUrl = jsonObject.optString(URL);
            }
            if (jsonObject.has(NAME)) {
                mName = jsonObject.optString(NAME);
            }
            if (jsonObject.has(SIZE)) {
                mSize = jsonObject.optLong(SIZE);
            }
            if (jsonObject.has(TYPE)) {
                mType = jsonObject.optString(TYPE);
            }
        } catch (JSONException e) {
            LoggerUtils.e("FileMessage decode JSONException " + e.getMessage());
        }
    }

    @Override
    public String conversationDigest() {
        return DIGEST;
    }

    public String getName() {
        return mName;
    }

    public void setName(String name) {
        this.mName = name;
    }

    public String getUrl() {
        return mUrl;
    }

    public void setUrl(String url) {
        this.mUrl = url;
    }

    public long getSize() {
        return mSize;
    }

    public void setSize(long size) {
        this.mSize = size;
    }

    public String getType() {
        return mType;
    }

    public void setType(String type) {
        this.mType = type;
    }

    private String mName;
    private String mUrl;
    private long mSize;
    private String mType;

    private static final String NAME = "name";
    private static final String URL = "url";
    private static final String SIZE = "size";
    private static final String TYPE = "type";
    private static final String DIGEST = "[File]";
}
