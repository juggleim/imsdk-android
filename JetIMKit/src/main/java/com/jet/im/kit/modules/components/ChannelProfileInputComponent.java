package com.jet.im.kit.modules.components;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.jet.im.kit.R;
import com.jet.im.kit.interfaces.OnInputTextChangedListener;
import com.jet.im.kit.internal.ui.components.ChannelProfileInputView;

/**
 * This class creates and performs a view corresponding the channel profile input area in Sendbird UIKit.
 * since 3.2.0
 */
public class ChannelProfileInputComponent {
    @Nullable
    private ChannelProfileInputView rootView;
    @Nullable
    private OnInputTextChangedListener inputTextChangedListener;
    @Nullable
    private View.OnClickListener clearButtonClickListener;
    @Nullable
    private View.OnClickListener onMediaSelectButtonClickListener;

    @NonNull
    private final Params params;

    public ChannelProfileInputComponent() {
        this.params = new Params();
    }

    /**
     * Returns the view created by {@link #onCreateView(Context, LayoutInflater, ViewGroup, Bundle)}.
     *
     * @return the topmost view containing this view
     * since 3.2.0
     */
    @Nullable
    public View getRootView() {
        return rootView;
    }

    /**
     * Returns a collection of parameters applied to this component.
     *
     * @return {@code Params} applied to this component
     * since 3.2.0
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
     * since 3.2.0
     */
    @NonNull
    public View onCreateView(@NonNull Context context, @NonNull LayoutInflater inflater, @NonNull ViewGroup parent, @Nullable Bundle args) {
        if (args != null) params.apply(context, args);

        this.rootView = new ChannelProfileInputView(context, null, R.attr.sb_component_channel_profile_input);
        this.rootView.setOnInputTextChangedListener(this::onInputTextChanged);
        this.rootView.setOnClearButtonClickListener(this::onClearButtonClicked);
        this.rootView.setOnMediaSelectButtonClickListener(this::onMediaSelectButtonClicked);
        return rootView;
    }

    /**
     * Draw cover image when the cover image selected.
     *
     * @param uri the image uri to draw.
     * since 3.2.0
     */
    public void notifyCoverImageChanged(@Nullable Uri uri) {
        if (this.rootView != null) {
            rootView.drawCoverImage(uri);
        }
    }

    /**
     * Register a callback to be invoked when the input text is changed.
     *
     * @param textChangedListener The callback that will run
     * since 3.2.0
     */
    public void setOnInputTextChangedListener(@Nullable OnInputTextChangedListener textChangedListener) {
        this.inputTextChangedListener = textChangedListener;
    }

    /**
     * Register a callback to be invoked when the clear button related to the input is clicked.
     *
     * @param clearButtonClickListener The callback that will run
     * since 3.2.0
     */
    public void setOnClearButtonClickListener(@Nullable View.OnClickListener clearButtonClickListener) {
        this.clearButtonClickListener = clearButtonClickListener;
    }

    /**
     * Register a callback to be invoked when the media selector is clicked.
     *
     * @param onMediaSelectButtonClickListener The callback that will run
     * since 3.2.0
     */
    public void setOnMediaSelectButtonClickListener(@Nullable View.OnClickListener onMediaSelectButtonClickListener) {
        this.onMediaSelectButtonClickListener = onMediaSelectButtonClickListener;
    }

    /**
     * Called when the clear button related to the input is clicked.
     *
     * @param view The view clicked
     * since 3.2.0
     */
    protected void onClearButtonClicked(@NonNull View view) {
        if (this.clearButtonClickListener != null) {
            this.clearButtonClickListener.onClick(view);
            return;
        }

        if (rootView == null) return;
        rootView.setText("");
    }

    /**
     * Called when the input text is changed.
     * <p>
     * This method is called to notify you that, within <code>s</code>,
     * the <code>count</code> characters beginning at <code>start</code>
     * have just replaced old text that had length <code>before</code>.
     * It is an error to attempt to make changes to <code>s</code> from
     * this callback.
     * </p>
     *
     * since 3.2.0
     */
    protected void onInputTextChanged(@NonNull CharSequence s, int start, int before, int count) {
        if (inputTextChangedListener != null) {
            inputTextChangedListener.onInputTextChanged(s, start, before, count);
        }
    }

    /**
     * Called when the clear button related to the input is clicked.
     *
     * @param view The view clicked
     * since 3.2.0
     */
    protected void onMediaSelectButtonClicked(@NonNull View view) {
        if (this.onMediaSelectButtonClickListener != null) {
            this.onMediaSelectButtonClickListener.onClick(view);
        }
    }

    /**
     * A collection of parameters, which can be applied to a default View. The values of params are not dynamically applied at runtime.
     * Params cannot be created directly, and it is automatically created together when components are created.
     * <p>Since the onCreateView configuring View uses the values of the set Params, we recommend that you set up for Params before the onCreateView is called.</p>
     *
     * @see #getParams()
     * since 3.2.0
     */
    public static class Params {
        /**
         * Constructor
         *
         * since 3.2.0
         */
        protected Params() {
        }

        /**
         * Apply data that matches keys mapped to Params' properties.
         *
         * @param context The {@code Context} this component is currently associated with
         * @param args    The sets of arguments to apply at Params.
         * @return This Params object that applied with given data.
         * since 3.2.0
         */
        @NonNull
        protected Params apply(@NonNull Context context, @NonNull Bundle args) {
            return this;
        }
    }
}
