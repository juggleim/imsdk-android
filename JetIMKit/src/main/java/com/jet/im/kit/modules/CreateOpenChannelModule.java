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
import com.jet.im.kit.modules.components.ChannelProfileInputComponent;
import com.jet.im.kit.modules.components.StateHeaderComponent;

/**
 * A module for create open channel.
 * All composed components are created when the module is created. After than those components can replace.
 *
 * since 3.2.0
 */
public class CreateOpenChannelModule extends BaseModule {
    @NonNull
    private final Params params;
    @NonNull
    private final StateHeaderComponent headerComponent;
    @NonNull
    private final ChannelProfileInputComponent channelProfileInputComponent;

    /**
     * Constructor
     *
     * @param context The {@code Context} this module is currently associated with
     * since 3.2.0
     */
    public CreateOpenChannelModule(@NonNull Context context) {
        this(context, new Params(context));
    }

    /**
     * Constructor
     *
     * @param context The {@code Context} this module is currently associated with
     * @param params  The Parameter object that can customize a default Fragment.
     * since 3.2.0
     */
    public CreateOpenChannelModule(@NonNull Context context, @NonNull Params params) {
        this.params = params;
        this.headerComponent = new StateHeaderComponent();
        this.headerComponent.getParams().setRightButtonText(context.getString(R.string.sb_text_button_create));
        this.channelProfileInputComponent = new ChannelProfileInputComponent();
    }

    @Override
    @NonNull
    public View onCreateView(@NonNull Context context, @NonNull LayoutInflater inflater, @Nullable Bundle args) {
        if (args != null) params.apply(context, args);
        final Context moduleContext = new ContextThemeWrapper(context, params.getTheme());
        final LinearLayout parent = new LinearLayout(context);
        parent.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        parent.setOrientation(LinearLayout.VERTICAL);

        final TypedValue values = new TypedValue();
        if (params.shouldUseHeader()) {
            moduleContext.getTheme().resolveAttribute(R.attr.sb_component_state_header, values, true);
            final Context headerThemeContext = new ContextThemeWrapper(moduleContext, values.resourceId);
            final LayoutInflater headerInflater = inflater.cloneInContext(headerThemeContext);
            final View header = this.headerComponent.onCreateView(headerThemeContext, headerInflater, parent, args);
            parent.addView(header);
        }

        final ScrollView scrollView = new ScrollView(context);
        scrollView.setBackgroundResource(SendbirdUIKit.isDarkMode() ? R.color.background_600 : R.color.background_50);
        scrollView.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        scrollView.setFillViewport(true);

        final LinearLayout innerContainer = new LinearLayout(context);
        innerContainer.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        innerContainer.setOrientation(LinearLayout.VERTICAL);

        moduleContext.getTheme().resolveAttribute(R.attr.sb_component_channel_profile_input, values, true);
        final Context listThemeContext = new ContextThemeWrapper(moduleContext, values.resourceId);
        final LayoutInflater listInflater = inflater.cloneInContext(listThemeContext);
        final View userListLayout = this.channelProfileInputComponent.onCreateView(listThemeContext, listInflater, innerContainer, args);
        innerContainer.addView(userListLayout);

        scrollView.addView(innerContainer);
        parent.addView(scrollView);
        return parent;
    }

    /**
     * Returns a collection of parameters applied to this module.
     *
     * @return {@link Params} applied to this module.
     * since 3.2.0
     */
    @NonNull
    public Params getParams() {
        return params;
    }

    /**
     * Returns the header component.
     *
     * @return The header component of this module
     * since 3.2.0
     */
    @NonNull
    public StateHeaderComponent getHeaderComponent() {
        return headerComponent;
    }

    /**
     * Returns the channel profile input component.
     *
     * @return The channel profile input component of this module
     * since 3.2.0
     */
    @NonNull
    public ChannelProfileInputComponent getChannelProfileInputComponent() {
        return channelProfileInputComponent;
    }

    public static class Params extends BaseModule.Params {
        /**
         * Constructor
         *
         * @param context The {@code Context} this module is currently associated with
         * since 3.2.0
         */
        public Params(@NonNull Context context) {
            this(context, SendbirdUIKit.getDefaultThemeMode());
        }

        /**
         * Constructor
         *
         * @param context   The {@code Context} this module is currently associated with
         * @param themeMode The theme of Sendbird UIKit to be applied to this module
         * since 3.2.0
         */
        public Params(@NonNull Context context, @NonNull SendbirdUIKit.ThemeMode themeMode) {
            super(context, themeMode, R.attr.sb_module_create_open_channel);
        }

        /**
         * Constructor
         *
         * @param context    The {@code Context} this module is currently associated with
         * @param themeResId The theme resource ID to be applied to this module
         * since 3.2.0
         */
        public Params(@NonNull Context context, @StyleRes int themeResId) {
            super(context, themeResId, R.attr.sb_module_create_open_channel);
        }
    }
}
