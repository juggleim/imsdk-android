package com.jet.im.kit.consts;

import androidx.annotation.NonNull;

/**
 * Represents type of message group UI.
 */
public enum MessageGroupType {
    /**
     * A type of message that exists singly without a group.
     */
    GROUPING_TYPE_SINGLE(0),
    /**
     * The type of message at the top of the group.
     */
    GROUPING_TYPE_HEAD(1),
    /**
     * The type of message located in the middle of the group.
     */
    GROUPING_TYPE_BODY(2),
    /**
     * The type of message at the bottom of the group.
     */
    GROUPING_TYPE_TAIL(3);

    int value;

    MessageGroupType(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    @NonNull
    public static MessageGroupType from(int value) {
        for (MessageGroupType type : values()) {
            if (type.value == value) {
                return type;
            }
        }
        return GROUPING_TYPE_SINGLE;
    }
}
