package com.jet.im.kit.modules.components;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.jet.im.kit.internal.ui.components.StateHeaderView;

/**
 * This class creates and performs a view corresponding the header area when selecting users in Sendbird UIKit.
 *
 * since 3.0.0
 */
public class SelectUserHeaderComponent extends StateHeaderComponent {

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
        final View parentView = super.onCreateView(context, inflater, parent, args);
        if (parentView instanceof StateHeaderView) {
            final StateHeaderView headerView = (StateHeaderView) parentView;
            headerView.setEnabledRightButton(false);
        }
        return parentView;
    }

    /**
     * Notifies this component that an user is selected.
     *
     * @param count Number of selected users
     * since 3.0.0
     */
    public void notifySelectedUserChanged(int count) {
        if (!(getRootView() instanceof StateHeaderView)) return;

        final StateHeaderView headerView = (StateHeaderView) getRootView();
        headerView.setEnabledRightButton(count > 0);

        if (getParams().getRightButtonText() != null) {
            final String rightButtonText = getParams().getRightButtonText();
            headerView.setRightButtonText(count > 0 ? rightButtonText + " " + "(" + count + ")" : rightButtonText);
        }
    }
}
