package com.jet.im.kit.interfaces;

import androidx.annotation.NonNull;

import java.util.List;

/**
 * Interface definition for a callback to be invoked when a user view holder is clicked.
 *
 * since 1.2.0
 */
public interface OnUserSelectChangedListener {
    /**
     * Called when a user view holder has been clicked.
     *
     * @param selectedUserIds The list of user ids who are selected.
     * @param isSelected Whether the view holder is checked or not.
     */
    void onUserSelectChanged(@NonNull List<String> selectedUserIds, boolean isSelected);
}
