package com.juggle.im.call.internal.fsm;

import android.os.Message;

import com.juggle.im.call.internal.CallEvent;
import com.juggle.im.call.internal.CallSessionImpl;
import com.juggle.im.internal.util.JLogger;
import com.juggle.im.internal.util.statemachine.State;

import java.lang.ref.WeakReference;

public class CallState extends State {
    @Override
    public void enter() {
        String name = "";
        String[] ss = getName().split("\\.");
        if (ss.length > 0) {
            name = ss[ss.length-1];
        }
        JLogger.i("FSM-Sm", "enter state [" + name + "]");
    }

    @Override
    public void exit() {
        JLogger.i("FSM-Sm", "leave state [" + getName() + "]");
    }

    @Override
    public boolean processMessage(Message msg) {
        JLogger.i("FSM-Sm", "[" + getName() + "] processMessage : " + CallEvent.nameOfEvent(msg.what));
        return super.processMessage(msg);
    }

    @Override
    public String getName() {
        String name = "";
        String[] ss = super.getName().split("\\.");
        if (ss.length > 0) {
            name = ss[ss.length-1];
        }
        return name;
    }

    public CallSessionImpl getCallSessionImpl() {
        return mWeakCallSessionImpl.get();
    }

    public void setCallSessionImpl(CallSessionImpl callSessionImpl) {
        mWeakCallSessionImpl = new WeakReference<>(callSessionImpl);
    }

    protected WeakReference<CallSessionImpl> mWeakCallSessionImpl;
}
