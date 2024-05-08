package com.jet.im.kit.modules.components;

import androidx.annotation.NonNull;

import com.sendbird.android.user.Member;
import com.jet.im.kit.activities.adapter.RegisterOperatorListAdapter;
import com.jet.im.kit.providers.AdapterProviders;

/**
 * This class creates and performs a view corresponding the member list area when registering operators in Sendbird UIKit.
 *
 * since 3.0.0
 */
public class RegisterOperatorListComponent extends SelectUserListComponent<Member> {
    @NonNull
    private RegisterOperatorListAdapter adapter = AdapterProviders.getRegisterOperatorList().provide();

    /**
     * Returns the member list adapter when registering operators.
     *
     * @return The adapter applied to this list component
     * since 3.0.0
     */
    @NonNull
    @Override
    protected RegisterOperatorListAdapter getAdapter() {
        return adapter;
    }

    /**
     * Sets the member list adapter when registering operators to provide child views on demand. The default is {@code new RegisterOperatorListAdapter()}.
     * <p>When adapter is changed, all existing views are recycled back to the pool. If the pool has only one adapter, it will be cleared.</p>
     *
     * @param adapter The adapter to be applied to this list component
     * since 3.0.0
     */
    public <T extends RegisterOperatorListAdapter> void setAdapter(@NonNull T adapter) {
        this.adapter = adapter;
        super.setAdapter(this.adapter);
    }
}
