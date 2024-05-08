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
import com.jet.im.kit.modules.components.HeaderComponent;
import com.jet.im.kit.modules.components.OpenChannelModerationListComponent;

/**
 * A module for open channel moderation.
 * All composed components are created when the module is created. After than those components can replace.
 *
 * since 3.1.0
 */
public class OpenChannelModerationModule extends BaseModule {
    @NonNull
    private final Params params;
    @NonNull
    private HeaderComponent headerComponent;
    @NonNull
    private OpenChannelModerationListComponent moderationListComponent;

    @Nullable
    private LoadingDialogHandler loadingDialogHandler;

    /**
     * Constructor
     *
     * @param context The {@code Context} this module is currently associated with
     * since 3.1.0
     */
    public OpenChannelModerationModule(@NonNull Context context) {
        this(context, new Params(context));
    }

    /**
     * Constructor
     *
     * @param context The {@code Context} this module is currently associated with
     * @param params  The Parameter object that can customize a default Fragment.
     * since 3.1.0
     */
    public OpenChannelModerationModule(@NonNull Context context, @NonNull Params params) {
        this.params = params;
        this.headerComponent = new HeaderComponent();
        this.headerComponent.getParams().setUseRightButton(false);
        this.moderationListComponent = new OpenChannelModerationListComponent();
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
            moduleContext.getTheme().resolveAttribute(R.attr.sb_component_header, values, true);
            final Context headerThemeContext = new ContextThemeWrapper(moduleContext, values.resourceId);
            final LayoutInflater headerInflater = inflater.cloneInContext(headerThemeContext);
            final View header = this.headerComponent.onCreateView(headerThemeContext, headerInflater, parent, args);
            parent.addView(header);
        }

        final FrameLayout innerContainer = new FrameLayout(context);
        innerContainer.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        parent.addView(innerContainer);

        moduleContext.getTheme().resolveAttribute(R.attr.sb_component_moderation_list, values, true);
        final Context listThemeContext = new ContextThemeWrapper(moduleContext, values.resourceId);
        final LayoutInflater listInflater = inflater.cloneInContext(listThemeContext);
        final View moderationLayout = this.moderationListComponent.onCreateView(listThemeContext, listInflater, innerContainer, args);
        innerContainer.addView(moderationLayout);
        return parent;
    }

    /**
     * Sets a custom header component.
     *
     * @param component The header component to be used in this module
     * since 3.1.0
     */
    public <T extends HeaderComponent> void setHeaderComponent(@NonNull T component) {
        this.headerComponent = component;
    }

    /**
     * Sets a custom list component.
     *
     * @param component The list component to be used in this module
     * since 3.1.0
     */
    public <T extends OpenChannelModerationListComponent> void setModerationListComponent(@NonNull T component) {
        this.moderationListComponent = component;
    }

    /**
     * Returns the header component.
     *
     * @return The header component of this module
     * since 3.1.0
     */
    @NonNull
    public HeaderComponent getHeaderComponent() {
        return headerComponent;
    }

    /**
     * Returns the list component.
     *
     * @return The list component of this module
     * since 3.1.0
     */
    @NonNull
    public OpenChannelModerationListComponent getModerationListComponent() {
        return moderationListComponent;
    }

    /**
     * Sets the handler for the loading dialog.
     *
     * @param loadingDialogHandler Loading dialog handler to be used in this module
     * since 3.1.0
     */
    public void setOnLoadingDialogHandler(@Nullable LoadingDialogHandler loadingDialogHandler) {
        this.loadingDialogHandler = loadingDialogHandler;
    }

    /**
     * Returns the handler for loading dialog.
     *
     * @return Loading dialog handler to be used in this module
     * since 3.1.0
     */
    @Nullable
    public LoadingDialogHandler getLoadingDialogHandler() {
        return loadingDialogHandler;
    }

    /**
     * It will be called when the loading dialog needs displaying.
     *
     * @return True if the callback has consumed the event, false otherwise.
     * since 3.1.0
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
     * since 3.1.0
     */
    public void shouldDismissLoadingDialog() {
        if (loadingDialogHandler != null) {
            loadingDialogHandler.shouldDismissLoadingDialog();
            return;
        }
        WaitingDialog.dismiss();
    }

    /**
     * Returns a collection of parameters applied to this module.
     *
     * @return {@link Params} applied to this module.
     * since 3.1.0
     */
    @NonNull
    public Params getParams() {
        return params;
    }

    public static class Params extends BaseModule.Params {
        /**
         * Constructor
         *
         * @param context The {@code Context} this module is currently associated with
         * since 3.1.0
         */
        public Params(@NonNull Context context) {
            this(context, SendbirdUIKit.getDefaultThemeMode());
        }

        /**
         * Constructor
         *
         * @param context   The {@code Context} this module is currently associated with
         * @param themeMode The theme of Sendbird UIKit to be applied to this module
         * since 3.1.0
         */
        public Params(@NonNull Context context, @NonNull SendbirdUIKit.ThemeMode themeMode) {
            super(context, themeMode, R.attr.sb_module_open_channel_moderation);
        }

        /**
         * Constructor
         *
         * @param context    The {@code Context} this module is currently associated with
         * @param themeResId The theme resource ID to be applied to this module
         * since 3.1.0
         */
        public Params(@NonNull Context context, @StyleRes int themeResId) {
            super(context, themeResId, R.attr.sb_module_open_channel_moderation);
        }
    }
}
