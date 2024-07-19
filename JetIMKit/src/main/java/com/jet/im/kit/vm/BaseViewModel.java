package com.jet.im.kit.vm;

import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;
import androidx.lifecycle.ViewModel;

import com.jet.im.kit.interfaces.AuthenticateHandler;
import com.jet.im.kit.interfaces.ConnectHandler;
import com.jet.im.kit.internal.contracts.SendbirdUIKitImpl;
import com.jet.im.kit.internal.contracts.SendbirdUIKitContract;
import com.jet.im.kit.log.Logger;

/**
 * ViewModel preparing and managing data commonly used in UIKit's Activities or Fragments.
 *
 * since 3.0.0
 */
public abstract class BaseViewModel extends ViewModel {
    @NonNull
    protected final SendbirdUIKitContract sendbirdUIKit;

    protected BaseViewModel() {
        this(new SendbirdUIKitImpl());
    }

    @VisibleForTesting
    BaseViewModel(@NonNull SendbirdUIKitContract sendbirdUIKit) {
        this.sendbirdUIKit = sendbirdUIKit;
    }

    void connect(@NonNull ConnectHandler handler) {
        Logger.dev(">> BaseViewModel::connect()");
        sendbirdUIKit.connect(handler);
    }

    /**
     * Authenticates before using ViewModels data.
     *
     * @param handler Callback notifying the result of authentication
     * since 3.0.0
     */
    abstract public void authenticate(@NonNull AuthenticateHandler handler);
}
