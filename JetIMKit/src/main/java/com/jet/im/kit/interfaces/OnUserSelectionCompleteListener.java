package com.jet.im.kit.interfaces;

import androidx.annotation.NonNull;

import java.util.List;

/**
 * Interface definition for a callback that will be invoked when user selections are completed.
 *
 * since 3.0.0
 */
public interface OnUserSelectionCompleteListener {
    /**
     * Called when user selections are completed.
     *
     * @param selectedUserIds The list of user's ids who are selected.
     */
    void onUserSelectionCompleted(@NonNull List<String> selectedUserIds);
}
