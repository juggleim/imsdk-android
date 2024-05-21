package com.jet.im.model.messages;

import com.jet.im.model.MessageContent;
import com.jet.im.internal.util.JLogger;

import org.json.JSONException;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class RecallInfoMessage extends MessageContent {
    public RecallInfoMessage() {
        mContentType = CONTENT_TYPE;
    }

    @Override
    public byte[] encode() {
        JSONObject jsonObject = new JSONObject();
        try {
            if (mExtra != null) {
                for (Map.Entry<String, String> entry : mExtra.entrySet()) {
                    jsonObject.put(entry.getKey(), entry.getValue());
                }
            }
        } catch (JSONException e) {
            JLogger.e("RecallInfoMessage JSONException " + e.getMessage());
        }
        return jsonObject.toString().getBytes(StandardCharsets.UTF_8);
    }

    @Override
    public void decode(byte[] data) {
        if (data == null) {
            JLogger.e("RecallInfoMessage decode data is null");
            return;
        }
        String jsonStr = new String(data, StandardCharsets.UTF_8);
        try {
            JSONObject jsonObject = new JSONObject(jsonStr);
            decodeExt(jsonObject);
        } catch (JSONException e) {
            JLogger.e("RecallInfoMessage decode JSONException " + e.getMessage());
        }
    }

    private void decodeExt(JSONObject jsonObject) {
        if (jsonObject == null) return;

        mExtra = new HashMap<>();
        for (Iterator<String> it = jsonObject.keys(); it.hasNext(); ) {
            try {
                String key = it.next();
                String value = jsonObject.getString(key);
                mExtra.put(key, value);
            } catch (JSONException e) {
                JLogger.e("RecallInfoMessage decodeExt JSONException " + e.getMessage());
            }
        }
    }

    public Map<String, String> getExtra() {
        return mExtra;
    }

    public void setExtra(Map<String, String> extra) {
        mExtra = extra;
    }

    public static final String CONTENT_TYPE = "jg:recallinfo";

    private Map<String, String> mExtra;

    private static final String EXTRA = "extra";

}
