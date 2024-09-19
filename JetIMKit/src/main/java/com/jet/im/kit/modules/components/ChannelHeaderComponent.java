package com.jet.im.kit.modules.components;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.juggle.im.model.ConversationInfo;
import com.sendbird.android.channel.GroupChannel;
import com.jet.im.kit.consts.StringSet;
import com.jet.im.kit.internal.ui.components.HeaderView;
import com.jet.im.kit.utils.ChannelUtils;
import com.jet.im.kit.utils.TextUtils;


/**
 * This class creates and performs a view corresponding the channel header area in Sendbird UIKit.
 *
 * since 3.0.0
 */
public class ChannelHeaderComponent extends HeaderComponent {

    /**
     * Constructor
     *
     * since 3.0.0
     */
    public ChannelHeaderComponent() {
        super(new Params());
    }

    /**
     * Returns a collection of parameters applied to this component.
     *
     * @return {@code Params} applied to this component
     * since 3.0.0
     */
    @NonNull
    @Override
    public Params getParams() {
        return (Params) super.getParams();
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
    @Override
    public View onCreateView(@NonNull Context context, @NonNull LayoutInflater inflater, @NonNull ViewGroup parent, @Nullable Bundle args) {
        final View layout = super.onCreateView(context, inflater, parent, args);
        if (layout instanceof HeaderView) {
            final HeaderView headerView = (HeaderView) layout;
            headerView.getDescriptionTextView().setVisibility(View.GONE);
            headerView.getProfileView().setVisibility(getParams().useProfileImage ? View.VISIBLE : View.GONE);
        }
        return layout;
    }

    /**
     * Notifies this component that the channel data has changed.
     *
     * @param channel The latest group channel
     * since 3.0.0
     */
    public void notifyChannelChanged(@NonNull ConversationInfo channel) {
        final View rootView = getRootView();
        if (!(rootView instanceof HeaderView)) return;

        final HeaderView headerView = (HeaderView) rootView;
        if (getParams().getTitle() == null) {
            headerView.getTitleTextView().setText(ChannelUtils.makeTitleText(headerView.getContext(), channel));
        }
        if (getParams().useProfileImage) {
            ChannelUtils.loadChannelCover(headerView.getProfileView(), channel);
        }
    }

    /**
     * Called when the description of the header is changed.
     *
     * @param description The latest description
     * since 3.0.0
     */
    public void notifyHeaderDescriptionChanged(@Nullable String description) {
        final View rootView = getRootView();
        if (!(rootView instanceof HeaderView)) return;
        if (!getParams().useTypingIndicator) return;

        final HeaderView headerView = (HeaderView) rootView;
        if (TextUtils.isEmpty(description)) {
            headerView.getDescriptionTextView().setVisibility(View.GONE);
        } else {
            headerView.getDescriptionTextView().setVisibility(View.VISIBLE);
            headerView.getDescriptionTextView().setText(description);
        }
    }

    /**
     * A collection of parameters, which can be applied to a default View. The values of params are not dynamically applied at runtime.
     * Params cannot be created directly, and it is automatically created together when components are created.
     * <p><b>Since the onCreateView configuring View uses the values of the set Params, we recommend that you set up for Params before the onCreateView is called.</b></p>
     *
     * @see #getParams()
     * since 3.0.0
     */
    public static class Params extends HeaderComponent.Params {
        private boolean useTypingIndicator = true;
        private boolean useProfileImage = true;

        /**
         * Constructor
         *
         * since 3.0.0
         */
        protected Params() {
            super();
        }

        /**
         * Sets whether the typing indicator is used.
         *
         * @param useTypingIndicator <code>true</code> if the typing indicator is used, <code>false</code> otherwise
         * since 3.0.0
         */
        public void setUseTypingIndicator(boolean useTypingIndicator) {
            this.useTypingIndicator = useTypingIndicator;
        }

        /**
         * Sets whether the profile image placed on the left top is used.
         *
         * @param useProfileImage <code>true</code> if the profile image is used, <code>false</code> otherwise
         * since 3.0.0
         */
        public void setUseProfileImage(boolean useProfileImage) {
            this.useProfileImage = useProfileImage;
        }

        /**
         * Returns whether the typing indicator is used.
         *
         * @return <code>true</code> if the typing indicator is used, <code>false</code> otherwise
         */
        public boolean shouldUseTypingIndicator() {
            return useTypingIndicator;
        }

        /**
         * Returns whether the profile image placed on the left top is used.
         *
         * @return <code>true</code> if the profile image is used, <code>false</code> otherwise
         */
        public boolean shouldUseProfileImage() {
            return useProfileImage;
        }

        /**
         * Apply data that matches keys mapped to Params' properties.
         * {@code KEY_USE_TYPING_INDICATOR} is mapped to {@link #setUseTypingIndicator(boolean)}
         * {@code KEY_USE_HEADER_PROFILE_IMAGE} is mapped to {@link #setUseProfileImage(boolean)}
         *
         * @param context The {@code Context} this component is currently associated with
         * @param args    The sets of arguments to apply at Params.
         * @return This Params object that applied with given data.
         * since 3.0.0
         */
        @NonNull
        protected Params apply(@NonNull Context context, @NonNull Bundle args) {
            super.apply(context, args);
            if (args.containsKey(StringSet.KEY_USE_TYPING_INDICATOR)) {
                setUseTypingIndicator(args.getBoolean(StringSet.KEY_USE_TYPING_INDICATOR));
            }
            if (args.containsKey(StringSet.KEY_USE_HEADER_PROFILE_IMAGE)) {
                setUseProfileImage(args.getBoolean(StringSet.KEY_USE_HEADER_PROFILE_IMAGE));
            }
            return this;
        }
    }
}
