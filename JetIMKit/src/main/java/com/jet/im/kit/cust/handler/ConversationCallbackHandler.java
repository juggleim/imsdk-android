package com.jet.im.kit.cust.handler;

import com.juggle.im.model.ConversationInfo;

import java.util.List;

public interface ConversationCallbackHandler {
    void onResult(List<ConversationInfo> result);
}
