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
import com.jet.im.kit.modules.components.ChannelHeaderComponent;
import com.jet.im.kit.modules.components.MessageInputComponent;
import com.jet.im.kit.modules.components.MessageListComponent;
import com.jet.im.kit.modules.components.StatusComponent;

/**
 * A module for channel. This module is composed of a header, list, input, and status.
 * All composed components are created when the module is created. After than those components can replace.
 *
 * <ul>
 *  <li>Header component is {@link ChannelHeaderComponent} and you can set custom header component by {@link #setHeaderComponent(ChannelHeaderComponent)}
 *  <li>List component is {@link MessageListComponent} and you can set custom list component by {@link #setMessageListComponent(MessageListComponent)}
 *  <li>Input component is {@link MessageInputComponent} and you can set custom input component by {@link #setInputComponent(MessageInputComponent)}
 *  <li>Status component is {@link StatusComponent} and you can set custom status component by {@link #setStatusComponent(StatusComponent)}
 * </ul>
 *
 * since 3.0.0
 */
public class ChannelModule extends BaseMessageListModule<MessageListComponent> {
    @NonNull
    private final Params params;
    @NonNull
    private ChannelHeaderComponent headerComponent;

    /**
     * Constructor
     *
     * @param context The {@code Context} this module is currently associated with
     * since 3.0.0
     */
    public ChannelModule(@NonNull Context context) {
        this(context, new Params(context));
    }

    /**
     * Constructor
     *
     * @param context The {@code Context} this module is currently associated with
     * @param params  The Parameter object that can customize a default Fragment.
     * since 3.0.0
     */
    public ChannelModule(@NonNull Context context, @NonNull Params params) {
        super(context, new MessageListComponent());
        this.params = params;
        this.headerComponent = new ChannelHeaderComponent();
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
            moduleContext.getTheme().resolveAttribute(R.attr.sb_component_header, values, true);
            final Context headerThemeContext = new ContextThemeWrapper(moduleContext, values.resourceId);
            final LayoutInflater headerInflater = inflater.cloneInContext(headerThemeContext);
            final View header = getHeaderComponent().onCreateView(headerThemeContext, headerInflater, parent, args);
            parent.addView(header);
        }

        final FrameLayout bodyContainer = new FrameLayout(context);
        bodyContainer.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT, 1.0f));
        parent.addView(bodyContainer);

        moduleContext.getTheme().resolveAttribute(R.attr.sb_component_list, values, true);
        final Context listThemeContext = new ContextThemeWrapper(moduleContext, values.resourceId);
        final LayoutInflater listInflater = inflater.cloneInContext(listThemeContext);
        final View messageListLayout = getMessageListComponent().onCreateView(listThemeContext, listInflater, bodyContainer, args);
        bodyContainer.addView(messageListLayout);

        moduleContext.getTheme().resolveAttribute(R.attr.sb_component_status, values, true);
        final Context statusThemeContext = new ContextThemeWrapper(moduleContext, values.resourceId);
        final LayoutInflater statusInflater = inflater.cloneInContext(statusThemeContext);
        final View statusLayout = getStatusComponent().onCreateView(statusThemeContext, statusInflater, bodyContainer, args);
        bodyContainer.addView(statusLayout);

        moduleContext.getTheme().resolveAttribute(R.attr.sb_component_channel_message_input, values, true);
        final Context inputThemeContext = new ContextThemeWrapper(moduleContext, values.resourceId);
        final LayoutInflater inputInflater = inflater.cloneInContext(inputThemeContext);
        final View inputLayout = getMessageInputComponent().onCreateView(inputThemeContext, inputInflater, parent, args);
        parent.addView(inputLayout);
        return parent;
    }

    /**
     * Sets a custom channel header component.
     *
     * @param component The channel header component to be used in this module
     * since 3.0.0
     */
    public <T extends ChannelHeaderComponent> void setHeaderComponent(@NonNull T component) {
        this.headerComponent = component;
    }

    /**
     * Returns the channel header component.
     *
     * @return The channel header component of this module
     * since 3.0.0
     */
    @NonNull
    public ChannelHeaderComponent getHeaderComponent() {
        return headerComponent;
    }

    @NonNull
    @Override
    public Params getParams() {
        return params;
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
            super(context, themeMode, R.attr.sb_module_channel);
        }

        /**
         * Constructor
         *
         * @param context    The {@code Context} this module is currently associated with
         * @param themeResId The theme resource ID to be applied to this module
         * since 3.0.0
         */
        public Params(@NonNull Context context, @StyleRes int themeResId) {
            super(context, themeResId, R.attr.sb_module_channel);
        }
    }
}
