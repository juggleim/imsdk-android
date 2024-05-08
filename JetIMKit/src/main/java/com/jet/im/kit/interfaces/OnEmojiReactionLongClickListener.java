package com.jet.im.kit.interfaces;

import android.view.View;

import androidx.annotation.NonNull;

import com.sendbird.android.message.BaseMessage;

/**
 * Interface definition for a callback to be invoked when a emoji is clicked.
 */
public interface OnEmojiReactionLongClickListener {
    /**
     * Called when a view has been clicked.
     *
     * @param view The view that was clicked.
     * @param position The position that was clicked.
     * @param message The message that was clicked.
     * @param reactionKey The reaction key that was clicked.
     */
    void onEmojiReactionLongClick(@NonNull View view, int position, @NonNull BaseMessage message, @NonNull String reactionKey);
}
