package com.juggle.im.internal.util;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.juggle.im.JIM;
import com.juggle.im.internal.ConnectionManager;

public class NetworkChangeReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent == null || intent.getAction() == null) return;

        boolean networkAvailable = NetUtils.isNetworkAvailable(context);
        JLogger.d("Network-Change", "network available : " + networkAvailable);

        if (networkAvailable) {
            if (mListener != null) {
                mListener.onNetworkAvailable();
            }
        }
    }

    public NetworkChangeReceiver(INetworkChangeReceiverListener listener) {
        this.mListener = listener;
    }

    public interface INetworkChangeReceiverListener {
        void onNetworkAvailable();
    }

    private final INetworkChangeReceiverListener mListener;
}
