package com.juggle.chat.common.widgets.refresh.constant;

/** TopBottomcomponentdrag Created by scwang on 2017/5/26. */
@SuppressWarnings("DeprecatedIsStillUsed")
public class SpinnerStyle {

    public static final SpinnerStyle Translate = new SpinnerStyle(0, true, false);
    /**
     * Scale pull-down 【】（header） 【Layout】（layout）app ，  Header
     * 【Scale】【FixedBehind】 Custom 【】【】【】 Header
     *
     * @deprecated use {@link SpinnerStyle#FixedBehind}
     */
    @Deprecated public static final SpinnerStyle Scale = new SpinnerStyle(1, true, true);

    public static final SpinnerStyle FixedBehind = new SpinnerStyle(2, false, false);
    public static final SpinnerStyle FixedFront = new SpinnerStyle(3, true, false);
    public static final SpinnerStyle MatchLayout = new SpinnerStyle(4, true, false);

    public static final SpinnerStyle[] values =
            new SpinnerStyle[] {
                Translate, //         : HeaderViewheight，
                Scale, //             ：pull-down（HeaderViewheight），autotriggerOnDraw
                FixedBehind, //     ：HeaderViewheight，
                FixedFront, //      ：HeaderViewheight，
                MatchLayout // Layout        ：HeaderViewheight， RefreshLayout
            };

    public final int ordinal;
    public final boolean front;
    public final boolean scale;

    protected SpinnerStyle(int ordinal, boolean front, boolean scale) {
        this.ordinal = ordinal;
        this.front = front;
        this.scale = scale;
    }
}
