package com.jet.im.kit.modules.components;

import android.content.Context;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.view.ContextThemeWrapper;
import androidx.core.widget.NestedScrollView;

import com.sendbird.android.channel.GroupChannel;
import com.jet.im.kit.R;
import com.jet.im.kit.interfaces.OnMenuItemClickListener;
import com.jet.im.kit.internal.ui.widgets.SingleMenuItemView;

/**
 * This class creates and performs a view corresponding the moderation list area in Sendbird UIKit.
 *
 * since 3.0.0
 */
public class ModerationListComponent {

    /**
     * Represents all moderation menus.
     *
     * since 1.2.0
     */
    public enum ModerationMenu {
        /**
         * Menu to administrate operators
         */
        OPERATORS,
        /**
         * Menu to administrate muted members
         */
        MUTED_MEMBERS,
        /**
         * Menu to administrate banned members
         */
        BANNED_MEMBERS,
        /**
         * Menu to freeze or unfreeze the channel
         */
        FREEZE_CHANNEL
    }

    @NonNull
    private final Params params;
    @Nullable
    private NestedScrollView nestedScrollView;

    @Nullable
    private OnMenuItemClickListener<ModerationMenu, Void> menuItemClickListener;

    @SuppressWarnings("FieldCanBeLocal")
    @Nullable
    private SingleMenuItemView operators;
    @Nullable
    private SingleMenuItemView mutedMembers;
    @SuppressWarnings("FieldCanBeLocal")
    @Nullable
    private SingleMenuItemView bannedMembers;
    @Nullable
    private SingleMenuItemView frozenState;

    /**
     * Constructor
     *
     * since 3.0.0
     */
    public ModerationListComponent() {
        this.params = new Params();
    }

    /**
     * Returns the nested scroll view used in this component.
     *
     * @return {@link NestedScrollView} that this component creates and performs by default
     * since 3.0.0
     */
    @Nullable
    protected NestedScrollView getNestedScrollView() {
        return this.nestedScrollView;
    }

    /**
     * Returns a collection of parameters applied to this component.
     *
     * @return {@code Params} applied to this component
     * since 3.0.0
     */
    @NonNull
    public Params getParams() {
        return params;
    }

    /**
     * Returns the view created by {@link #onCreateView(Context, LayoutInflater, ViewGroup, Bundle)}.
     *
     * @return the topmost view containing this view
     * since 3.0.0
     */
    @Nullable
    public View getRootView() {
        return this.nestedScrollView;
    }

    /**
     * Called after the component was created to make views.
     * <p><b>If this function is used override, {@link #getRootView()} must also be override.</b></p>
     *
     * @param context  The {@code Context} this component is currently associated with
     * @param inflater The LayoutInflater object that can be used to inflate any views in the component
     * @param parent   The ViewGroup into which the new View will be added
     * @param args     The arguments supplied when the component was instantiated, if any
     * @return Return the View for the UI.
     * since 3.0.0
     */
    @NonNull
    public View onCreateView(@NonNull Context context, @NonNull LayoutInflater inflater, @NonNull ViewGroup parent, @Nullable Bundle args) {
        if (args != null) params.apply(context, args);
        final TypedValue values = new TypedValue();
        context.getTheme().resolveAttribute(R.attr.sb_component_moderation_list, values, true);
        final Context listThemeContext = new ContextThemeWrapper(context, values.resourceId);

        final NestedScrollView listView = new NestedScrollView(listThemeContext);
        final LinearLayout innerLayout = new LinearLayout(listThemeContext);
        innerLayout.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        innerLayout.setOrientation(LinearLayout.VERTICAL);
        listView.addView(innerLayout);
        final int height = listThemeContext.getResources().getDimensionPixelSize(R.dimen.sb_size_56);
        final LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, height);

        this.operators = new SingleMenuItemView(listThemeContext);
        this.mutedMembers = new SingleMenuItemView(listThemeContext);
        this.bannedMembers = new SingleMenuItemView(listThemeContext);
        this.frozenState = new SingleMenuItemView(listThemeContext);

        this.operators.setMenuType(SingleMenuItemView.Type.NEXT);
        this.operators.setIcon(R.drawable.icon_operator);
        this.operators.setName(listThemeContext.getString(R.string.sb_text_menu_operators));
        this.operators.setNextActionDrawable(R.drawable.icon_chevron_right);
        this.operators.setLayoutParams(layoutParams);
        this.operators.setOnClickListener(v -> onMenuItemClicked(v, ModerationMenu.OPERATORS));

        this.mutedMembers.setMenuType(SingleMenuItemView.Type.NEXT);
        this.mutedMembers.setIcon(R.drawable.icon_mute);
        this.mutedMembers.setName(listThemeContext.getString(R.string.sb_text_menu_muted_members));
        this.mutedMembers.setNextActionDrawable(R.drawable.icon_chevron_right);
        this.mutedMembers.setVisibility(View.GONE);
        this.mutedMembers.setLayoutParams(layoutParams);
        this.mutedMembers.setOnClickListener(v -> onMenuItemClicked(v, ModerationMenu.MUTED_MEMBERS));

        this.bannedMembers.setMenuType(SingleMenuItemView.Type.NEXT);
        this.bannedMembers.setIcon(R.drawable.icon_ban);
        this.bannedMembers.setName(listThemeContext.getString(R.string.sb_text_menu_banned_users));
        this.bannedMembers.setNextActionDrawable(R.drawable.icon_chevron_right);
        this.bannedMembers.setLayoutParams(layoutParams);
        this.bannedMembers.setOnClickListener(v -> onMenuItemClicked(v, ModerationMenu.BANNED_MEMBERS));

        this.frozenState.setMenuType(SingleMenuItemView.Type.SWITCH);
        this.frozenState.setIcon(R.drawable.icon_freeze);
        this.frozenState.setName(listThemeContext.getString(R.string.sb_text_menu_freeze_channel));
        this.frozenState.setNextActionDrawable(R.drawable.icon_chevron_right);
        this.frozenState.setVisibility(View.GONE);
        this.frozenState.setLayoutParams(layoutParams);
        this.frozenState.setOnClickListener(v -> onMenuItemClicked(v, ModerationMenu.FREEZE_CHANNEL));
        this.frozenState.setOnActionMenuClickListener(v -> onMenuItemClicked(v, ModerationMenu.FREEZE_CHANNEL));

        innerLayout.addView(operators);
        innerLayout.addView(mutedMembers);
        innerLayout.addView(bannedMembers);
        innerLayout.addView(frozenState);
        this.nestedScrollView = listView;
        return listView;
    }

    /**
     * Register a callback to be invoked when the item of the menu is clicked.
     *
     * @param menuItemClickListener The callback that will run
     * since 3.0.0
     */
    public void setOnMenuItemClickListener(@Nullable OnMenuItemClickListener<ModerationMenu, Void> menuItemClickListener) {
        this.menuItemClickListener = menuItemClickListener;
    }

    /**
     * Called when the item of the menu list is clicked.
     *
     * @param view The View clicked
     * @param menu The menu that the clicked item displays
     * since 3.0.0
     */
    protected void onMenuItemClicked(@NonNull View view, @NonNull ModerationMenu menu) {
        if (menuItemClickListener != null)
            menuItemClickListener.onMenuItemClicked(view, menu, null);
    }

    /**
     * Notifies this component that the channel data has changed.
     *
     * @param channel The latest group channel
     * since 3.0.0
     */
    public void notifyChannelChanged(@NonNull GroupChannel channel) {
        if (mutedMembers != null) {
            mutedMembers.setVisibility(channel.isBroadcast() ? View.GONE : View.VISIBLE);
        }
        if (frozenState != null) {
            frozenState.setChecked(channel.isFrozen());
            frozenState.setVisibility(channel.isBroadcast() ? View.GONE : View.VISIBLE);
        }
    }

    /**
     * A collection of parameters, which can be applied to a default View. The values of params are not dynamically applied at runtime.
     * Params cannot be created directly, and it is automatically created together when components are created.
     * <p>Since the onCreateView configuring View uses the values of the set Params, we recommend that you set up for Params before the onCreateView is called.</p>
     *
     * @see #getParams()
     * since 3.0.0
     */
    public static class Params {
        protected Params() {
        }

        /**
         * Apply data that matches keys mapped to Params' properties.
         *
         * @param context The {@code Context} this component is currently associated with
         * @param args    The sets of arguments to apply at Params.
         * @return This Params object that applied with given data.
         * since 3.0.0
         */
        @NonNull
        protected Params apply(@NonNull Context context, @NonNull Bundle args) {
            return this;
        }
    }
}
