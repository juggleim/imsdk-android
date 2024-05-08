package com.jet.im.kit.modules.components;

import android.content.Context;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.appcompat.view.ContextThemeWrapper;

import com.sendbird.android.SendbirdChat;
import com.sendbird.android.channel.OpenChannel;
import com.jet.im.kit.R;
import com.jet.im.kit.SendbirdUIKit;
import com.jet.im.kit.interfaces.OnItemClickListener;
import com.jet.im.kit.internal.ui.widgets.SingleMenuItemView;
import com.jet.im.kit.utils.ChannelUtils;

/**
 * This class creates and performs a view corresponding the open channel settings menu area in Sendbird UIKit.
 *
 * since 3.0.0
 */
public class OpenChannelSettingsMenuComponent {
    public enum Menu {
        /**
         * A menu of Moderations to users or members control.
         *
         * since 3.1.0
         */
        MODERATIONS,
        /**
         * A menu for viewing the participants of the current channel.
         */
        PARTICIPANTS,
        /**
         * A menu to delete the current channel.
         */
        DELETE_CHANNEL
    }

    @NonNull
    private final Params params;
    @Nullable
    private View menuView;

    @Nullable
    protected OnItemClickListener<Menu> menuClickListener;

    /**
     * Constructor
     *
     * since 3.0.0
     */
    public OpenChannelSettingsMenuComponent() {
        this.params = new Params();
    }

    /**
     * Returns the view created by {@link #onCreateView(Context, LayoutInflater, ViewGroup, Bundle)}.
     *
     * @return the topmost view containing this view
     * since 3.0.0
     */
    @Nullable
    public View getRootView() {
        return this.menuView;
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
        context.getTheme().resolveAttribute(R.attr.sb_component_open_channel_settings_menu, values, true);
        final Context menuThemeContext = new ContextThemeWrapper(context, values.resourceId);
        final LayoutInflater menuInflater = inflater.cloneInContext(menuThemeContext);

        final View view = menuInflater.inflate(R.layout.sb_view_open_channel_settings_menu, parent, false);
        final SingleMenuItemView moderationsItemView = view.findViewById(R.id.moderations);
        final SingleMenuItemView participantsItemView = view.findViewById(R.id.participants);
        final SingleMenuItemView deleteItemView = view.findViewById(R.id.delete);

        moderationsItemView.setName(context.getString(R.string.sb_text_channel_settings_moderations));
        moderationsItemView.setMenuType(SingleMenuItemView.Type.NEXT);
        moderationsItemView.setIcon(R.drawable.icon_moderations);
        moderationsItemView.setVisibility(View.GONE);

        participantsItemView.setName(context.getString(R.string.sb_text_header_participants));
        participantsItemView.setMenuType(SingleMenuItemView.Type.NEXT);
        participantsItemView.setIcon(R.drawable.icon_members);
        deleteItemView.setName(context.getString(R.string.sb_text_channel_settings_delete_channel));
        deleteItemView.setMenuType(SingleMenuItemView.Type.NONE);
        deleteItemView.setIcon(R.drawable.icon_delete);
        deleteItemView.setIconTint(AppCompatResources.getColorStateList(context, SendbirdUIKit.isDarkMode() ? R.color.error_200 : R.color.error_300));

        moderationsItemView.setOnClickListener(v -> onMenuClicked(v, Menu.MODERATIONS));
        participantsItemView.setOnClickListener(v -> onMenuClicked(v, Menu.PARTICIPANTS));
        deleteItemView.setOnClickListener(v -> onMenuClicked(v, Menu.DELETE_CHANNEL));
        this.menuView = view;
        return view;
    }

    /**
     * Notifies this component that the channel data has changed.
     *
     * @param channel The latest open channel
     * since 3.0.0
     */
    public void notifyChannelChanged(@NonNull OpenChannel channel) {
        if (this.menuView == null) return;

        final SingleMenuItemView moderationsItemView = menuView.findViewById(R.id.moderations);
        moderationsItemView.setVisibility(channel.isOperator(SendbirdChat.getCurrentUser()) ? View.VISIBLE : View.GONE);

        SingleMenuItemView participantsItemView = menuView.findViewById(R.id.participants);
        participantsItemView.setDescription(ChannelUtils.makeMemberCountText(channel.getParticipantCount()).toString());
    }

    /**
     * Register a callback to be invoked when the item of the menu is clicked.
     *
     * @param menuClickListener The callback that will run
     * since 3.0.0
     */
    public void setOnMenuClickListener(@NonNull OnItemClickListener<Menu> menuClickListener) {
        this.menuClickListener = menuClickListener;
    }

    /**
     * Called when the item of the menu list is clicked.
     *
     * @param view The View clicked
     * @param menu The menu that the clicked item displays
     * since 3.0.0
     */
    protected void onMenuClicked(@NonNull View view, @NonNull Menu menu) {
        if (this.menuClickListener != null) this.menuClickListener.onItemClick(view, 0, menu);
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
        /**
         * Constructor
         *
         * since 3.0.0
         */
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
