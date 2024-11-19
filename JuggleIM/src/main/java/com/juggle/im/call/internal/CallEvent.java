package com.juggle.im.call.internal;

import java.util.HashMap;
import java.util.Map;

public class CallEvent {
    public final static int INVITE = 101;
    public final static int ACCEPT = 102;
    public final static int HANGUP= 103;
    public final static int ACCEPT_AFTER_HANGUP_OTHER = 104;

    public final static int INVITE_FAIL = 201;
    public final static int INVITE_TIMEOUT= 202;
    public final static int ACCEPT_DONE= 203;
    public final static int ACCEPT_FAIL = 204;
    public final static int INCOMING_TIMEOUT = 205;

    public final static int RECEIVE_INVITE = 301;
    public final static int RECEIVE_ACCEPT = 302;
    public final static int RECEIVE_HANGUP = 303;
    public final static int ROOM_DESTROY = 304;

    public final static int JOIN_CHANNEL_DONE = 401;
    public final static int JOIN_CHANNEL_FAIL = 402;

    public final static int PARTICIPANT_JOIN_CHANNEL = 501;
    public final static int PARTICIPANT_LEAVE_CHANNEL = 502;
    public final static int PARTICIPANT_ENABLE_CAMERA = 503;
    public final static int PARTICIPANT_ENABLE_MIC = 504;

    public static String nameOfEvent(int event) {
        return sEventNameMap.get(event);
    }

    private static final Map<Integer, String> sEventNameMap = new HashMap<Integer, String>() {{
       put(INVITE, "invite");
       put(ACCEPT, "accept");
       put(HANGUP, "hangup");
       put(ACCEPT_AFTER_HANGUP_OTHER, "accept after hangup other");

       put(INVITE_FAIL, "invite fail");
       put(INVITE_TIMEOUT, "invite timeout");
       put(ACCEPT_DONE, "accept done");
       put(ACCEPT_FAIL, "accept fail");
       put(INCOMING_TIMEOUT, "incoming timeout");

       put(RECEIVE_INVITE, "receive invite");
       put(RECEIVE_ACCEPT, "receive accept");
       put(RECEIVE_HANGUP, "receive hangup");
       put(ROOM_DESTROY, "room destroy");

       put(JOIN_CHANNEL_DONE, "join channel done");
       put(JOIN_CHANNEL_FAIL, "join channel fail");

       put(PARTICIPANT_JOIN_CHANNEL, "participant join channel");
       put(PARTICIPANT_LEAVE_CHANNEL, "participant leave channel");
       put(PARTICIPANT_ENABLE_CAMERA, "participant enable camera");
       put(PARTICIPANT_ENABLE_MIC, "participant enable mic");

    }};
}
