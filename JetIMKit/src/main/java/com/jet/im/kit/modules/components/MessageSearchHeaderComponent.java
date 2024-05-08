package com.jet.im.kit.modules.components;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.jet.im.kit.R;
import com.jet.im.kit.consts.StringSet;
import com.jet.im.kit.interfaces.OnInputTextChangedListener;
import com.jet.im.kit.interfaces.OnSearchEventListener;
import com.jet.im.kit.internal.ui.components.SearchBarView;
import com.jet.im.kit.utils.SoftInputUtils;

/**
 * This class creates and performs a view corresponding the message search header area in Sendbird UIKit.
 *
 * since 3.0.0
 */
public class MessageSearchHeaderComponent {
    @NonNull
    private final Params params;
    @Nullable
    private SearchBarView searchBarView;
    @Nullable
    private OnInputTextChangedListener inputTextChangedListener;
    @Nullable
    private OnSearchEventListener searchEventListener;
    @Nullable
    private View.OnClickListener clearButtonClickListener;

    /**
     * Constructor
     *
     * since 3.0.0
     */
    public MessageSearchHeaderComponent() {
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
        return this.searchBarView;
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
        final SearchBarView headerView = new SearchBarView(context, null, R.attr.sb_component_message_search_header);
        if (params.searchBarButtonText != null) {
            headerView.getSearchButton().setText(params.searchBarButtonText);
        }
        headerView.getSearchButton().setEnabled(false);
        // bind event
        headerView.setOnSearchEventListener(this::onSearchRequested);
        headerView.setOnInputTextChangedListener(this::onInputTextChanged);
        headerView.setOnClearButtonClickListener(this::onClearButtonClicked);
        SoftInputUtils.showSoftKeyboard(headerView.getBinding().etInputText);
        this.searchBarView = headerView;
        return headerView;
    }

    /**
     * Register a callback to be invoked when the user requests to search messages.
     *
     * @param searchEventListener The callback that will run
     * since 3.0.0
     */
    public void setOnSearchEventListener(@Nullable OnSearchEventListener searchEventListener) {
        this.searchEventListener = searchEventListener;
    }

    /**
     * Register a callback to be invoked when the input text is changed.
     *
     * @param textChangedListener The callback that will run
     * since 3.0.0
     */
    public void setOnInputTextChangedListener(@Nullable OnInputTextChangedListener textChangedListener) {
        this.inputTextChangedListener = textChangedListener;
    }

    /**
     * Register a callback to be invoked when the clear button related to the input is clicked.
     *
     * @param clearButtonClickListener The callback that will run
     * since 3.0.0
     */
    public void setOnClearButtonClickListener(@Nullable View.OnClickListener clearButtonClickListener) {
        this.clearButtonClickListener = clearButtonClickListener;
    }

    /**
     * Called when the user requests to search messages.
     *
     * @param keyword Keyword to search for messages
     * since 3.0.0
     */
    protected void onSearchRequested(@NonNull String keyword) {
        if (this.searchEventListener != null) this.searchEventListener.onSearchRequested(keyword);
    }

    /**
     * Called when the clear button related to the input is clicked.
     *
     * @param view The view clicked
     * since 3.0.0
     */
    protected void onClearButtonClicked(@NonNull View view) {
        if (this.clearButtonClickListener != null) {
            this.clearButtonClickListener.onClick(view);
            return;
        }

        if (searchBarView == null) return;
        searchBarView.setText("");
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
     * since 3.0.0
     */
    protected void onInputTextChanged(@NonNull CharSequence s, int start, int before, int count) {
        if (inputTextChangedListener != null) {
            inputTextChangedListener.onInputTextChanged(s, start, before, count);
            return;
        }

        if (searchBarView == null) return;
        searchBarView.getSearchButton().setEnabled(s.length() > 0);
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
        private String searchBarButtonText;

        /**
         * Constructor
         *
         * since 3.0.0
         */
        protected Params() {
        }

        /**
         * Sets the String of the search button on the search bar.
         *
         * @param searchBarButtonText String to be displayed on the search button
         * since 3.0.0
         */
        public void setSearchBarButtonText(@Nullable String searchBarButtonText) {
            this.searchBarButtonText = searchBarButtonText;
        }

        /**
         * Returns the String of the search button on the search bar.
         *
         * @return String displayed on the search button
         * since 3.0.0
         */
        @Nullable
        public String getSearchBarButtonText() {
            return searchBarButtonText;
        }

        /**
         * Apply data that matches keys mapped to Params' properties.
         * {@code KEY_SEARCH_BAR_BUTTON_TEXT} is mapped to {@link #setSearchBarButtonText(String)}.
         *
         * @param context The {@code Context} this component is currently associated with
         * @param args    The sets of arguments to apply at Params.
         * @return This Params object that applied with given data.
         * since 3.0.0
         */
        @NonNull
        protected Params apply(@NonNull Context context, @NonNull Bundle args) {
            if (args.containsKey(StringSet.KEY_SEARCH_BAR_BUTTON_TEXT)) {
                setSearchBarButtonText(args.getString(StringSet.KEY_SEARCH_BAR_BUTTON_TEXT));
            }
            return this;
        }
    }
}
