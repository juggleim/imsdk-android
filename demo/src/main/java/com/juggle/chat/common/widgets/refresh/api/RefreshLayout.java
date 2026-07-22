package com.juggle.chat.common.widgets.refresh.api;

import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Interpolator;

import androidx.annotation.ColorInt;
import androidx.annotation.ColorRes;
import androidx.annotation.FloatRange;
import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.juggle.chat.common.widgets.refresh.constant.RefreshState;
import com.juggle.chat.common.widgets.refresh.listener.OnLoadMoreListener;
import com.juggle.chat.common.widgets.refresh.listener.OnMultiListener;
import com.juggle.chat.common.widgets.refresh.listener.OnRefreshListener;
import com.juggle.chat.common.widgets.refresh.listener.OnRefreshLoadMoreListener;
import com.juggle.chat.common.widgets.refresh.listener.ScrollBoundaryDecider;


/** RefreshLayout interface of the refresh layout Created by scwang on 2017/5/26. */
@SuppressWarnings({"UnusedReturnValue", "SameParameterValue", "unused"})
public interface RefreshLayout {

    /**
     * Set the Footer's height. Set Footer height
     *
     * @param dp Density-independent Pixels （pxpx2dp）
     * @return RefreshLayout
     */
    RefreshLayout setFooterHeight(float dp);

    /**
     * Set Footer height
     *
     * @param px
     * @return RefreshLayout
     */
    RefreshLayout setFooterHeightPx(int px);

    /**
     * Set the Header's height. Set Header height
     *
     * @param dp Density-independent Pixels （pxpx2dp）
     * @return RefreshLayout
     */
    RefreshLayout setHeaderHeight(float dp);

    /**
     * Set Header height
     *
     * @param px
     * @return RefreshLayout
     */
    RefreshLayout setHeaderHeightPx(int px);

    /**
     * Set the Header's start offset（see srlHeaderInsetStart in the RepastPracticeActivity XML in
     * demo-app for the practical application）. Set Header （method demo-app
     * RepastPracticeActivity xml  srlHeaderInsetStart）
     *
     * @param dp Density-independent Pixels （pxpx2dp）
     * @return RefreshLayout
     */
    RefreshLayout setHeaderInsetStart(float dp);

    /**
     * Set the Header's start offset（see srlHeaderInsetStart in the RepastPracticeActivity XML in
     * demo-app for the practical application）. Set Header （method demo-app
     * RepastPracticeActivity xml  srlHeaderInsetStart）
     *
     * @param px
     * @return RefreshLayout
     */
    RefreshLayout setHeaderInsetStartPx(int px);

    /**
     * Set the Footer's start offset. Set Footer （ setHeaderInsetStart ）
     *
     * @see RefreshLayout#setHeaderInsetStart(float)
     * @param dp Density-independent Pixels （pxpx2dp）
     * @return RefreshLayout
     */
    RefreshLayout setFooterInsetStart(float dp);

    /**
     * Set the Footer's start offset. Set Footer （ setFooterInsetStartPx ）
     *
     * @param px
     * @return RefreshLayout
     */
    RefreshLayout setFooterInsetStartPx(int px);

    /**
     * Set the damping effect. Showdragheight/dragheight （Default0.5，）
     *
     * @param rate ratio = (The drag height of the view)/(The actual drag height of the finger)  =
     *     Viewdragheight / fingerdragheight
     * @return RefreshLayout
     */
    RefreshLayout setDragRate(@FloatRange(from = 0, to = 1) float rate);

    /**
     * Set the ratio of the maximum height to drag header. Setpull-downheightHeaderheight（pull-downheight）
     *
     * @param rate ratio = (the maximum height to drag header)/(the height of header)  = pull-downheight /
     *     Headerheight
     * @return RefreshLayout
     */
    RefreshLayout setHeaderMaxDragRate(@FloatRange(from = 1, to = 10) float rate);

    /**
     * Set the ratio of the maximum height to drag footer. Setpull-upheightFooterheight（pull-upheight）
     *
     * @param rate ratio = (the maximum height to drag footer)/(the height of footer)  = pull-downheight /
     *     Footerheight
     * @return RefreshLayout
     */
    RefreshLayout setFooterMaxDragRate(@FloatRange(from = 1, to = 10) float rate);

    /**
     * Set the ratio at which the refresh is triggered. Set triggerRefresh  HeaderHeight
     *
     * @param rate triggerRefresh  HeaderHeight
     * @return RefreshLayout
     */
    RefreshLayout setHeaderTriggerRate(@FloatRange(from = 0, to = 1.0) float rate);

    /**
     * Set the ratio at which the load more is triggered. Set triggerLoad  FooterHeight
     *
     * @param rate triggerLoad  FooterHeight
     * @return RefreshLayout
     */
    RefreshLayout setFooterTriggerRate(@FloatRange(from = 0, to = 1.0) float rate);

    /**
     * Set the rebound interpolator. SetreboundShow [reboundanimation,endanimation]
     *
     * @param interpolator animation
     * @return RefreshLayout
     */
    RefreshLayout setReboundInterpolator(@NonNull Interpolator interpolator);

    /**
     * Set the duration of the rebound animation. Setreboundanimation [reboundanimation,endanimation]
     *
     * @param duration
     * @return RefreshLayout
     */
    RefreshLayout setReboundDuration(int duration);

    /**
     * Set the footer of RefreshLayout. Set Footer
     *
     * @param footer RefreshFooter Refresh
     * @return RefreshLayout
     */
    RefreshLayout setRefreshFooter(@NonNull RefreshFooter footer);

    /**
     * Set the footer of RefreshLayout. Set Footer
     *
     * @param footer RefreshFooter Refresh
     * @param width the width in px, can use MATCH_PARENT and WRAP_CONTENT. width  MATCH_PARENT,
     *     WRAP_CONTENT
     * @param height the height in px, can use MATCH_PARENT and WRAP_CONTENT. height  MATCH_PARENT,
     *     WRAP_CONTENT
     * @return RefreshLayout
     */
    RefreshLayout setRefreshFooter(@NonNull RefreshFooter footer, int width, int height);

    /**
     * Set the header of RefreshLayout. Set Header
     *
     * @param header RefreshHeader Refresh
     * @return RefreshLayout
     */
    RefreshLayout setRefreshHeader(@NonNull RefreshHeader header);

    /**
     * Set the header of RefreshLayout. Set Header
     *
     * @param header RefreshHeader Refresh
     * @param width the width in px, can use MATCH_PARENT and WRAP_CONTENT. width  MATCH_PARENT,
     *     WRAP_CONTENT
     * @param height the height in px, can use MATCH_PARENT and WRAP_CONTENT. height  MATCH_PARENT,
     *     WRAP_CONTENT
     * @return RefreshLayout
     */
    RefreshLayout setRefreshHeader(@NonNull RefreshHeader header, int width, int height);

    /**
     * Set the content of RefreshLayout（Suitable for non-XML pages, not suitable for replacing empty
     * layouts）。 Set Content（XMLPage，Layout）
     *
     * @param content View ContentView
     * @return RefreshLayout
     */
    RefreshLayout setRefreshContent(@NonNull View content);

    /**
     * Set the content of RefreshLayout（Suitable for non-XML pages, not suitable for replacing empty
     * layouts）. Set Content（XMLPage，Layout）
     *
     * @param content View ContentView
     * @param width the width in px, can use MATCH_PARENT and WRAP_CONTENT. width  MATCH_PARENT,
     *     WRAP_CONTENT
     * @param height the height in px, can use MATCH_PARENT and WRAP_CONTENT. height  MATCH_PARENT,
     *     WRAP_CONTENT
     * @return RefreshLayout
     */
    RefreshLayout setRefreshContent(@NonNull View content, int width, int height);

    /**
     * Whether to enable pull-down refresh (enabled by default). pull-downRefresh（Default）
     *
     * @param enabled
     * @return RefreshLayout
     */
    RefreshLayout setEnableRefresh(boolean enabled);

    /**
     * Set whether to enable pull-up loading more (enabled by default). Setpull-upLoad（Default）
     *
     * @param enabled
     * @return RefreshLayout
     */
    RefreshLayout setEnableLoadMore(boolean enabled);

    /**
     * Sets whether to listen for the list to trigger a load event when scrolling to the bottom
     * (default true). SetListenerlistScrollBottomtriggerLoad（Defaulttrue）
     *
     * @param enabled
     * @return RefreshLayout
     */
    RefreshLayout setEnableAutoLoadMore(boolean enabled);

    /**
     * Set whether to pull down the content while pulling down the header. Setpull-down Header pull-downContent
     *
     * @param enabled
     * @return RefreshLayout
     */
    RefreshLayout setEnableHeaderTranslationContent(boolean enabled);

    /**
     * Set whether to pull up the content while pulling up the header. Setpull-up Footer pull-upContent
     *
     * @param enabled
     * @return RefreshLayout
     */
    RefreshLayout setEnableFooterTranslationContent(boolean enabled);

    /**
     * Set whether to enable cross-border rebound function. Setrebound
     *
     * @param enabled
     * @return RefreshLayout
     */
    RefreshLayout setEnableOverScrollBounce(boolean enabled);

    /**
     * Set whether to enable the pure scroll mode. SetScrollmode
     *
     * @param enabled
     * @return RefreshLayout
     */
    RefreshLayout setEnablePureScrollMode(boolean enabled);

    /**
     * Set whether to scroll the content to display new data after loading more complete.
     * SetLoadcompleteScrollContentShow
     *
     * @param enabled
     * @return RefreshLayout
     */
    RefreshLayout setEnableScrollContentWhenLoaded(boolean enabled);

    /**
     * Set whether to scroll the content to display new data after the refresh is complete.
     * RefreshcompleteScrollContentShow
     *
     * @param enabled
     * @return RefreshLayout
     */
    RefreshLayout setEnableScrollContentWhenRefreshed(boolean enabled);

    /**
     * Set whether to pull up and load more when the content is not full of one page.
     * SetContent，pull-upLoad
     *
     * @param enabled
     * @return RefreshLayout
     */
    RefreshLayout setEnableLoadMoreWhenContentNotFull(boolean enabled);

    /**
     * Set whether to enable cross-border drag (imitation iphone effect). Setdrag（）
     *
     * @param enabled
     * @return RefreshLayout
     */
    RefreshLayout setEnableOverScrollDrag(boolean enabled);

    /**
     * Set whether or not Footer follows the content after there is no more data. Set
     * Footer Content
     *
     * @param enabled
     * @return RefreshLayout
     */
    RefreshLayout setEnableFooterFollowWhenNoMoreData(boolean enabled);

    /**
     * Set whether to clip header when the Header is in the FixedBehind state. Set Header
     * FixedBehind state Header
     *
     * @param enabled
     * @return RefreshLayout
     */
    RefreshLayout setEnableClipHeaderWhenFixedBehind(boolean enabled);

    /**
     * Set whether to clip footer when the Footer is in the FixedBehind state. Set Footer
     * FixedBehind state Footer
     *
     * @param enabled
     * @return RefreshLayout
     */
    RefreshLayout setEnableClipFooterWhenFixedBehind(boolean enabled);

    /**
     * Setting whether nesting scrolling is enabled (default off + smart on).
     * SetnestedScrollFeature（Default+）
     *
     * @param enabled
     * @return RefreshLayout
     */
    RefreshLayout setEnableNestedScroll(boolean enabled);
    /**
     * Set Header ViewId， Footer ScrollScroll
     *
     * @param id ViewId
     * @return RefreshLayout
     */
    RefreshLayout setFixedHeaderViewId(@IdRes int id);
    /**
     * Set Footer ViewId， Header ScrollScroll
     *
     * @param id BottomViewId
     * @return RefreshLayout
     */
    RefreshLayout setFixedFooterViewId(@IdRes int id);
    /**
     * Set Header Scroll，ScrollViewId，DefaultContentView
     *
     * @param id ViewId
     * @return RefreshLayout
     */
    RefreshLayout setHeaderTranslationViewId(@IdRes int id);
    /**
     * Set Footer Scroll，ScrollViewId，DefaultContentView
     *
     * @param id ViewId
     * @return RefreshLayout
     */
    RefreshLayout setFooterTranslationViewId(@IdRes int id);
    /**
     * Set whether to enable the action content view when refreshing. SetRefreshContentView
     *
     * @param disable
     * @return RefreshLayout
     */
    RefreshLayout setDisableContentWhenRefresh(boolean disable);

    /**
     * Set whether to enable the action content view when loading. SetLoadContentView
     *
     * @param disable
     * @return RefreshLayout
     */
    RefreshLayout setDisableContentWhenLoading(boolean disable);

    /**
     * Set refresh listener separately. SetRefreshListener
     *
     * @param listener OnRefreshListener RefreshListener
     * @return RefreshLayout
     */
    RefreshLayout setOnRefreshListener(OnRefreshListener listener);

    /**
     * Set load more listener separately. SetLoadListener
     *
     * @param listener OnLoadMoreListener LoadListener
     * @return RefreshLayout
     */
    RefreshLayout setOnLoadMoreListener(OnLoadMoreListener listener);

    /**
     * Set refresh and load listeners at the same time. SetRefreshLoadListener
     *
     * @param listener OnRefreshLoadMoreListener RefreshLoadListener
     * @return RefreshLayout
     */
    RefreshLayout setOnRefreshLoadMoreListener(OnRefreshLoadMoreListener listener);

    /**
     *
     * @param listener OnMultiPurposeListener FeatureListener
     * @return RefreshLayout
     */
    RefreshLayout setOnMultiListener(OnMultiListener listener);

    RefreshLayout setScrollBoundaryDecider(ScrollBoundaryDecider boundary);

    /**
     * Set theme color int (primaryColor and accentColor). Setcolor
     *
     * @param primaryColors ColorInt color
     * @return RefreshLayout
     */
    RefreshLayout setPrimaryColors(@ColorInt int... primaryColors);

    /**
     * Set theme color id (primaryColor and accentColor). Setcolor
     *
     * @param primaryColorId ColorRes colorID
     * @return RefreshLayout
     */
    RefreshLayout setPrimaryColorsId(@ColorRes int... primaryColorId);

    /**
     * finish refresh. completeRefresh
     *
     * @return RefreshLayout
     */
    RefreshLayout finishRefresh();

    /**
     * finish refresh. completeRefresh
     *
     * @param delayed start
     * @return RefreshLayout
     */
    RefreshLayout finishRefresh(int delayed);

    /**
     * finish refresh. completeLoad
     *
     * @param success SuccessRefresh （updatetime）
     * @return RefreshLayout
     */
    RefreshLayout finishRefresh(boolean success);

    /**
     * finish refresh. completeRefresh
     *
     * @param delayed start
     * @param success SuccessRefresh （updatetime）
     * @param noMoreData
     * @return RefreshLayout
     */
    RefreshLayout finishRefresh(int delayed, boolean success, Boolean noMoreData);

    /**
     * finish load more with no more data. completeRefresh
     *
     * @return RefreshLayout
     */
    RefreshLayout finishRefreshWithNoMoreData();

    /**
     * finish load more. completeLoad
     *
     * @return RefreshLayout
     */
    RefreshLayout finishLoadMore();

    /**
     * finish load more. completeLoad
     *
     * @param delayed start
     * @return RefreshLayout
     */
    RefreshLayout finishLoadMore(int delayed);

    /**
     * finish load more. completeLoad
     *
     * @param success Success
     * @return RefreshLayout
     */
    RefreshLayout finishLoadMore(boolean success);

    /**
     * finish load more. completeLoad
     *
     * @param delayed start
     * @param success Success
     * @param noMoreData
     * @return RefreshLayout
     */
    RefreshLayout finishLoadMore(int delayed, boolean success, boolean noMoreData);

    /**
     * finish load more with no more data. completeLoad
     *
     * @return RefreshLayout
     */
    RefreshLayout finishLoadMoreWithNoMoreData();

    /**
     * Close the Header or Footer, can't replace finishRefresh and finishLoadMore.  Header
     * Footer ： 1.closeHeaderOrFooter state header  footer 2.finishRefresh
     * finishLoadMore  Refresh  Load
     *
     * @return RefreshLayout
     */
    RefreshLayout closeHeaderOrFooter();

    /**
     * Restore the original state after finishLoadMoreWithNoMoreData. Setstate
     *
     * @param noMoreData
     * @return RefreshLayout method，stateanimationend use {@link
     *     RefreshLayout#resetNoMoreData()} use {@link RefreshLayout#finishRefreshWithNoMoreData()}
     *     use {@link RefreshLayout#finishLoadMoreWithNoMoreData()}
     */
    RefreshLayout setNoMoreData(boolean noMoreData);

    /**
     * Restore the original state after finishLoadMoreWithNoMoreData. state
     *
     * @return RefreshLayout
     */
    RefreshLayout resetNoMoreData();

    /**
     * Get header of RefreshLayout Get Header
     *
     * @return RefreshLayout
     */
    @Nullable
    RefreshHeader getRefreshHeader();

    /**
     * Get footer of RefreshLayout Get Footer
     *
     * @return RefreshLayout
     */
    @Nullable
    RefreshFooter getRefreshFooter();

    /**
     * Get the current state of RefreshLayout Getstate
     *
     * @return RefreshLayout
     */
    @NonNull
    RefreshState getState();

    /**
     * Get the ViewGroup of RefreshLayout GetLayoutView
     *
     * @return ViewGroup
     */
    @NonNull
    ViewGroup getLayout();

    /**
     * Display refresh animation and trigger refresh event. ShowRefreshanimationtriggerRefresh
     *
     * @return true or false, Status non-compliance will fail. Success（stateFailed）
     */
    boolean autoRefresh();

    /**
     * Display refresh animation and trigger refresh event, Delayed start. ShowRefreshanimationtriggerRefresh，
     *
     * @param delayed start
     * @return true or false, Status non-compliance will fail. Success（stateFailed）
     */
    boolean autoRefresh(int delayed);

    /**
     * Display refresh animation without triggering events. ShowRefreshanimation，trigger
     *
     * @return true or false, Status non-compliance will fail. Success（stateFailed）
     */
    boolean autoRefreshAnimationOnly();

    /**
     * Display refresh animation, Multifunction. ShowRefreshanimationtriggerRefresh
     *
     * @param delayed start
     * @param duration animationtime
     * @param dragRate height
     * @param animationOnly animation only animation
     * @return true or false, Status non-compliance will fail. Success（stateFailed）
     */
    boolean autoRefresh(int delayed, int duration, float dragRate, boolean animationOnly);

    /**
     * Display load more animation and trigger load more event. ShowLoadanimationtriggerRefresh
     *
     * @return true or false, Status non-compliance will fail. Success（stateFailed）
     */
    boolean autoLoadMore();

    /**
     * Display load more animation and trigger load more event, Delayed start. ShowLoadanimationtriggerRefresh,
     *
     * @param delayed start
     * @return true or false, Status non-compliance will fail. Success（stateFailed）
     */
    boolean autoLoadMore(int delayed);

    /**
     * Display load more animation without triggering events. ShowLoadanimation，trigger
     *
     * @return true or false, Status non-compliance will fail. Success（stateFailed）
     */
    boolean autoLoadMoreAnimationOnly();

    /**
     * Display load more animation and trigger load more event, Delayed start. ShowLoadanimation, Feature
     *
     * @param delayed start
     * @param duration animationtime
     * @param dragRate height
     * @param animationOnly Showanimation，Callback
     * @return true or false, Status non-compliance will fail. Success（stateFailed）
     */
    boolean autoLoadMore(int delayed, int duration, float dragRate, boolean animationOnly);

    /**
     * Refresh
     *
     * @return RefreshLayout
     */
    boolean isRefreshing();

    /**
     * Load
     *
     * @return RefreshLayout
     */
    boolean isLoading();
}
