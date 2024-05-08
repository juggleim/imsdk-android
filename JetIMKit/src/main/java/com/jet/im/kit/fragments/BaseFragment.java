package com.jet.im.kit.fragments;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.jet.im.kit.interfaces.DialogProvider;
import com.jet.im.kit.internal.ui.widgets.WaitingDialog;
import com.jet.im.kit.utils.ContextUtils;

/**
 * Fragment that is the basis of fragments provided by UIKit.
 */
public abstract class BaseFragment extends Fragment implements DialogProvider {
    /**
     * Shows a waiting Dialog.
     */
    @Override
    public void showWaitingDialog() {
        if (!isFragmentAlive()) return;
        WaitingDialog.show(requireContext());
    }

    /**
     * Dismisses a waiting Dialog.
     */
    @Override
    public void dismissWaitingDialog() {
        WaitingDialog.dismiss();
    }

    /**
     * Shows an error toast.
     *
     * @param messageRes String resource ID displayed on error toast
     */
    @Override
    public void toastError(int messageRes) {
        toastError(messageRes, false);
    }

    /**
     * Shows an error toast.
     *
     * @param messageRes String resource ID displayed on error toast
     * @param useOverlay Whether to apply overlay style
     */
    @Override
    public void toastError(int messageRes, boolean useOverlay) {
        if (isFragmentAlive()) ContextUtils.toastError(requireContext(), messageRes);
    }

    /**
     * Shows an error toast.
     *
     * @param message String displayed on error toast
     */
    @Override
    public void toastError(@NonNull String message) {
        toastError(message, false);
    }

    /**
     * Shows an error toast.
     *
     * @param message    String displayed on error toast
     * @param useOverlay Whether to apply overlay style
     */
    @Override
    public void toastError(@NonNull String message, boolean useOverlay) {
        if (isFragmentAlive()) ContextUtils.toastError(requireContext(), message, useOverlay);
    }

    /**
     * Shows an success toast.
     *
     * @param messageRes String resource ID displayed on success toast
     */
    @Override
    public void toastSuccess(int messageRes) {
        toastSuccess(messageRes, false);
    }

    /**
     * Shows an success toast.
     *
     * @param messageRes String resource ID displayed on success toast
     * @param useOverlay Whether to apply overlay style
     */
    @Override
    public void toastSuccess(int messageRes, boolean useOverlay) {
        if (isFragmentAlive()) ContextUtils.toastSuccess(requireContext(), messageRes);
    }

    /**
     * Determines whether the current fragment is alive on the window.
     *
     * @return {@code true} if the current fragment is alive, {@code false} otherwise
     */
    protected boolean isFragmentAlive() {
        boolean isDeactivated = !isAdded() || isRemoving() || isDetached() || getContext() == null;
        return !isDeactivated;
    }

    /**
     * Finishes the activity that has the current fragment.
     */
    protected void shouldActivityFinish() {
        if (getActivity() != null) {
            getActivity().finish();
        }
    }
}
