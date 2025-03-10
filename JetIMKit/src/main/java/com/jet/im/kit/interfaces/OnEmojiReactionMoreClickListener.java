package com.jet.im.kit.interfaces;

import android.view.View;

import androidx.annotation.NonNull;

import com.juggle.im.model.Message;
import com.juggle.im.model.MessageReaction;

public interface OnEmojiReactionMoreClickListener {
    void onEmojiReactionMoreClick(@NonNull View view, int position, @NonNull Message message, @NonNull MessageReaction reaction);
}
