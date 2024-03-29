package com.jet.im.push.google;

import com.google.firebase.messaging.RemoteMessage;
import com.jet.im.push.PushType;

import org.json.JSONException;
import org.json.JSONObject;

public class FirebaseMessagingService extends com.google.firebase.messaging.FirebaseMessagingService {
    private static final String TAG = "RongFirebaseMessagingService";

    /**
     * Called when message is received.
     *
     * @param remoteMessage Object representing the message received from Firebase Cloud Messaging.
     */
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        // There are two types of messages data messages and notification messages. Data messages
        // are handled
        // here in onMessageReceived whether the app is in the foreground or background. Data
        // messages are the type
        // traditionally used with GCM. Notification messages are only received here in
        // onMessageReceived when the app
        // is in the foreground. When the app is in the background an automatically generated
        // notification is displayed.
        // When the user taps on the notification they are returned to the app. Messages containing
        // both notification
        // and data payloads are treated as notification messages. The Firebase console always sends
        // notification
        // messages. For more see: https://firebase.google.com/docs/cloud-messaging/concept-options
        //todo 透传方式
        if (null == remoteMessage) return;
        try {
            JSONObject json = new JSONObject(remoteMessage.getData());
            String message = json.getString("message");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onNewToken(String s) {
        super.onNewToken(s);
        if (GooglePush.sCallback != null) {
            GooglePush.sCallback.onReceivedToken(PushType.GOOGLE, s);
        }
    }
}
