package com.jet.im.kit.interfaces;

import androidx.annotation.NonNull;
import androidx.annotation.StringRes;

public interface DialogProvider {
    void showWaitingDialog();

    void dismissWaitingDialog();

    void toastError(@StringRes int messageRes);

    void toastError(@StringRes int messageRes, boolean useOverlay);

    void toastError(@NonNull String message);

    void toastError(@NonNull String message, boolean useOverlay);

    void toastSuccess(int messageRes);

    void toastSuccess(int messageRes, boolean useOverlay);
}
