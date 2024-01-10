package com.jet.im.internal;

import com.jet.im.core.JetIMCore;
import com.jet.im.interfaces.IConversationManager;

public class ConversationManager implements IConversationManager {

    public ConversationManager(JetIMCore core) {
        this.mCore = core;
    }

    private JetIMCore mCore;
}
