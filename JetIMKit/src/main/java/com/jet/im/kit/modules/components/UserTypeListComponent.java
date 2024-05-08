package com.jet.im.kit.modules.components;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.sendbird.android.user.User;
import com.jet.im.kit.R;
import com.jet.im.kit.activities.adapter.UserTypeListAdapter;
import com.jet.im.kit.consts.StringSet;
import com.jet.im.kit.interfaces.OnItemClickListener;
import com.jet.im.kit.interfaces.OnItemLongClickListener;
import com.jet.im.kit.interfaces.OnPagedDataLoader;
import com.jet.im.kit.internal.ui.widgets.PagerRecyclerView;

import java.util.List;

/**
 * This class creates and performs a view corresponding the user list area in Sendbird UIKit.
 *
 * since 3.0.0
 */
abstract public class UserTypeListComponent<T extends User> {
    @NonNull
    private final Params params;
    @Nullable
    private PagerRecyclerView recyclerView;

    @Nullable
    private OnItemClickListener<T> itemClickListener;
    @Nullable
    private OnItemLongClickListener<T> itemLongClickListener;
    @Nullable
    private OnItemClickListener<T> actionItemClickListener;
    @Nullable
    private OnItemClickListener<T> profileClickListener;

    /**
     * Constructor
     *
     * since 3.0.0
     */
    public UserTypeListComponent() {
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
        return this.recyclerView;
    }

    /**
     * Gets the parameter object on this component
     *
     * @return The data sets of this component.
     * @see MessageListComponent.Params
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
        this.recyclerView = new PagerRecyclerView(context, null, R.attr.sb_component_list);
        this.recyclerView.setLayoutManager(new LinearLayoutManager(context));
        this.recyclerView.setHasFixedSize(true);
        this.recyclerView.setThreshold(5);
        setAdapter(getAdapter());
        return this.recyclerView;
    }

    /**
     * Register a callback to be invoked when the item of the user is clicked.
     *
     * @param itemClickListener The callback that will run
     * since 3.0.0
     */
    public void setOnItemClickListener(@Nullable OnItemClickListener<T> itemClickListener) {
        this.itemClickListener = itemClickListener;
    }

    /**
     * Register a callback to be invoked when the item of the user is long-clicked.
     *
     * @param itemLongClickListener The callback that will run
     * since 3.0.0
     */
    public void setOnItemLongClickListener(@Nullable OnItemLongClickListener<T> itemLongClickListener) {
        this.itemLongClickListener = itemLongClickListener;
    }

    /**
     * Register a callback to be invoked when the action button of the item is clicked.
     *
     * @param actionItemClickListener The callback that will run
     * since 3.0.0
     */
    public void setOnActionItemClickListener(@Nullable OnItemClickListener<T> actionItemClickListener) {
        this.actionItemClickListener = actionItemClickListener;
    }

    /**
     * Register a callback to be invoked when the profile view of the item is clicked.
     *
     * @param profileClickListener The callback that will run
     * since 3.0.0
     */
    public void setOnProfileClickListener(@Nullable OnItemClickListener<T> profileClickListener) {
        this.profileClickListener = profileClickListener;
    }

    /**
     * Sets the paged data loader for user list.
     *
     * @param pagedDataLoader The paged data loader to be applied to this list component
     * since 3.0.0
     */
    public void setPagedDataLoader(@NonNull OnPagedDataLoader<List<T>> pagedDataLoader) {
        if (recyclerView != null) recyclerView.setPager(pagedDataLoader);
    }

    /**
     * Called when the profile view of the item is clicked.
     *
     * @param view     The View clicked.
     * @param position The position clicked.
     * @param user     The user that the clicked item displays
     * since 3.0.0
     */
    protected void onUserProfileClicked(@NonNull View view, int position, @NonNull T user) {
        if (profileClickListener != null) profileClickListener.onItemClick(view, position, user);
    }

    /**
     * Called when the action button of the item is clicked.
     *
     * @param view     The View clicked.
     * @param position The position clicked.
     * @param user     The user that the clicked item displays
     * since 3.0.0
     */
    protected void onActionItemClicked(@NonNull View view, int position, @NonNull T user) {
        if (actionItemClickListener != null)
            actionItemClickListener.onItemClick(view, position, user);
    }

    /**
     * Called when the item of the user list is long-clicked.
     *
     * @param view     The View long-clicked.
     * @param position The position long-clicked.
     * @param user     The user that the long-clicked item displays
     * since 3.0.0
     */
    protected void onItemLongClicked(@NonNull View view, int position, @NonNull T user) {
        if (itemLongClickListener != null)
            itemLongClickListener.onItemLongClick(view, position, user);
    }

    /**
     * Called when the item of the user list is clicked.
     *
     * @param view     The View clicked.
     * @param position The position clicked.
     * @param user     The user that the clicked item displays
     * since 3.0.0
     */
    protected void onItemClicked(@NonNull View view, int position, @NonNull T user) {
        if (itemClickListener != null) itemClickListener.onItemClick(view, position, user);
    }

    /**
     * Sets the user list adapter.
     *
     * @param adapter The adapter to be applied to this list component
     * since 3.0.0
     */
    protected <A extends UserTypeListAdapter<T>> void setAdapter(@NonNull A adapter) {
        if (adapter.getOnItemClickListener() == null) {
            adapter.setOnItemClickListener(this::onItemClicked);
        }
        if (adapter.getOnItemLongClickListener() == null) {
            adapter.setOnItemLongClickListener(this::onItemLongClicked);
        }
        if (adapter.getOnActionItemClickListener() == null) {
            adapter.setOnActionItemClickListener(this::onActionItemClicked);
        }
        if (adapter.getOnProfileClickListener() == null) {
            adapter.setOnProfileClickListener(params.useUserProfile ? this::onUserProfileClicked : null);
        }

        if (!(getRootView() instanceof PagerRecyclerView)) return;
        final PagerRecyclerView listView = (PagerRecyclerView) getRootView();
        listView.setAdapter(adapter);
    }

    /**
     * Returns the user list adapter.
     *
     * @return The adapter applied to this list component
     * since 3.0.0
     */
    @NonNull
    abstract protected UserTypeListAdapter<T> getAdapter();

    /**
     * A collection of parameters, which can be applied to a default View. The values of params are not dynamically applied at runtime.
     * Params cannot be created directly, and it is automatically created together when components are created.
     * <p>Since the onCreateView configuring View uses the values of the set Params, we recommend that you set up for Params before the onCreateView is called.</p>
     *
     * @see #getParams()
     * since 3.0.0
     */
    public static class Params {
        private boolean useUserProfile = true;

        /**
         * Constructor
         *
         * since 3.0.0
         */
        protected Params() {
        }

        /**
         * Sets whether the user profile is shown when the profile of the user is clicked.
         *
         * @param useUserProfile <code>true</code> if the user profile is shown, <code>false</code> otherwise
         * since 3.0.0
         */
        public void setUseUserProfile(boolean useUserProfile) {
            this.useUserProfile = useUserProfile;
        }

        /**
         * Returns whether the user profile uses when the profile of the user is clicked.
         *
         * @return <code>true</code> if the user profile is shown, <code>false</code> otherwise
         * since 3.0.0
         */
        public boolean shouldUseUserProfile() {
            return useUserProfile;
        }

        /**
         * Apply data that matches keys mapped to Params' properties.
         * {@code KEY_HEADER_TITLE} is mapped to {@link #setUseUserProfile(boolean)}.
         *
         * @param context The {@code Context} this component is currently associated with
         * @param args    The sets of arguments to apply at Params.
         * @return This Params object that applied with given data.
         * since 3.0.0
         */
        @NonNull
        protected Params apply(@NonNull Context context, @NonNull Bundle args) {
            if (args.containsKey(StringSet.KEY_USE_USER_PROFILE)) {
                setUseUserProfile(args.getBoolean(StringSet.KEY_USE_USER_PROFILE));
            }
            return this;
        }
    }
}
