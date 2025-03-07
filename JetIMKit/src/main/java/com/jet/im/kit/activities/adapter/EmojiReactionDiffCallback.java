package com.jet.im.kit.activities.adapter;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;

import com.juggle.im.model.MessageReactionItem;
import com.sendbird.android.message.Reaction;

import java.util.List;

class EmojiReactionDiffCallback extends DiffUtil.Callback {
    @NonNull
    private final List<MessageReactionItem> oldReactionList;
    @NonNull
    private final List<MessageReactionItem> newReactionList;

    EmojiReactionDiffCallback(@NonNull List<MessageReactionItem> oldReactionList, @NonNull List<MessageReactionItem> newReactionList) {
        //todo reaction
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
        final MessageReactionItem oldReaction = oldReactionList.get(oldItemPosition);
        final MessageReactionItem newReaction = newReactionList.get(newItemPosition);

        return oldReaction.equals(newReaction);
    }

    @Override
    public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
        final MessageReactionItem oldReaction = oldReactionList.get(oldItemPosition);
        final MessageReactionItem newReaction = newReactionList.get(newItemPosition);

        if (!areItemsTheSame(oldItemPosition, newItemPosition)) {
            return false;
        }
        //todo reaction
        return true;
//        return oldReaction.getUserIds().equals(newReaction.getUserIds());
    }
}
