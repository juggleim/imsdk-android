package com.juggle.im.model;

public class PushData {

    public String getContent() {
        return mContent;
    }

    public void setContent(String content) {
        mContent = content;
    }

    public String getExtra() {
        return mExtra;
    }

    public void setExtra(String extra) {
        mExtra = extra;
    }

    // 推送内容，用于通知栏的展示
    private String mContent;
    // 扩展字段，可以携带自定义 json
    private String mExtra;
}
