package com.jet.im.kit.modules.components;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.sendbird.android.channel.GroupChannel;
import com.jet.im.kit.R;
import com.jet.im.kit.internal.ui.components.ChannelPushSettingView;

public class ChannelPushSettingComponent {

    @NonNull
    private final Params params;
    private ChannelPushSettingView settingView;
    private View.OnClickListener channelPushButtonClickListener;
    private View.OnClickListener pushOptionAllClickListener;
    private View.OnClickListener pushOptionMentionsOnlyClickListener;

    public ChannelPushSettingComponent() {
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
        return settingView;
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
        final ChannelPushSettingView settingView = new ChannelPushSettingView(context, null, R.attr.sb_component_channel_push_setting);
        settingView.setOnSwitchButtonClickListener(this::onSwitchButtonClicked);
        settingView.setOnPushOptionAllClickListener(this::onPushOptionAllClicked);
        settingView.setOnPushOptionMentionsOnlyClickListener(this::onPushOptionMentionsOnlyClicked);
        this.settingView = settingView;
        return settingView;
    }

    /**
     * Register a callback to be invoked when the item of the menu is clicked.
     *
     * @param channelPushButtonClickListener The callback that will run
     * since 3.0.0
     */
    public void setOnSwitchButtonClickListener(@Nullable View.OnClickListener channelPushButtonClickListener) {
        this.channelPushButtonClickListener = channelPushButtonClickListener;
    }

    /**
     * Register a callback to be invoked when the item of the menu is clicked.
     *
     * @param pushOptionAllClickListener The callback that will run
     * since 3.0.0
     */
    public void setOnPushOptionAllClickListener(@Nullable View.OnClickListener pushOptionAllClickListener) {
        this.pushOptionAllClickListener = pushOptionAllClickListener;
    }

    /**
     * Register a callback to be invoked when the item of the menu is clicked.
     *
     * @param pushOptionMentionsOnlyClickListener The callback that will run
     * since 3.0.0
     */
    public void setOnPushOptionMentionsOnlyClickListener(@Nullable View.OnClickListener pushOptionMentionsOnlyClickListener) {
        this.pushOptionMentionsOnlyClickListener = pushOptionMentionsOnlyClickListener;
    }

    /**
     * Called when the item of the menu list is clicked.
     *
     * @param view The View clicked
     * since 3.0.0
     */
    protected void onSwitchButtonClicked(@NonNull View view) {
        if (channelPushButtonClickListener != null) channelPushButtonClickListener.onClick(view);
    }

    /**
     * Called when the item of the menu list is clicked.
     *
     * @param view The View clicked
     * since 3.0.0
     */
    protected void onPushOptionAllClicked(@NonNull View view) {
        if (pushOptionAllClickListener != null) pushOptionAllClickListener.onClick(view);
    }

    /**
     * Called when the item of the menu list is clicked.
     *
     * @param view The View clicked
     * since 3.0.0
     */
    protected void onPushOptionMentionsOnlyClicked(@NonNull View view) {
        if (pushOptionMentionsOnlyClickListener != null) pushOptionMentionsOnlyClickListener.onClick(view);
    }

    /**
     * Notifies this component that the channel data has changed.
     *
     * @param channel The latest group channel
     * since 3.0.0
     */
    public void notifyChannelChanged(@NonNull GroupChannel channel) {
        if (this.settingView == null) return;
        this.settingView.notifyChannelPushOptionChanged(channel.getMyPushTriggerOption());
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
        /**
         * Constructor
         *
         * since 3.0.0
         */
        protected Params() {
        }

        /**
         * Apply data that matches keys mapped to Params' properties.
         *
         * @param context The {@code Context} this component is currently associated with
         * @param args    The sets of arguments to apply at Params.
         * @return This Params object that applied with given data.
         * since 3.0.0
         */
        @NonNull
        protected Params apply(@NonNull Context context, @NonNull Bundle args) {
            return this;
        }
    }
}
