package com.jet.im.internal;

import android.text.TextUtils;

import com.jet.im.model.MessageContent;
import com.jet.im.utils.LoggerUtils;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.ConcurrentHashMap;

public class ContentTypeCenter {
    public static ContentTypeCenter getInstance() {
        return SingletonHolder.sInstance;
    }

    private static class SingletonHolder {
        static final ContentTypeCenter sInstance = new ContentTypeCenter();
    }

    public void registerContentType(Class<? extends MessageContent> cls) {
        try {
            Constructor<? extends MessageContent> constructor = cls.getDeclaredConstructor();
            MessageContent content = constructor.newInstance();
            String type = content.getContentType();
            if (TextUtils.isEmpty(type)) {
                LoggerUtils.e("registerContentType type is empty when class is " + cls);
                return;
            }
            mContentTypeMap.put(type, cls);
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException |
                 InstantiationException e) {
            LoggerUtils.e("registerContentType exception, msg is " + e.getMessage());
        }
    }

    public MessageContent getContent(byte[] data, String type) {
        Class<? extends MessageContent> cls = mContentTypeMap.get(type);
        if (cls == null) {
            return null;
        }
        MessageContent content = null;
        try {
            Constructor<? extends MessageContent> constructor = cls.getDeclaredConstructor();
            content = constructor.newInstance();
            content.decode(data);
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException |
                 InstantiationException e) {
            LoggerUtils.e("getMessageContent exception, msg is " + e.getMessage());
        }
        return content;
    }

    private final ConcurrentHashMap<String, Class<? extends MessageContent>> mContentTypeMap = new ConcurrentHashMap<>();
}
