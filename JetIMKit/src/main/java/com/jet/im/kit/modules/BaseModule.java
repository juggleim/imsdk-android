package com.jet.im.kit.modules;

import android.content.Context;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;

import androidx.annotation.AttrRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StyleRes;
import androidx.appcompat.view.ContextThemeWrapper;

import com.jet.im.kit.SendbirdUIKit;
import com.jet.im.kit.consts.StringSet;

/**
 * Abstract class for implementing modules used by UIKit
 */
public abstract class BaseModule {
    /**
     * Called after the module was created to make views to use in the Fragment or Activity.
     * In this method, the module is structuring the views using components that belong to components.
     * <p>Not allowed {@code null} value as a return value.</p>
     * <p>The Context and LayoutInflater have already applied themes set at the Params.</p>
     *
     * @param context  The {@code Context} this module is currently associated with
     * @param inflater The LayoutInflater object that can be used to inflate any views in the module
     * @param args     The arguments supplied when the module was instantiated, if any
     * @return Return the View for the UI.
     * since 3.0.0
     */
    @NonNull
    public abstract View onCreateView(@NonNull Context context, @NonNull LayoutInflater inflater, @Nullable Bundle args);

    /**
     * Parameters applied to this module and view of this module. The values of params are not dynamically applied at runtime.
     * When used in each fragment, it is recommended to get the created params and set values in {@code onConfigureParams(BaseModule, Bundle)}.
     *
     * since 3.0.0
     */
    public static class Params {
        @StyleRes
        private int themeResId;
        @AttrRes
        private final int themeAttrResId;

        @NonNull
        private final Context context;

        private Boolean useHeader = true;

        /**
         * Constructor.
         *
         * @param context        The {@code Context} this module is currently associated with
         * @param themeMode      The theme of Sendbird UIKit to be applied to this module
         * @param themeAttrResId The attribute ID to apply to this module.
         * since 3.0.0
         */
        public Params(@NonNull Context context, @NonNull SendbirdUIKit.ThemeMode themeMode, @AttrRes int themeAttrResId) {
            this(context, themeMode.getResId(), themeAttrResId);
        }

        /**
         * Constructor.
         *
         * @param context        The {@code Context} this module is currently associated with
         * @param themeResId     The theme resource ID to be applied to this module
         * @param themeAttrResId The attribute ID to apply to this module.
         * since 3.0.0
         */
        public Params(@NonNull Context context, @StyleRes int themeResId, @AttrRes int themeAttrResId) {
            this.context = context;
            this.themeResId = themeResId;
            this.themeAttrResId = themeAttrResId;
        }

        /**
         * Returns the theme, which can be applied to a fragment.
         *
         * @return The resource identifier of the style to be applied to a fragment.
         * since 3.0.0
         */
        @StyleRes
        public int getTheme() {
            final Context appThemeContext = new ContextThemeWrapper(context, themeResId);
            final TypedValue values = new TypedValue();
            appThemeContext.getTheme().resolveAttribute(themeAttrResId, values, true);
            return values.resourceId;
        }

        /**
         * Returns whether the header is used.
         *
         * @return <code>true</code> if the header is used, <code>false</code> otherwise
         * since 3.0.0
         */
        public boolean shouldUseHeader() {
            return useHeader;
        }

        /**
         * Sets whether the header is used. The default is <code>false</code>.
         *
         * @param useHeader <code>true</code> if the header is used, <code>false</code> otherwise
         * since 3.0.0
         */
        public void setUseHeader(boolean useHeader) {
            this.useHeader = useHeader;
        }

        /**
         * Apply data that matches keys mapped to Params' properties.
         * To reflect values to the UI, this method must be called before calling {@link #onCreateView(Context, LayoutInflater, Bundle)}.
         * {@code KEY_THEME_RES_ID} is mapped to {@link #themeResId}
         * {@code KEY_USE_HEADER} is mapped to {@link #setUseHeader(boolean)}.
         *
         * @param context The {@code Context} this component is currently associated with
         * @param args    The sets of arguments to apply at Params.
         * @return This Params object that applied with given data.
         * since 3.0.0
         */
        @NonNull
        protected Params apply(@NonNull Context context, @NonNull Bundle args) {
            if (args.containsKey(StringSet.KEY_THEME_RES_ID)) {
                this.themeResId = args.getInt(StringSet.KEY_THEME_RES_ID);
            }
            if (args.containsKey(StringSet.KEY_USE_HEADER)) {
                setUseHeader(args.getBoolean(StringSet.KEY_USE_HEADER));
            }
            return this;
        }
    }
}
