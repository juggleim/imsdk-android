package com.jet.im.kit.modules.components;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.jet.im.kit.R;
import com.jet.im.kit.activities.adapter.SelectUserListAdapter;
import com.jet.im.kit.interfaces.OnPagedDataLoader;
import com.jet.im.kit.interfaces.OnUserSelectChangedListener;
import com.jet.im.kit.interfaces.OnUserSelectionCompleteListener;
import com.jet.im.kit.internal.ui.widgets.PagerRecyclerView;

import java.util.List;

/**
 * This class creates and performs a view corresponding the user list area when selecting users in Sendbird UIKit.
 *
 * since 3.0.0
 */
abstract public class SelectUserListComponent<T> {
    @NonNull
    private final Params params;
    @Nullable
    private PagerRecyclerView recyclerView;

    @Nullable
    private OnUserSelectChangedListener userSelectChangedListener;
    private OnUserSelectionCompleteListener userSelectionCompleteListener;

    /**
     * Constructor
     *
     * since 3.0.0
     */
    public SelectUserListComponent() {
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
        this.recyclerView = new PagerRecyclerView(context, null, R.attr.sb_component_list);
        this.recyclerView.setLayoutManager(new LinearLayoutManager(context));
        this.recyclerView.setHasFixedSize(true);
        this.recyclerView.setThreshold(5);
        setAdapter(getAdapter());
        return this.recyclerView;
    }

    /**
     * Register a callback to be invoked when the user is selected.
     *
     * @param userSelectChangedListener The callback that will run
     * since 3.0.0
     */
    public void setOnUserSelectChangedListener(@Nullable OnUserSelectChangedListener userSelectChangedListener) {
        this.userSelectChangedListener = userSelectChangedListener;
    }

    /**
     * Register a callback to be invoked when selecting users is completed.
     *
     * @param userSelectionCompleteListener The callback that will run
     * since 3.0.0
     */
    public void setOnUserSelectionCompleteListener(@Nullable OnUserSelectionCompleteListener userSelectionCompleteListener) {
        this.userSelectionCompleteListener = userSelectionCompleteListener;
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
     * Sets the user list adapter when selecting users.
     *
     * @param adapter The adapter to be applied to this list component
     * since 3.0.0
     */
    protected <A extends SelectUserListAdapter<T>> void setAdapter(A adapter) {
        if (adapter.getOnUserSelectChangedListener() == null) {
            adapter.setOnUserSelectChangedListener(this::onUserSelectionChanged);
        }
        if (!(getRootView() instanceof PagerRecyclerView)) return;
        final PagerRecyclerView listView = (PagerRecyclerView) getRootView();
        listView.setAdapter(adapter);
    }

    /**
     * Returns the user list adapter when selecting users.
     *
     * @return The adapter applied to this list component
     * since 3.0.0
     */
    @NonNull
    abstract protected SelectUserListAdapter<T> getAdapter();

    /**
     * Called when the user is selected.
     *
     * @param selectedUserIds The list of identifiers for selected users
     * @param isSelected      Whether the clicked user is selected
     */
    protected void onUserSelectionChanged(@NonNull List<String> selectedUserIds, boolean isSelected) {
        if (userSelectChangedListener != null)
            userSelectChangedListener.onUserSelectChanged(selectedUserIds, isSelected);
    }

    /**
     * Notifies this component that selecting users has been completed.
     *
     * since 3.0.0
     */
    public void notifySelectionComplete() {
        if (recyclerView == null) return;
        final List<String> selectedUserIds = getAdapter().getSelectedUserIdList();
        if (userSelectionCompleteListener != null)
            userSelectionCompleteListener.onUserSelectionCompleted(selectedUserIds);
    }

    /**
     * Notifies this component that the list of users who can be selected is changed.
     *
     * @param data The list of users to be displayed on this component
     * since 3.0.0
     */
    public void notifyDataSetChanged(@NonNull List<T> data) {
        if (recyclerView == null) return;
        getAdapter().setItems(data);
    }

    /**
     * Notifies this component that the list of disabled users is changed.
     *
     * @param disabledUserIds The list of disabled users to be displayed on this component
     * since 3.0.0
     */
    public void notifyDisabledUserIds(@NonNull List<String> disabledUserIds) {
        if (recyclerView == null) return;
        getAdapter().setDisabledUserIdList(disabledUserIds);
        getAdapter().notifyItemRangeChanged(0, getAdapter().getItemCount());
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
