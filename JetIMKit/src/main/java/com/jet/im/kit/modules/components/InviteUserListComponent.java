package com.jet.im.kit.modules.components;

import androidx.annotation.NonNull;

import com.jet.im.kit.activities.adapter.InviteUserListAdapter;
import com.jet.im.kit.interfaces.UserInfo;
import com.jet.im.kit.providers.AdapterProviders;

/**
 * This class creates and performs a view corresponding the user list area when inviting users in Sendbird UIKit.
 *
 * since 3.0.0
 */
public class InviteUserListComponent extends SelectUserListComponent<UserInfo> {
    @NonNull
    private InviteUserListAdapter adapter = AdapterProviders.getInviteUserList().provide();

    /**
     * Sets the user list adapter when inviting users to provide child views on demand. The default is {@code new InviteUserListAdapter()}.
     * <p>When adapter is changed, all existing views are recycled back to the pool. If the pool has only one adapter, it will be cleared.</p>
     *
     * @param adapter The adapter to be applied to this list component
     * since 3.0.0
     */
    public <T extends InviteUserListAdapter> void setAdapter(@NonNull T adapter) {
        this.adapter = adapter;
        super.setAdapter(this.adapter);
    }
    /**
     * Returns the user list adapter when inviting users.
     *
     * @return The adapter applied to this list component
     * since 3.0.0
     */
    @NonNull
    @Override
    protected InviteUserListAdapter getAdapter() {
        return adapter;
    }
}
