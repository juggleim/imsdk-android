package com.jet.im.kit.modules.components;

import androidx.annotation.NonNull;

import com.sendbird.android.channel.Role;
import com.sendbird.android.user.User;
import com.jet.im.kit.activities.adapter.OperatorListAdapter;
import com.jet.im.kit.providers.AdapterProviders;

import java.util.List;

/**
 * This class creates and performs a view corresponding the operator list area in Sendbird UIKit.
 *
 * since 3.0.0
 */
public class OperatorListComponent extends UserTypeListComponent<User> {

    @NonNull
    private OperatorListAdapter adapter = AdapterProviders.getOperatorList().provide();

    /**
     * Returns the operator list adapter.
     *
     * @return The adapter applied to this list component
     * since 3.0.0
     */
    @NonNull
    @Override
    protected OperatorListAdapter getAdapter() {
        return adapter;
    }

    /**
     * Sets the operator list  adapter to provide child views on demand. The default is {@code new OperatorListAdapter()}.
     * <p>When adapter is changed, all existing views are recycled back to the pool. If the pool has only one adapter, it will be cleared.</p>
     *
     * @param adapter The adapter to be applied to this list component
     * since 3.0.0
     */
    public <T extends OperatorListAdapter> void setAdapter(@NonNull T adapter) {
        this.adapter = adapter;
        super.setAdapter(this.adapter);
    }

    /**
     * Notifies this component that the list of users is changed.
     *
     * @param userList The list of users to be displayed on this component
     * @param myRole   Role of the current user
     * since 3.0.0
     */
    public void notifyDataSetChanged(@NonNull List<User> userList, @NonNull Role myRole) {
        this.adapter.setItems(userList, myRole);
    }
}
