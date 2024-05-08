package com.jet.im.kit.modules;

import android.content.Context;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StyleRes;
import androidx.appcompat.view.ContextThemeWrapper;

import com.jet.im.kit.R;
import com.jet.im.kit.SendbirdUIKit;
import com.jet.im.kit.consts.StringSet;
import com.jet.im.kit.interfaces.LoadingDialogHandler;
import com.jet.im.kit.modules.components.OpenChannelHeaderComponent;
import com.jet.im.kit.modules.components.OpenChannelMessageInputComponent;
import com.jet.im.kit.modules.components.OpenChannelMessageListComponent;
import com.jet.im.kit.modules.components.StatusComponent;
import com.jet.im.kit.widgets.MessageInputView;

/**
 * A module for the open channel. This module is composed of a header, list, input, and status.
 * All composed components are created when the module is created. After than those components can replace.
 *
 * <ul>
 *  <li>Header component is {@link OpenChannelHeaderComponent} and you can set custom header component by {@link #setHeaderComponent(OpenChannelHeaderComponent)}
 *  <li>List component is {@link OpenChannelMessageListComponent} and you can set custom list component by {@link #setMessageListComponent(OpenChannelMessageListComponent)}
 *  <li>Input component is {@link OpenChannelMessageInputComponent} and you can set custom input component by {@link #setInputComponent(OpenChannelMessageInputComponent)}
 *  <li>Status component is {@link StatusComponent} and you can set custom status component by {@link #setStatusComponent(StatusComponent)}
 * </ul>
 *
 * since 3.0.0
 */
public class OpenChannelModule extends BaseModule {
    @NonNull
    private final Params params;
    @NonNull
    private OpenChannelHeaderComponent headerComponent;
    @NonNull
    private OpenChannelMessageListComponent messageListComponent;
    @NonNull
    private OpenChannelMessageInputComponent inputComponent;
    @NonNull
    private StatusComponent statusComponent;
    @Nullable
    private LoadingDialogHandler loadingDialogHandler;

    /**
     * Constructor
     *
     * @param context The {@code Context} this module is currently associated with
     * since 3.0.0
     */
    public OpenChannelModule(@NonNull Context context) {
        this(context, new Params(context));
    }

    /**
     * Constructor
     *
     * @param context The {@code Context} this module is currently associated with
     * @param params  The Parameter object that can customize a default Fragment.
     * since 3.0.0
     */
    public OpenChannelModule(@NonNull Context context, @NonNull Params params) {
        this.params = params;
        this.headerComponent = new OpenChannelHeaderComponent();
        this.messageListComponent = new OpenChannelMessageListComponent();
        this.inputComponent = new OpenChannelMessageInputComponent();
        this.statusComponent = new StatusComponent();
    }

    @Override
    @NonNull
    public View onCreateView(@NonNull Context context, @NonNull LayoutInflater inflater, @Nullable Bundle args) {
        if (args != null) params.apply(context, args);
        int themeResId = params.useOverlayMode ? R.style.Module_Overlay_OpenChannel : params.getTheme();
        final Context moduleContext = new ContextThemeWrapper(context, themeResId);
        final LinearLayout parent = new LinearLayout(context);
        parent.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        parent.setOrientation(LinearLayout.VERTICAL);

        if (params.useOverlayMode) {
            parent.setBackgroundResource(R.color.onlight_03);
        }

        final TypedValue values = new TypedValue();
        if (params.shouldUseHeader()) {
            moduleContext.getTheme().resolveAttribute(R.attr.sb_component_header, values, true);
            final Context headerThemeContext = new ContextThemeWrapper(moduleContext, values.resourceId);
            final LayoutInflater headerInflater = inflater.cloneInContext(headerThemeContext);
            final View header = this.headerComponent.onCreateView(headerThemeContext, headerInflater, parent, args);
            parent.addView(header);
        }

        final FrameLayout bodyContainer = new FrameLayout(context);
        bodyContainer.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT, 1.0f));
        parent.addView(bodyContainer);

        moduleContext.getTheme().resolveAttribute(R.attr.sb_component_list, values, true);
        final Context listThemeContext = new ContextThemeWrapper(moduleContext, values.resourceId);
        final LayoutInflater listInflater = inflater.cloneInContext(listThemeContext);
        final View messageListLayout = messageListComponent.onCreateView(listThemeContext, listInflater, bodyContainer, args);
        bodyContainer.addView(messageListLayout);

        moduleContext.getTheme().resolveAttribute(R.attr.sb_component_status, values, true);
        final Context statusThemeContext = new ContextThemeWrapper(moduleContext, values.resourceId);
        final LayoutInflater statusInflater = inflater.cloneInContext(statusThemeContext);
        final View statusLayout = statusComponent.onCreateView(statusThemeContext, statusInflater, bodyContainer, args);
        bodyContainer.addView(statusLayout);

        moduleContext.getTheme().resolveAttribute(R.attr.sb_component_open_channel_message_input, values, true);
        final Context inputThemeContext = new ContextThemeWrapper(moduleContext, values.resourceId);
        final LayoutInflater inputInflater = inflater.cloneInContext(inputThemeContext);
        final View inputLayout = inputComponent.onCreateView(inputThemeContext, inputInflater, parent, args);
        if (inputComponent.getRootView() instanceof MessageInputView) {
            ((MessageInputView) inputComponent.getRootView()).setUseOverlay(params.useOverlayMode);
        }
        parent.addView(inputLayout);
        return parent;
    }

    /**
     * Sets the channel header component for {@code OpenChannel}.
     *
     * @param component The channel header component to be used in this module
     * since 3.0.0
     */
    public <T extends OpenChannelHeaderComponent> void setHeaderComponent(@NonNull T component) {
        this.headerComponent = component;
    }

    /**
     * Sets the message list component.
     *
     * @param component The message list component to be used in this module
     * since 3.0.0
     */
    public <T extends OpenChannelMessageListComponent> void setMessageListComponent(@NonNull T component) {
        this.messageListComponent = component;
    }

    /**
     * Sets the message input component.
     *
     * @param component The message input component to be used in this module
     * since 3.0.0
     */
    public <T extends OpenChannelMessageInputComponent> void setInputComponent(@NonNull T component) {
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
     * Returns the channel header component for {@code OpenChannel}.
     *
     * @return The header component of this module
     * since 3.0.0
     */
    @NonNull
    public OpenChannelHeaderComponent getHeaderComponent() {
        return headerComponent;
    }

    /**
     * Returns the message list component.
     *
     * @return The message list component of this module
     * since 3.0.0
     */
    @NonNull
    public OpenChannelMessageListComponent getMessageListComponent() {
        return messageListComponent;
    }

    /**
     * Returns the message input component.
     *
     * @return The message input component of this module
     * since 3.0.0
     */
    @NonNull
    public OpenChannelMessageInputComponent getMessageInputComponent() {
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
     * Returns a collection of parameters applied to this module.
     *
     * @return {@link Params} applied to this module.
     * since 3.0.0
     */
    @NonNull
    public Params getParams() {
        return params;
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

    public static class Params extends BaseModule.Params {
        private boolean useOverlayMode = false;

        /**
         * Constructor
         *
         * @param context The {@code Context} this module is currently associated with
         * since 3.0.0
         */
        public Params(@NonNull Context context) {
            this(context, SendbirdUIKit.getDefaultThemeMode());
        }

        /**
         * Constructor
         *
         * @param context   The {@code Context} this module is currently associated with
         * @param themeMode The theme of Sendbird UIKit to be applied to this module
         * since 3.0.0
         */
        public Params(@NonNull Context context, @NonNull SendbirdUIKit.ThemeMode themeMode) {
            super(context, themeMode, R.attr.sb_module_open_channel);
        }

        /**
         * Constructor
         *
         * @param context    The {@code Context} this module is currently associated with
         * @param themeResId The theme resource ID to be applied to this module
         * since 3.0.0
         */
        public Params(@NonNull Context context, @StyleRes int themeResId) {
            super(context, themeResId, R.attr.sb_module_open_channel);
        }

        /**
         * Sets whether use the channel displays as a overlay mode.
         *
         * @param useOverlayMode <code>true</code> if a overlay mode. is shown, <code>false</code> otherwise
         */
        public void setUseOverlayMode(boolean useOverlayMode) {
            this.useOverlayMode = useOverlayMode;
        }

        /**
         * Returns whether use the channel displays as a overlay mode.
         *
         * @return <code>true</code> if a overlay mode. is shown, <code>false</code> otherwise
         */
        public boolean shouldUseOverlayMode() {
            return useOverlayMode;
        }

        /**
         * Apply data that matches keys mapped to Params' properties.
         * {@code KEY_USE_HEADER} is mapped to {@link #setUseHeader(boolean)}.
         * {@code KEY_USE_OVERLAY_MODE} is mapped to {@link #setUseOverlayMode(boolean)}
         *
         * @param context The {@code Context} this component is currently associated with
         * @param args    The sets of arguments to apply at Params.
         * @return This Params object that applied with given data.
         * since 3.0.0
         */
        @NonNull
        protected Params apply(@NonNull Context context, @NonNull Bundle args) {
            super.apply(context, args);
            if (args.containsKey(StringSet.KEY_USE_OVERLAY_MODE)) {
                setUseOverlayMode(args.getBoolean(StringSet.KEY_USE_OVERLAY_MODE));
            }
            return this;
        }
    }
}
