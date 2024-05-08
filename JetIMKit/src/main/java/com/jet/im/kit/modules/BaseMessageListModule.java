package com.jet.im.kit.modules;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.jet.im.kit.activities.adapter.BaseMessageListAdapter;
import com.jet.im.kit.interfaces.LoadingDialogHandler;
import com.jet.im.kit.modules.components.BaseMessageListComponent;
import com.jet.im.kit.modules.components.MessageInputComponent;
import com.jet.im.kit.modules.components.StatusComponent;

abstract public class BaseMessageListModule<LC extends BaseMessageListComponent<? extends BaseMessageListAdapter>> extends BaseModule {
    @NonNull
    private LC messageListComponent;
    @NonNull
    private MessageInputComponent inputComponent;
    @NonNull
    private StatusComponent statusComponent;
    @Nullable
    private LoadingDialogHandler loadingDialogHandler;

    BaseMessageListModule(@NonNull Context context, @NonNull LC messageListComponent) {
        this.messageListComponent = messageListComponent;
        this.inputComponent = new MessageInputComponent();
        this.statusComponent = new StatusComponent();
    }

    /**
     * Sets the handler for the loading dialog.
     *
     * @param loadingDialogHandler Loading dialog handler to be used in this module
     * since 3.0.0
     */
    public void setOnLoadingDialogHandler(@Nullable LoadingDialogHandler loadingDialogHandler) {
        this.loadingDialogHandler = loadingDialogHandler;
    }

    /**
     * Returns the handler for loading dialog.
     *
     * @return Loading dialog handler to be used in this module
     * since 3.0.0
     */
    @Nullable
    public LoadingDialogHandler getLoadingDialogHandler() {
        return loadingDialogHandler;
    }

    /**
     * Sets a custom message list component.
     *
     * @param component The message list component to be used in this module
     * since 3.0.0
     */
    public void setMessageListComponent(@NonNull LC component) {
        this.messageListComponent = component;
    }

    /**
     * Sets a custom message input component.
     *
     * @param component The message input component to be used in this module
     * since 3.0.0
     */
    public <T extends MessageInputComponent> void setInputComponent(@NonNull T component) {
        this.inputComponent = component;
    }

    /**
     * Sets a custom status component.
     *
     * @param component The status component to be used in this module
     * since 3.0.0
     */
    public <T extends StatusComponent> void setStatusComponent(@NonNull T component) {
        this.statusComponent = component;
    }

    /**
     * Returns the message list component.
     *
     * @return The message list component of this module
     * since 3.0.0
     */
    @NonNull
    public LC getMessageListComponent() {
        return messageListComponent;
    }

    /**
     * Returns the message input component.
     *
     * @return The message input component of this module
     * since 3.0.0
     */
    @NonNull
    public MessageInputComponent getMessageInputComponent() {
        return inputComponent;
    }

    /**
     * Returns the status component.
     *
     * @return The status component of this module
     * since 3.0.0
     */
    @NonNull
    public StatusComponent getStatusComponent() {
        return statusComponent;
    }

    /**
     * It will be called when the loading dialog needs displaying.
     *
     * @return True if the callback has consumed the event, false otherwise.
     * since 3.0.0
     */
    public boolean shouldShowLoadingDialog() {
        if (loadingDialogHandler != null && loadingDialogHandler.shouldShowLoadingDialog()) {
            return true;
        }
        // Do nothing on the channel.
        return false;
    }

    /**
     * It will be called when the loading dialog needs dismissing.
     *
     * since 3.0.0
     */
    public void shouldDismissLoadingDialog() {
        if (loadingDialogHandler != null) {
            loadingDialogHandler.shouldDismissLoadingDialog();
        }
    }

    /**
     * Returns a collection of parameters applied to this module.
     *
     * @return {@link Params} applied to this module.
     * since 3.0.0
     */
    @NonNull
    abstract public Params getParams();
}

