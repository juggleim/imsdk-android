package com.jet.im.kit.vm;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.sendbird.android.channel.OpenChannel;
import com.sendbird.android.handler.OpenChannelCallbackHandler;
import com.sendbird.android.params.OpenChannelCreateParams;
import com.jet.im.kit.interfaces.AuthenticateHandler;
import com.jet.im.kit.log.Logger;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * ViewModel preparing and managing data when creating a channel
 *
 * since 3.2.0
 */
public class CreateOpenChannelViewModel extends BaseViewModel {
    private final AtomicBoolean isCreatingChannel = new AtomicBoolean();

    /**
     * Tries to connect Sendbird Server and retrieve a channel instance.
     *
     * @param handler Callback notifying the result of authentication
     * since 3.2.0
     */
    @Override
    public void authenticate(@NonNull AuthenticateHandler handler) {
        connect((user, e) -> {
            if (user != null) {
                handler.onAuthenticated();
            } else {
                handler.onAuthenticationFailed();
            }
        });
    }

    @Override
    protected void onCleared() {
        super.onCleared();
    }

    /**
     * Creates a open channel.
     *
     * @param params Params to be applied when creating a channel
     * @param handler Callback notifying the result of authentication
     * since 3.2.0
     */
    public void createOpenChannel(@NonNull OpenChannelCreateParams params, @Nullable OpenChannelCallbackHandler handler) {
        Logger.dev("++ createOpenChannel isCreatingChannel : " + isCreatingChannel.get());
        if (isCreatingChannel.compareAndSet(false, true)) {
            OpenChannel.createChannel(params, (channel, e) -> {
                isCreatingChannel.set(false);
                if (handler != null) handler.onResult(channel, e);
            });
        }
    }
}
