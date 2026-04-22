package com.juggle.im.model;

public class ConversationTagInfo {
    public enum TagType {
        USER(0),
        SYSTEM(1),
        GLOBAL(2);

        TagType(int value) {
            this.mValue = value;
        }

        public int getValue() {
            return mValue;
        }

        public static TagType setValue(int value) {
            for (TagType t : TagType.values()) {
                if (value == t.mValue) {
                    return t;
                }
            }
            return USER;
        }

        private final int mValue;
    }

    public TagType getType() {
        return mType;
    }

    public void setType(TagType type) {
        mType = type;
    }

    public String getName() {
        return mName;
    }

    public void setName(String name) {
        mName = name;
    }

    public String getTagId() {
        return mTagId;
    }

    public void setTagId(String tagId) {
        mTagId = tagId;
    }

    private String mTagId;
    private String mName;
    private TagType mType;
}
