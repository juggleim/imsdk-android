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
import com.jet.im.kit.interfaces.LoadingDialogHandler;
import com.jet.im.kit.internal.ui.widgets.WaitingDialog;
import com.jet.im.kit.modules.components.MessageSearchHeaderComponent;
import com.jet.im.kit.modules.components.MessageSearchListComponent;
import com.jet.im.kit.modules.components.StatusComponent;

/**
 * A module for searching for messages. This module is composed of a header, list, and status.
 * All composed components are created when the module is created. After than those components can replace.
 *
 * <ul>
 *  <li>Header component is {@link MessageSearchHeaderComponent} and you can set custom header component by {@link #setHeaderComponent(MessageSearchHeaderComponent)}.
 *  <li>List component is {@link MessageSearchListComponent} and you can set custom list component by {@link #setMessageListComponent(MessageSearchListComponent)}.
 *  <li>Status component is {@link StatusComponent} and you can set custom status component by {@link #setStatusComponent(StatusComponent)}
 * </ul>
 *
 * since 3.0.0
 */
public class MessageSearchModule extends BaseModule {
    @NonNull
    private final Params params;
    @NonNull
    private MessageSearchHeaderComponent headerComponent;
    @NonNull
    private MessageSearchListComponent messageListComponent;
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
    public MessageSearchModule(@NonNull Context context) {
        this(context, new Params(context));
    }

    /**
     * Constructor
     *
     * @param context The {@code Context} this module is currently associated with
     * @param params  The Parameter object that can customize a default Fragment.
     * since 3.0.0
     */
    public MessageSearchModule(@NonNull Context context, @NonNull Params params) {
        this.params = params;
        this.headerComponent = new MessageSearchHeaderComponent();
        this.messageListComponent = new MessageSearchListComponent();
        this.statusComponent = new StatusComponent();
    }

    @NonNull
    @Override
    public View onCreateView(@NonNull Context context, @NonNull LayoutInflater inflater, @Nullable Bundle args) {
        if (args != null) params.apply(context, args);
        final Context moduleContext = new ContextThemeWrapper(context, params.getTheme());

        final LinearLayout parent = new LinearLayout(context);
        parent.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        parent.setOrientation(LinearLayout.VERTICAL);

        final TypedValue values = new TypedValue();
        if (params.shouldUseSearchBar()) {
            moduleContext.getTheme().resolveAttribute(R.attr.sb_component_message_search_header, values, true);
            final Context headerThemeContext = new ContextThemeWrapper(moduleContext, values.resourceId);
            final LayoutInflater headerInflater = inflater.cloneInContext(headerThemeContext);
            final View header = this.headerComponent.onCreateView(headerThemeContext, headerInflater, parent, args);
            parent.addView(header);
        }

        final FrameLayout innerContainer = new FrameLayout(context);
        innerContainer.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        parent.addView(innerContainer);

        moduleContext.getTheme().resolveAttribute(R.attr.sb_component_list, values, true);
        final Context listThemeContext = new ContextThemeWrapper(moduleContext, values.resourceId);
        final LayoutInflater listInflater = inflater.cloneInContext(listThemeContext);
        final View messageListLayout = this.messageListComponent.onCreateView(listThemeContext, listInflater, innerContainer, args);
        innerContainer.addView(messageListLayout);

        moduleContext.getTheme().resolveAttribute(R.attr.sb_component_status, values, true);
        final Context statusThemeContext = new ContextThemeWrapper(moduleContext, values.resourceId);
        final LayoutInflater statusInflater = inflater.cloneInContext(statusThemeContext);
        final View statusLayout = statusComponent.onCreateView(statusThemeContext, statusInflater, innerContainer, args);
        innerContainer.addView(statusLayout);
        return parent;
    }

    /**
     * Sets the message search header component.
     *
     * @param component The message search header component to be used in this module
     * since 3.0.0
     */
    public <T extends MessageSearchHeaderComponent> void setHeaderComponent(@NonNull T component) {
        this.headerComponent = component;
    }

    /**
     * Sets the list of the message search result component.
     *
     * @param component The list of the message search result component to be used in this module
     * since 3.0.0
     */
    public <T extends MessageSearchListComponent> void setMessageListComponent(@NonNull T component) {
        this.messageListComponent = component;
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
     * Returns the message search header component.
     *
     * @return The message search header component of this module
     * since 3.0.0
     */
    @NonNull
    public MessageSearchHeaderComponent getHeaderComponent() {
        return headerComponent;
    }

    /**
     * Returns the list of the message search result component.
     *
     * @return The list of the message search result component of this module
     * since 3.0.0
     */
    @NonNull
    public MessageSearchListComponent getMessageListComponent() {
        return messageListComponent;
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
     * Returns a collection of parameters, which can be applied to a fragment.
     *
     * @return Collection of parameters, which can be applied to a fragment.
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
     * It will be called when the loading dialog needs displaying.
     *
     * @return True if the callback has consumed the event, false otherwise.
     * since 3.0.0
     */
    public boolean shouldShowLoadingDialog(@NonNull Context context) {
        if (loadingDialogHandler != null && loadingDialogHandler.shouldShowLoadingDialog()) {
            return true;
        }

        WaitingDialog.show(context);
        return true;
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
     * It will be called when the loading dialog needs dismissing.
     *
     * since 3.0.0
     */
    public void shouldDismissLoadingDialog() {
        if (loadingDialogHandler != null) {
            loadingDialogHandler.shouldDismissLoadingDialog();
            return;
        }
        WaitingDialog.dismiss();
    }

    public static class Params extends BaseModule.Params {
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
            super(context, themeMode, R.attr.sb_module_message_search);
        }

        /**
         * Constructor
         *
         * @param context    The {@code Context} this module is currently associated with
         * @param themeResId The theme resource ID to be applied to this module
         * since 3.0.0
         */
        public Params(@NonNull Context context, @StyleRes int themeResId) {
            super(context, themeResId, R.attr.sb_module_message_search);
        }

        /**
         * Sets whether the message search header is used. The default is <code>false</code>.
         *
         * @param useSearchBar <code>true</code> if the message search header is used, <code>false</code> otherwise
         * since 3.0.0
         */
        public void setUseSearchBar(boolean useSearchBar) {
            setUseHeader(useSearchBar);
        }

        /**
         * Returns whether the message search header is used.
         *
         * @return <code>true</code> if the message search header is used, <code>false</code> otherwise
         * since 3.0.0
         */
        public boolean shouldUseSearchBar() {
            return shouldUseHeader();
        }
    }
}
