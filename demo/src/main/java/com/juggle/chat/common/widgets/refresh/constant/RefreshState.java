package com.juggle.chat.common.widgets.refresh.constant;

/** Refreshstate */
@SuppressWarnings("unused")
public enum RefreshState {
    None(0, false, false, false, false, false),
    PullDownToRefresh(1, true, false, false, false, false),
    PullUpToLoad(2, true, false, false, false, false),
    PullDownCanceled(1, false, false, false, false, false),
    PullUpCanceled(2, false, false, false, false, false),
    ReleaseToRefresh(1, true, false, false, false, true),
    ReleaseToLoad(2, true, false, false, false, true),
    ReleaseToTwoLevel(1, true, false, false, true, true),
    TwoLevelReleased(1, false, false, false, true, false),
    RefreshReleased(1, false, false, false, false, false),
    LoadReleased(2, false, false, false, false, false),
    Refreshing(1, false, true, false, false, false),
    Loading(2, false, true, false, false, false),
    TwoLevel(1, false, true, false, true, false),
    RefreshFinish(1, false, false, true, false, false),
    LoadFinish(2, false, false, true, false, false),
    TwoLevelFinish(1, false, false, true, true, false);

    public final boolean isHeader;
    public final boolean isFooter;
    public final boolean isTwoLevel; // Refresh ReleaseToTwoLevel TwoLevelReleased TwoLevel
    public final boolean
            isDragging; // dragstate：PullDownToRefresh PullUpToLoad ReleaseToRefresh ReleaseToLoad
    // ReleaseToTwoLevel
    public final boolean isOpening; // Refreshstate：Refreshing Loading TwoLevel
    public final boolean isFinishing; // completestate：RefreshFinish LoadFinish TwoLevelFinish
    public final boolean
            isReleaseToOpening; // release ReleaseToRefresh ReleaseToLoad ReleaseToTwoLevel

    RefreshState(
            int role,
            boolean dragging,
            boolean opening,
            boolean finishing,
            boolean twoLevel,
            boolean releaseToOpening) {
        this.isHeader = role == 1;
        this.isFooter = role == 2;
        this.isDragging = dragging;
        this.isOpening = opening;
        this.isFinishing = finishing;
        this.isTwoLevel = twoLevel;
        this.isReleaseToOpening = releaseToOpening;
    }

    public RefreshState toFooter() {
        if (isHeader && !isTwoLevel) {
            return values()[ordinal() + 1];
        }
        return this;
    }

    public RefreshState toHeader() {
        if (isFooter && !isTwoLevel) {
            return values()[ordinal() - 1];
        }
        return this;
    }
}
