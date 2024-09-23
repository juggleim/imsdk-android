package com.jet.im.kit.modules.components;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.jet.im.kit.R;
import com.jet.im.kit.consts.StringSet;
import com.jet.im.kit.widgets.StatusFrameView;

/**
 * This class creates and performs a view corresponding the status area in Sendbird UIKit.
 *
 * since 3.0.0
 */
public class StatusComponent {
    @NonNull
    private final Params params;
    @Nullable
    private StatusFrameView statusFrameView;

    @Nullable
    private View.OnClickListener actionButtonClickListener;

    /**
     * Constructor
     *
     * since 3.0.0
     */
    public StatusComponent() {
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
        return this.statusFrameView;
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
        final StatusFrameView statusView = new StatusFrameView(context, null, R.attr.sb_component_status);

        if (params.errorText != null) {
            statusView.setErrorText(params.errorText);
        }

        if (params.emptyIcon != null) {
            statusView.setEmptyIcon(params.emptyIcon);
        }

        if (params.emptyIconTint != null) {
            statusView.setEmptyIconTint(params.emptyIconTint);
            statusView.setActionIconTint(params.emptyIconTint);
            statusView.setErrorIconTint(params.emptyIconTint);
        }

        if (params.emptyText != null) {
            statusView.setEmptyText(params.emptyText);
        }
        this.statusFrameView = statusView;
        this.statusFrameView.setOnActionEventListener(this::onActionButtonClicked);
        return this.statusFrameView;
    }

    /**
     * Register a callback to be invoked when the action button is clicked.
     *
     * @param actionButtonClickListener The callback that will run
     * since 3.0.0
     */
    public void setOnActionButtonClickListener(@Nullable View.OnClickListener actionButtonClickListener) {
        this.actionButtonClickListener = actionButtonClickListener;
    }

    /**
     * Called when the action button is clicked.
     *
     * @param view The View clicked.
     * since 3.0.0
     */
    protected void onActionButtonClicked(@NonNull View view) {
        if (actionButtonClickListener != null) actionButtonClickListener.onClick(view);
    }

    /**
     * Notifies this component that the status is changed.
     *
     * @param status The status to be displayed on this component
     * @see StatusFrameView.Status
     * since 3.0.0
     */
    public void notifyStatusChanged(@NonNull StatusFrameView.Status status) {
        if (this.statusFrameView == null) return;
        this.statusFrameView.setStatus(status);
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
        @Nullable
        private Drawable emptyIcon;
        @Nullable
        private ColorStateList emptyIconTint;
        @Nullable
        private String emptyText;
        @Nullable
        private String errorText;

        /**
         * Constructor
         *
         * since 3.0.0
         */
        protected Params() {
        }

        /**
         * Sets the icon when the data is not exists.
         *
         * @param emptyIcon The Drawable to be displayed when the status is empty
         * since 3.0.0
         */
        public void setEmptyIcon(@Nullable Drawable emptyIcon) {
            this.emptyIcon = emptyIcon;
        }

        /**
         * Sets the color of the icon that is shown when the data is not exists.
         *
         * @param emptyIconTint Color state list to be applied to the empty icon
         * since 3.0.0
         */
        public void setEmptyIconTint(@Nullable ColorStateList emptyIconTint) {
            this.emptyIconTint = emptyIconTint;
        }

        /**
         * Sets the text when the data is not exists.
         *
         * @param emptyText The String to be displayed when the status is empty
         * since 3.0.0
         */
        public void setEmptyText(@Nullable String emptyText) {
            this.emptyText = emptyText;
        }

        /**
         * Sets the text when the error occurs.
         *
         * @param errorText The String to be displayed when the status is error
         * since 3.0.0
         */
        public void setErrorText(@Nullable String errorText) {
            this.errorText = errorText;
        }

        /**
         * Returns the icon when the data is not exists.
         *
         * @return The Drawable displayed when the status is empty
         * since 3.0.0
         */
        @Nullable
        public Drawable getEmptyIcon() {
            return emptyIcon;
        }

        /**
         * Returns the color of the icon that is shown when the data is not exists.
         *
         * @return Color state list applied to the empty icon
         * since 3.0.0
         */
        @Nullable
        public ColorStateList getEmptyIconTint() {
            return emptyIconTint;
        }

        /**
         * Returns the text when the data is not exists.
         *
         * @return The String displayed when the status is empty
         * since 3.0.0
         */
        @Nullable
        public String getEmptyText() {
            return emptyText;
        }

        /**
         * Returns the text when the error occurs.
         *
         * @return The String displayed when the status is error
         * since 3.0.0
         */
        @Nullable
        public String getErrorText() {
            return errorText;
        }

        /**
         * Apply data that matches keys mapped to Params' properties.
         * {@code KEY_EMPTY_ICON_RES_ID} is mapped to {@link #setEmptyIcon(Drawable)}
         * {@code KEY_EMPTY_ICON_TINT} is mapped to {@link #setEmptyIconTint(ColorStateList)}
         * {@code KEY_EMPTY_TEXT_RES_ID} is mapped to {@link #setEmptyText(String)}
         * {@code KEY_ERROR_TEXT_RES_ID} is mapped to {@link #setErrorText(String)}
         *
         * @param context The {@code Context} this component is currently associated with
         * @param args    The sets of arguments to apply at Params.
         * @return This Params object that applied with given data.
         * since 3.0.0
         */
        @NonNull
        protected Params apply(@NonNull Context context, @NonNull Bundle args) {
            if (args.containsKey(StringSet.KEY_EMPTY_ICON_RES_ID)) {
                final int iconResId = args.getInt(StringSet.KEY_EMPTY_ICON_RES_ID);
                setEmptyIcon(ContextCompat.getDrawable(context, iconResId > 0 ? iconResId : android.R.color.transparent));
            }
            if (args.containsKey(StringSet.KEY_EMPTY_ICON_TINT)) {
                setEmptyIconTint(args.getParcelable(StringSet.KEY_EMPTY_ICON_TINT));
            }
            if (args.containsKey(StringSet.KEY_EMPTY_TEXT_RES_ID)) {
                setEmptyText(context.getString(args.getInt(StringSet.KEY_EMPTY_TEXT_RES_ID)));
            }
            if (args.containsKey(StringSet.KEY_ERROR_TEXT_RES_ID)) {
                setErrorText(context.getString(args.getInt(StringSet.KEY_ERROR_TEXT_RES_ID)));
            }
            return this;
        }
    }
}
