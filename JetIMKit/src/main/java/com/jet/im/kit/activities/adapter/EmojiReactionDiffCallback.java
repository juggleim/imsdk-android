package com.jet.im.kit.activities.adapter;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;

import com.sendbird.android.message.Reaction;

import java.util.List;

class EmojiReactionDiffCallback extends DiffUtil.Callback {
    @NonNull
    private final List<Reaction> oldReactionList;
    @NonNull
    private final List<Reaction> newReactionList;

    EmojiReactionDiffCallback(@NonNull List<Reaction> oldReactionList, @NonNull List<Reaction> newReactionList) {
        this.oldReactionList = oldReactionList;
        this.newReactionList = newReactionList;
    }

    @Override
    public int getOldListSize() {
        return oldReactionList.size();
    }

    @Override
    public int getNewListSize() {
        return newReactionList.size();
    }

    @Override
    public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
        final Reaction oldReaction = oldReactionList.get(oldItemPosition);
        final Reaction newReaction = newReactionList.get(newItemPosition);

        return oldReaction.equals(newReaction);
    }

    @Override
    public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
        final Reaction oldReaction = oldReactionList.get(oldItemPosition);
        final Reaction newReaction = newReactionList.get(newItemPosition);

        if (!areItemsTheSame(oldItemPosition, newItemPosition)) {
            return false;
        }

        return oldReaction.getUserIds().equals(newReaction.getUserIds());
    }
}
