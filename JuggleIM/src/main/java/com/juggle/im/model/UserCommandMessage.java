package com.juggle.im.model;

public abstract class UserCommandMessage extends MessageContent {
    @Override
    public int getFlags() {
        return MessageFlag.IS_USER_CMD.getValue() | MessageFlag.IS_CMD.getValue();
    }
}
