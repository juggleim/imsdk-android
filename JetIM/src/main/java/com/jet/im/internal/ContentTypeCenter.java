package com.jet.im.internal;

import android.text.TextUtils;

import com.jet.im.internal.util.JLogger;
import com.jet.im.model.MessageContent;

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
                JLogger.e("MSG-Register", "registerContentType error, type is empty when class is " + cls);
                return;
            }
            mContentTypeMap.put(type, cls);
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException |
                 InstantiationException e) {
            JLogger.e("MSG-Register", "registerContentType error, class is " + cls + ", exception is " + e.getMessage());
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
            JLogger.e("MSG-Register", "getContent error, type is " + type + ", exception is " + e.getMessage());
        }
        return content;
    }

    public int flagsWithType(String type) {
        Class<? extends MessageContent> cls = mContentTypeMap.get(type);
        if (cls == null) {
            return -1;
        }
        try {
            Constructor<? extends MessageContent> constructor = cls.getDeclaredConstructor();
            MessageContent content = constructor.newInstance();
            return content.getFlags();
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException |
                 InstantiationException e) {
            JLogger.e("MSG-Register", "flagsWithType error, type is " + type + ", exception is " + e.getMessage());
        }
        return -1;
    }

    private final ConcurrentHashMap<String, Class<? extends MessageContent>> mContentTypeMap = new ConcurrentHashMap<>();
}
