package com.jet.im.kit.modules.components;

import androidx.annotation.NonNull;

import com.sendbird.android.channel.Role;
import com.sendbird.android.user.User;
import com.jet.im.kit.activities.adapter.BannedUserListAdapter;
import com.jet.im.kit.providers.AdapterProviders;

import java.util.List;

/**
 * This class creates and performs a view corresponding the banned user list area in Sendbird UIKit.
 *
 * since 3.0.0
 */
public class BannedUserListComponent extends UserTypeListComponent<User> {
    @NonNull
    private BannedUserListAdapter adapter = AdapterProviders.getBannedUserList().provide();

    /**
     * Constructor
     *
     * since 3.0.0
     */
    public BannedUserListComponent() {
        super();
    }

    /**
     * Sets the banned user list adapter to provide child views on demand. The default is {@code new BannedUserListAdapter()}.
     * <p>When adapter is changed, all existing views are recycled back to the pool. If the pool has only one adapter, it will be cleared.</p>
     *
     * @param adapter The adapter to be applied to this list component
     * since 3.0.0
     */
    public <T extends BannedUserListAdapter> void setAdapter(@NonNull T adapter) {
        this.adapter = adapter;
        super.setAdapter(this.adapter);
    }

    /**
     * Returns the banned user list adapter.
     *
     * @return The adapter applied to this list component
     * since 3.0.0
     */
    @NonNull
    @Override
    protected BannedUserListAdapter getAdapter() {
        return this.adapter;
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
