package com.jet.im.kit.interfaces;

import androidx.annotation.NonNull;

/**
 * Interface definition that delivers the results text.
 */
public interface OnEditTextResultListener {
    /**
     * Forward the text written in the target EditText.
     *
     * @param text A text that is written in the target EditText
     */
    void onResult(@NonNull String text);
}
