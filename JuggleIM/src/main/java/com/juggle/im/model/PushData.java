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

    public String getTitle() {
        return mTitle;
    }

    public void setTitle(String title) {
        mTitle = title;
    }

    // Push title used for notification display.
    private String mTitle;
    // Push content used for notification display.
    private String mContent;
    // Extra field that can carry custom JSON.
    private String mExtra;
}
