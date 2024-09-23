package com.jet.im.kit.fragments;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.RecyclerView;

public class ItemAnimator extends DefaultItemAnimator {
    @Override
    public boolean animateChange(@NonNull RecyclerView.ViewHolder oldHolder, @NonNull RecyclerView.ViewHolder newHolder, int fromX, int fromY, int toX, int toY) {
        super.animateChange(oldHolder, newHolder, fromX, fromY, toX, toY);
        oldHolder.itemView.setAlpha(0.0f);
        newHolder.itemView.setAlpha(1.0f);
        return true;
    }
}
