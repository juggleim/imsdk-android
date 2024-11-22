package com.juggle.im.call.internal.fsm;

import android.os.Message;

import com.juggle.im.call.internal.CallEvent;
import com.juggle.im.call.internal.CallSessionImpl;
import com.juggle.im.internal.util.JLogger;
import com.juggle.im.internal.util.statemachine.BaseState;

import java.lang.ref.WeakReference;

public class CallState extends BaseState {
    @Override
    public boolean processMessage(Message msg) {
        JLogger.i("FSM-Sm", "[" + getName() + "] processMessage : " + CallEvent.nameOfEvent(msg.what));
        return super.processMessage(msg);
    }

    public CallSessionImpl getCallSessionImpl() {
        return mWeakCallSessionImpl.get();
    }

    public void setCallSessionImpl(CallSessionImpl callSessionImpl) {
        mWeakCallSessionImpl = new WeakReference<>(callSessionImpl);
    }

    protected WeakReference<CallSessionImpl> mWeakCallSessionImpl;
}
