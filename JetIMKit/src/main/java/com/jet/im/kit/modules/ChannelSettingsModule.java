package com.jet.im.kit.modules;

import android.content.Context;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StyleRes;
import androidx.appcompat.view.ContextThemeWrapper;

import com.jet.im.kit.R;
import com.jet.im.kit.SendbirdUIKit;
import com.jet.im.kit.interfaces.LoadingDialogHandler;
import com.jet.im.kit.internal.ui.widgets.WaitingDialog;
import com.jet.im.kit.modules.components.ChannelSettingsHeaderComponent;
import com.jet.im.kit.modules.components.ChannelSettingsInfoComponent;
import com.jet.im.kit.modules.components.ChannelSettingsMenuComponent;

/**
 * A module for channel settings. This module is composed of a header, channel information and settings.
 * All composed components are created when the module is created. After than those components can replace.
 *
 * <ul>
 *  <li>Header component is {@link ChannelSettingsHeaderComponent} and you can set custom header component by {@link #setHeaderComponent(ChannelSettingsHeaderComponent)}
 *  <li>Info component is {@link ChannelSettingsInfoComponent} and you can set custom info component by {@link #setChannelSettingsInfoComponent(ChannelSettingsInfoComponent)}
 *  <li>Menu component is {@link ChannelSettingsMenuComponent} and you can set custom menu component by {@link #setChannelSettingsMenuComponent(ChannelSettingsMenuComponent)}
 * </ul>
 *
 * since 3.0.0
 */
public class ChannelSettingsModule extends BaseModule {
    @NonNull
    private final Params params;
    @NonNull
    private ChannelSettingsHeaderComponent headerComponent;
    @NonNull
    private ChannelSettingsInfoComponent infoComponent;
    @NonNull
    private ChannelSettingsMenuComponent menuComponent;

    @Nullable
    private LoadingDialogHandler loadingDialogHandler;

    /**
     * Constructor
     *
     * @param context The {@code Context} this module is currently associated with
     * since 3.0.0
     */
    public ChannelSettingsModule(@NonNull Context context) {
        this(context, new Params(context));
    }

    /**
     * Constructor
     *
     * @param context The {@code Context} this module is currently associated with
     * @param params The Parameter object that can customize a default Fragment.
     * since 3.0.0
     */
    public ChannelSettingsModule(@NonNull Context context, @NonNull Params params) {
        this.params = params;
        this.headerComponent = new ChannelSettingsHeaderComponent();
        this.infoComponent = new ChannelSettingsInfoComponent();
        this.menuComponent = new ChannelSettingsMenuComponent();
    }

    @Override
    @NonNull
    public View onCreateView(@NonNull Context context, @NonNull LayoutInflater inflater, @Nullable Bundle args) {
        if (args != null) params.apply(context, args);
        final Context moduleContext = new ContextThemeWrapper(context, params.getTheme());

        final LinearLayout parent = new LinearLayout(context);
        parent.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        parent.setOrientation(LinearLayout.VERTICAL);
        parent.setBackgroundResource(SendbirdUIKit.isDarkMode() ? R.color.background_600 : R.color.background_50);

        final TypedValue values = new TypedValue();
        if (params.shouldUseHeader()) {
            moduleContext.getTheme().resolveAttribute(R.attr.sb_component_state_header, values, true);
            final Context headerThemeContext = new ContextThemeWrapper(moduleContext, values.resourceId);
            final LayoutInflater headerInflater = inflater.cloneInContext(headerThemeContext);
            final View header = this.headerComponent.onCreateView(headerThemeContext, headerInflater, parent, args);
            parent.addView(header);
        }

        final ScrollView scrollView = new ScrollView(context);
        scrollView.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        scrollView.setFillViewport(true);

        final LinearLayout innerContainer = new LinearLayout(context);
        innerContainer.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        innerContainer.setOrientation(LinearLayout.VERTICAL);

        moduleContext.getTheme().resolveAttribute(R.attr.sb_component_channel_settings_info, values, true);
        final Context infoThemeContext = new ContextThemeWrapper(moduleContext, values.resourceId);
        final LayoutInflater infoInflater = inflater.cloneInContext(infoThemeContext);
        final View infoLayout = this.infoComponent.onCreateView(infoThemeContext, infoInflater, innerContainer, args);
        innerContainer.addView(infoLayout);

        moduleContext.getTheme().resolveAttribute(R.attr.sb_component_channel_settings_menu, values, true);
        final Context menuThemeContext = new ContextThemeWrapper(moduleContext, values.resourceId);
        final LayoutInflater menuInflater = inflater.cloneInContext(menuThemeContext);
        final View menuLayout = this.menuComponent.onCreateView(menuThemeContext, menuInflater, innerContainer, args);
        innerContainer.addView(menuLayout);

        scrollView.addView(innerContainer);
        parent.addView(scrollView);
        return parent;
    }

    /**
     * Sets a custom header component.
     *
     * @param component The header component to be used in this module
     * since 3.0.0
     */
    public <T extends ChannelSettingsHeaderComponent> void setHeaderComponent(@NonNull T component) {
        this.headerComponent = component;
    }

    /**
     * Sets the settings menu component.
     *
     * @param component the settings menu component to be used in this module
     * since 3.0.0
     */
    public <T extends ChannelSettingsMenuComponent> void setChannelSettingsMenuComponent(@NonNull T component) {
        this.menuComponent = component;
    }

    /**
     * Sets the channel information component.
     *
     * @param component the channel information component to be used in this module
     * since 3.0.0
     */
    public <T extends ChannelSettingsInfoComponent> void setChannelSettingsInfoComponent(@NonNull T component) {
        this.infoComponent = component;
    }

    /**
     * Returns the header component.
     *
     * @return The header component of this module.
     * since 3.0.0
     */
    @NonNull
    public ChannelSettingsHeaderComponent getHeaderComponent() {
        return headerComponent;
    }

    /**
     * Returns the settings menu component.
     *
     * @return The settings menu component of this module
     * since 3.0.0
     */
    @NonNull
    public ChannelSettingsMenuComponent getChannelSettingsMenuComponent() {
        return menuComponent;
    }

    /**
     * Returns the channel information component.
     *
     * @return The channel information component of this module
     * since 3.0.0
     */
    @NonNull
    public ChannelSettingsInfoComponent getChannelSettingsInfoComponent() {
        return infoComponent;
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
    public boolean shouldShowLoadingDialog(@NonNull Context context) {
        if (loadingDialogHandler != null && loadingDialogHandler.shouldShowLoadingDialog()) {
            return true;
        }
        WaitingDialog.show(context);
        return true;
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
         * @param context The {@code Context} this module is currently associated with
         * @param themeMode The theme of Sendbird UIKit to be applied to this module
         * since 3.0.0
         */
        public Params(@NonNull Context context, @NonNull SendbirdUIKit.ThemeMode themeMode) {
            super(context, themeMode, R.attr.sb_module_channel_settings);
        }

        /**
         * Constructor
         *
         * @param context The {@code Context} this module is currently associated with
         * @param themeResId The theme resource ID to be applied to this module
         * since 3.0.0
         */
        public Params(@NonNull Context context, @StyleRes int themeResId) {
            super(context, themeResId, R.attr.sb_module_channel_settings);
        }
    }
}
