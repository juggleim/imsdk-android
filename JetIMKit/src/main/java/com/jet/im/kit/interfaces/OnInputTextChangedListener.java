package com.jet.im.kit.interfaces;

import androidx.annotation.NonNull;

/**
 * Interface definition for a callback to be invoked when a text of message input is changed.
 * since 2.0.1
 */
public interface OnInputTextChangedListener {
    /**
     * This method is called to notify you that, within <code>s</code>,
     * the <code>count</code> characters beginning at <code>start</code>
     * have just replaced old text that had length <code>before</code>.
     * It is an error to attempt to make changes to <code>s</code> from
     * this callback.
     */
    void onInputTextChanged(@NonNull CharSequence s, int start, int before, int count);
}
