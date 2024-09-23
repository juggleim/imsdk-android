package com.jet.im.kit.interfaces;

/**
 * Interface definition for a callback to be invoked before when the loading dialog is called.
 *
 * since 1.2.5
 */
public interface LoadingDialogHandler {
    /**
     * It will be called when the loading dialog needs displaying.
     *
     * @return True if the callback has consumed the event, false otherwise.
     * since 1.2.5
     */
    boolean shouldShowLoadingDialog();

    /**
     * It will be called when the loading dialog needs dismissing.
     *
     * since 1.2.5
     */
    void shouldDismissLoadingDialog();
}
