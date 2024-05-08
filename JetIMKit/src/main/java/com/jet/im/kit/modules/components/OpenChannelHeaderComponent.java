package com.jet.im.kit.modules.components;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.content.res.AppCompatResources;

import com.sendbird.android.SendbirdChat;
import com.sendbird.android.channel.OpenChannel;
import com.jet.im.kit.R;
import com.jet.im.kit.consts.StringSet;
import com.jet.im.kit.internal.ui.components.HeaderView;
import com.jet.im.kit.utils.ChannelUtils;


/**
 * This class creates and performs a view corresponding the open channel header area in Sendbird UIKit.
 *
 * since 3.0.0
 */
public class OpenChannelHeaderComponent extends HeaderComponent {

    /**
     * Constructor
     *
     * since 3.0.0
     */
    public OpenChannelHeaderComponent() {
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
    public View onCreateView(@NonNull Context context, @NonNull LayoutInflater inflater, @NonNull ViewGroup parent, @Nullable Bundle args) {
        View layout = super.onCreateView(context, inflater, parent, args);
        if (getRootView() instanceof HeaderView) {
            final HeaderView headerView = (HeaderView) layout;
            headerView.getProfileView().setVisibility(getParams().useProfileImage ? View.VISIBLE : View.GONE);
            if (getParams().description != null) {
                headerView.getDescriptionTextView().setVisibility(View.VISIBLE);
                headerView.getDescriptionTextView().setText(getParams().description);
            } else {
                headerView.getDescriptionTextView().setVisibility(View.GONE);
            }
        }
        return layout;
    }

    /**
     * Notifies this component that the channel data has changed.
     *
     * @param channel The latest open channel
     * since 3.0.0
     */
    public void notifyChannelChanged(@NonNull OpenChannel channel) {
        final View rootView = getRootView();
        if (!(rootView instanceof HeaderView)) return;

        final HeaderView headerView = (HeaderView) rootView;
        if (getParams().getTitle() == null) {
            headerView.getTitleTextView().setText(channel.getName());
        }
        if (getParams().useProfileImage) {
            ChannelUtils.loadChannelCover(headerView.getProfileView(), channel);
        }
        if (getParams().description == null) {
            final Context context = rootView.getContext();
            final int count = channel.getParticipantCount();
            headerView.getDescriptionTextView().setVisibility(View.VISIBLE);
            headerView.getDescriptionTextView().setText(String.format(context.getString(R.string.sb_text_header_participants_count), ChannelUtils.makeMemberCountText(count)));
        }

        int headerRightButtonIconResId = channel.isOperator(SendbirdChat.getCurrentUser()) ? R.drawable.icon_info : R.drawable.icon_members;
        if (getParams().getRightButtonIcon() == null) {
            headerView.setRightButtonImageDrawable(AppCompatResources.getDrawable(headerView.getContext(), headerRightButtonIconResId));
        }
    }

    public static class Params extends HeaderComponent.Params {
        @Nullable
        private String description;
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
         * Sets the description of the header.
         *
         * @param description The String to be displayed on the description of header
         */
        public void setDescription(@Nullable String description) {
            this.description = description;
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
         * Returns the description of the header.
         *
         * @return The String displayed on the description of header
         * since 3.0.0
         */
        @Nullable
        public String getDescription() {
            return description;
        }

        /**
         * Returns whether the profile image placed on the left top is used.
         *
         * @return <code>true</code> if the profile image is used, <code>false</code> otherwise
         * since 3.0.0
         */
        public boolean useProfileImage() {
            return useProfileImage;
        }

        /**
         * Apply data that matches keys mapped to Params' properties.
         * {@code KEY_HEADER_DESCRIPTION} is mapped to {@link #setDescription(String)}
         * {@code KEY_USE_HEADER_PROFILE_IMAGE} is mapped to {@link #setUseProfileImage(boolean)}
         *
         * @param context The {@code Context} this component is currently associated with
         * @param args    The sets of arguments to apply at Params.
         * @return This Params object that applied with given data.
         * since 3.2.2
         */
        @NonNull
        protected Params apply(@NonNull Context context, @NonNull Bundle args) {
            super.apply(context, args);
            if (args.containsKey(StringSet.KEY_HEADER_DESCRIPTION)) {
                setDescription(args.getString(StringSet.KEY_HEADER_DESCRIPTION));
            }
            if (args.containsKey(StringSet.KEY_USE_HEADER_PROFILE_IMAGE)) {
                setUseProfileImage(args.getBoolean(StringSet.KEY_USE_HEADER_PROFILE_IMAGE));
            }
            return this;
        }
    }
}
