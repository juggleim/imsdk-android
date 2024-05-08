package com.jet.im.kit.modules.components;


import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.sendbird.android.channel.GroupChannel;
import com.jet.im.kit.internal.ui.components.HeaderView;
import com.jet.im.kit.utils.ChannelUtils;

/**
 * This class creates and performs a view corresponding the message thread header area in Sendbird UIKit.
 *
 * since 3.3.0
 */
public class MessageThreadHeaderComponent extends HeaderComponent {

    @Nullable
    private View.OnClickListener descriptionClickListener;

    /**
     * Constructor
     *
     * since 3.3.0
     */
    public MessageThreadHeaderComponent() {
        super(new Params());
    }

    /**
     * Returns a collection of parameters applied to this component.
     *
     * @return {@code Params} applied to this component
     * since 3.3.0
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
     * since 3.3.0
     */
    @NonNull
    @Override
    public View onCreateView(@NonNull Context context, @NonNull LayoutInflater inflater, @NonNull ViewGroup parent, @Nullable Bundle args) {
        final View layout = super.onCreateView(context, inflater, parent, args);
        if (layout instanceof HeaderView) {
            final HeaderView headerView = (HeaderView) layout;
            headerView.getDescriptionTextView().setVisibility(View.GONE);
            headerView.getDescriptionTextView().setOnClickListener(this::onDescriptionClicked);
        }
        return layout;
    }

    /**
     * Notifies this component that the channel data has changed.
     *
     * @param channel The latest group channel
     * since 3.3.0
     */
    public void notifyChannelChanged(@NonNull GroupChannel channel) {
        final View rootView = getRootView();
        if (!(rootView instanceof HeaderView)) return;

        final HeaderView headerView = (HeaderView) rootView;
        headerView.getDescriptionTextView().setVisibility(View.VISIBLE);
        headerView.getDescriptionTextView().setText(ChannelUtils.makeTitleText(headerView.getContext(), channel));
    }

    /**
     * Register a callback to be invoked when the description is clicked.
     *
     * @param descriptionClickListener The callback that will run
     * since 3.3.0
     */
    public void setOnDescriptionClickListener(@Nullable View.OnClickListener descriptionClickListener) {
        this.descriptionClickListener = descriptionClickListener;
    }

    /**
     * Called when the description is clicked.
     *
     * @param view     The View clicked
     * since 3.3.0
     */
    protected void onDescriptionClicked(@NonNull View view) {
        if (descriptionClickListener != null)
            descriptionClickListener.onClick(view);
    }

    /**
     * A collection of parameters, which can be applied to a default View. The values of params are not dynamically applied at runtime.
     * Params cannot be created directly, and it is automatically created together when components are created.
     * <p><b>Since the onCreateView configuring View uses the values of the set Params, we recommend that you set up for Params before the onCreateView is called.</b></p>
     *
     * @see #getParams()
     * since 3.3.0
     */
    public static class Params extends HeaderComponent.Params {}
}
