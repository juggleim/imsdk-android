package com.jet.im.internal;

import com.jet.im.internal.core.JetIMCore;
import com.jet.im.interfaces.IMessageManager;

public class MessageManager implements IMessageManager {

    public MessageManager(JetIMCore core) {
        this.mCore = core;
    }
    private JetIMCore mCore;
}
