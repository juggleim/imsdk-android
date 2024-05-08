package com.jet.im.kit.modules.components;

import android.content.Context;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.view.ContextThemeWrapper;

import com.sendbird.android.channel.GroupChannel;
import com.sendbird.android.channel.Role;
import com.jet.im.kit.R;
import com.jet.im.kit.SendbirdUIKit;
import com.jet.im.kit.consts.StringSet;
import com.jet.im.kit.interfaces.OnItemClickListener;
import com.jet.im.kit.internal.ui.widgets.SingleMenuItemView;
import com.jet.im.kit.model.configurations.ChannelSettingConfig;
import com.jet.im.kit.model.configurations.UIKitConfig;
import com.jet.im.kit.utils.ChannelUtils;

/**
 * This class creates and performs a view corresponding the channel settings menu area in Sendbird UIKit.
 *
 * since 3.0.0
 */
public class ChannelSettingsMenuComponent {
    public enum Menu {
        /**
         * A menu of Moderations to users or members control.
         */
        MODERATIONS,

        /**
         * A menu of notification settings to switch tern on and off.
         */
        NOTIFICATIONS,

        /**
         * A menu for viewing the members of the current channel.
         */
        MEMBERS,

        /**
         * A menu to leave the current channel.
         */
        LEAVE_CHANNEL,

        /**
         * A menu to search messages in the current channel.
         */
        SEARCH_IN_CHANNEL
    }

    @NonNull
    private final Params params;
    @Nullable
    private View menuView;

    @Nullable
    protected OnItemClickListener<Menu> menuClickListener;

    /**
     * Constructor
     *
     * since 3.0.0
     */
    public ChannelSettingsMenuComponent() {
        this.params = new Params();
    }

    /**
     * Returns the view created by {@link #onCreateView(Context, LayoutInflater, ViewGroup, Bundle)}.
     *
     * @return the topmost view containing this view
     * since 3.0.0
     */
    @Nullable
    public View getRootView() {
        return this.menuView;
    }

    /**
     * Returns a collection of parameters applied to this component.
     *
     * @return {@code Params} applied to this component
     * since 3.0.0
     */
    @NonNull
    public Params getParams() {
        return params;
    }

    /**
     * Called after the component was created to make views.
     * <p><b>If this function is used override, {@link #getRootView()} must also be override.</b></p>
     *
     * @param context  The {@code Context} this component is currently associated with
     * @param inflater The LayoutInflater object that can be used to inflate any views in the component
     * @param parent   The ViewGroup into which the new View will be added
     * @param args     The arguments supplied when the component was instantiated, if any
     * @return Return the View for the UI.
     * since 3.0.0
     */
    @NonNull
    public View onCreateView(@NonNull Context context, @NonNull LayoutInflater inflater, @NonNull ViewGroup parent, @Nullable Bundle args) {
        if (args != null) params.apply(context, args);
        final TypedValue values = new TypedValue();
        context.getTheme().resolveAttribute(R.attr.sb_component_channel_settings_menu, values, true);
        final Context menuThemeContext = new ContextThemeWrapper(context, values.resourceId);
        final LayoutInflater menuInflater = inflater.cloneInContext(menuThemeContext);
        final View view = menuInflater.inflate(R.layout.sb_view_channel_settings_menu, parent, false);

        final SingleMenuItemView moderationsItemView = view.findViewById(R.id.moderations);
        moderationsItemView.setName(context.getString(R.string.sb_text_channel_settings_moderations));
        moderationsItemView.setMenuType(SingleMenuItemView.Type.NEXT);
        moderationsItemView.setIcon(R.drawable.icon_moderations);
        moderationsItemView.setVisibility(View.GONE);

        final SingleMenuItemView notificationItemView = view.findViewById(R.id.notification);
        notificationItemView.setName(context.getString(R.string.sb_text_channel_settings_notification));
        notificationItemView.setMenuType(SingleMenuItemView.Type.NEXT);
        notificationItemView.setIcon(R.drawable.icon_notifications);

        final SingleMenuItemView membersItemView = view.findViewById(R.id.members);
        membersItemView.setName(context.getString(R.string.sb_text_channel_settings_members));
        membersItemView.setMenuType(SingleMenuItemView.Type.NEXT);
        membersItemView.setIcon(R.drawable.icon_members);

        final SingleMenuItemView messageSearchItemView = view.findViewById(R.id.messageSearch);
        messageSearchItemView.setName(context.getString(R.string.sb_text_channel_settings_message_search));
        messageSearchItemView.setMenuType(SingleMenuItemView.Type.NONE);
        messageSearchItemView.setIcon(R.drawable.icon_search);
        messageSearchItemView.setVisibility(
            ChannelSettingConfig.getEnableMessageSearch(params.channelSettingConfig) ? View.VISIBLE : View.GONE);

        final SingleMenuItemView leaveItemView = view.findViewById(R.id.leave);
        leaveItemView.setName(context.getString(R.string.sb_text_channel_settings_leave_channel));
        leaveItemView.setMenuType(SingleMenuItemView.Type.NONE);
        leaveItemView.setIcon(R.drawable.icon_leave);
        leaveItemView.setIconTint(SendbirdUIKit.getDefaultThemeMode().getErrorTintColorStateList(context));

        moderationsItemView.setOnClickListener(v -> onMenuClicked(v, Menu.MODERATIONS));
        notificationItemView.setOnClickListener(v -> onMenuClicked(v, Menu.NOTIFICATIONS));
        notificationItemView.setOnActionMenuClickListener(v -> onMenuClicked(v, Menu.NOTIFICATIONS));
        membersItemView.setOnClickListener(v -> onMenuClicked(v, Menu.MEMBERS));
        messageSearchItemView.setOnClickListener(v -> onMenuClicked(v, Menu.SEARCH_IN_CHANNEL));
        leaveItemView.setOnClickListener(v -> onMenuClicked(v, Menu.LEAVE_CHANNEL));
        this.menuView = view;
        return view;
    }

    /**
     * Notifies this component that the channel data has changed.
     *
     * @param channel The latest group channel
     * since 3.0.0
     */
    public void notifyChannelChanged(@NonNull GroupChannel channel) {
        if (this.menuView == null) return;


        final SingleMenuItemView membersItemView = menuView.findViewById(R.id.members);
        membersItemView.setDescription(ChannelUtils.makeMemberCountText(channel.getMemberCount()).toString());
        GroupChannel.PushTriggerOption pushTriggerOption = channel.getMyPushTriggerOption();
        final SingleMenuItemView notificationItemView = menuView.findViewById(R.id.notification);
        notificationItemView.setDescription(ChannelUtils.makePushSettingStatusText(menuView.getContext(), pushTriggerOption));

        final SingleMenuItemView moderationsItemView = menuView.findViewById(R.id.moderations);
        moderationsItemView.setVisibility(channel.getMyRole() == Role.OPERATOR ? View.VISIBLE : View.GONE);
        final SingleMenuItemView messageSearchItemView = menuView.findViewById(R.id.messageSearch);
        messageSearchItemView.setVisibility(
            ChannelSettingConfig.getEnableMessageSearch(params.channelSettingConfig) ? View.VISIBLE : View.GONE);
    }

    /**
     * Register a callback to be invoked when the item of the menu is clicked.
     *
     * @param menuClickListener The callback that will run
     * @see Menu
     * since 3.0.0
     */
    public void setOnMenuClickListener(@NonNull OnItemClickListener<Menu> menuClickListener) {
        this.menuClickListener = menuClickListener;
    }

    /**
     * Called when the item of the menu list is clicked.
     *
     * @param view The View clicked
     * @param menu The menu that the clicked item displays
     * @see Menu
     * since 3.0.0
     */
    protected void onMenuClicked(@NonNull View view, @NonNull Menu menu) {
        if (this.menuClickListener != null) this.menuClickListener.onItemClick(view, 0, menu);
    }

    /**
     * A collection of parameters, which can be applied to a default View. The values of params are not dynamically applied at runtime.
     * Params cannot be created directly, and it is automatically created together when components are created.
     * <p>Since the onCreateView configuring View uses the values of the set Params, we recommend that you set up for Params before the onCreateView is called.</p>
     *
     * @see #getParams()
     * since 3.0.0
     */
    public static class Params {
        @NonNull
        private ChannelSettingConfig channelSettingConfig = UIKitConfig.getGroupChannelSettingConfig();

        /**
         * Constructor
         *
         * since 3.0.0
         */
        protected Params() {
        }

        /**
         * Sets the {@link ChannelSettingConfig} for the channel settings menu.
         * Use {@code UIKitConfig.groupChannelSettingConfig.clone()} for the default value.
         * Example usage:
         *
         * <pre>
         * val channelSettingsMenuComponent = ChannelSettingsMenuComponent()
         * channelSettingsMenuComponent.params.channelSettingConfig = UIKitConfig.groupChannelSettingConfig.clone().apply {
         *     this.enableMessageSearch = true
         * }
         * </pre>
         *
         * @param channelSettingConfig The ChannelSettingConfig for the channel settings menu.
         * @see ChannelSettingConfig
         * since 3.6.0
         */
        public void setChannelSettingConfig(@NonNull ChannelSettingConfig channelSettingConfig) {
            this.channelSettingConfig = channelSettingConfig;
        }

        /**
         * Returns the {@link ChannelSettingConfig} for the channel settings menu.
         *
         * @return The ChannelSettingConfig for the channel settings menu.
         * @see ChannelSettingConfig
         * since 3.6.0
         */
        @NonNull
        public ChannelSettingConfig getChannelSettingConfig() {
            return channelSettingConfig;
        }


        /**
         * Apply data that matches keys mapped to Params' properties.
         * {@code KEY_CHANNEL_SETTING_CONFIG} is mapped to {@link #setChannelSettingConfig(ChannelSettingConfig)}
         *
         * @param context The {@code Context} this component is currently associated with
         * @param args    The sets of arguments to apply at Params.
         * @return This Params object that applied with given data.
         * since 3.0.0
         */
        @NonNull
        protected Params apply(@NonNull Context context, @NonNull Bundle args) {
            if (args.containsKey(StringSet.KEY_CHANNEL_SETTING_CONFIG)) {
                setChannelSettingConfig(args.getParcelable(StringSet.KEY_CHANNEL_SETTING_CONFIG));
            }
            return this;
        }
    }
}
