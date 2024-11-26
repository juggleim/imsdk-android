package com.juggle.im.internal.util.statemachine;

import android.os.Message;

import com.juggle.im.call.internal.CallEvent;
import com.juggle.im.internal.util.JLogger;

public class BaseState extends State {
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
    public String getName() {
        String name = "";
        String[] ss = super.getName().split("\\.");
        if (ss.length > 0) {
            name = ss[ss.length-1];
        }
        return name;
    }
}
