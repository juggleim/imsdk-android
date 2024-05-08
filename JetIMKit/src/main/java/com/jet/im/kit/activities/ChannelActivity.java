package com.jet.im.kit.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StyleRes;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.jet.im.kit.R;
import com.jet.im.kit.SendbirdUIKit;
import com.jet.im.kit.activities.MessageThreadActivity;
import com.jet.im.kit.consts.StringSet;

/**
 * Activity displays a list of messages from a channel.
 */
public class ChannelActivity extends AppCompatActivity {

    /**
     * Create an intent for a {@link ChannelActivity}.
     *
     * @param context A Context of the application package implementing this class.
     * @param channelUrl the url of the channel will be implemented.
     * @return ChannelActivity Intent
     */
    @NonNull
    public static Intent newIntent(@NonNull Context context, @NonNull String channelUrl) {
        return newIntentFromCustomActivity(context, ChannelActivity.class, channelUrl);
    }

    /**
     * Create an intent for a {@link ChannelActivity}.
     * This intent will redirect to {@link MessageThreadActivity} if message has parent.
     *
     * @param context A Context of the application package implementing this class.
     * @param channelUrl the url of the channel will be implemented.
     * @param messageId  The message id of the anchor that redirects message thread.
     * @return ChannelActivity Intent
     * since 3.3.0
     */
    @NonNull
    public static Intent newRedirectToMessageThreadIntent(@NonNull Context context, @NonNull String channelUrl, long messageId) {
        final Intent intent = newIntentFromCustomActivity(context, ChannelActivity.class, channelUrl);
        intent.putExtra(StringSet.KEY_ANCHOR_MESSAGE_ID, messageId);
        return intent;
    }

    /**
     * Create an intent for a custom activity. The custom activity must inherit {@link ChannelActivity}.
     *
     * @param context A Context of the application package implementing this class.
     * @param cls The activity class that is to be used for the intent.
     * @param channelUrl the url of the channel will be implemented.
     * @return Returns a newly created Intent that can be used to launch the activity.
     * since 1.1.2
     */
    @NonNull
    public static Intent newIntentFromCustomActivity(@NonNull Context context, @NonNull Class<? extends ChannelActivity> cls, @NonNull String channelUrl) {
        return new IntentBuilder(context, cls, channelUrl).build();
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        int themeResId = getIntent().getIntExtra(StringSet.KEY_THEME_RES_ID, SendbirdUIKit.getDefaultThemeMode().getResId());
        setTheme(themeResId);
        setContentView(R.layout.sb_activity);

        Fragment fragment = createFragment();
        FragmentManager manager = getSupportFragmentManager();
        manager.popBackStack();
        manager.beginTransaction()
            .replace(R.id.sb_fragment_container, fragment)
            .commit();
    }

    /**
     * It will be called when the {@link ChannelActivity} is being created.
     * The data contained in Intent is delivered to Fragment's Bundle.
     *
     * @return {@link com.jet.im.kit.fragments.ChannelFragment}
     * since 3.0.0
     */
    @NonNull
    protected Fragment createFragment() {
        final Intent intent = getIntent();
        if (intent != null) {
            if (intent.hasExtra(StringSet.KEY_FROM_SEARCH_RESULT)) {
                // If the request comes from Search page, the channel settings page should hide on the header view.
                // this is a internal behavior.
                boolean useRightButton = !intent.getBooleanExtra(StringSet.KEY_FROM_SEARCH_RESULT, true);
                intent.putExtra(StringSet.KEY_USE_HEADER_RIGHT_BUTTON, useRightButton);
                intent.putExtra(StringSet.KEY_TRY_ANIMATE_WHEN_MESSAGE_LOADED, true);
            }
            if ((intent.getFlags() & Intent.FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY) == Intent.FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY) {
                getIntent().removeExtra(StringSet.KEY_ANCHOR_MESSAGE_ID);
            }
            if (intent.hasExtra(StringSet.KEY_ANCHOR_MESSAGE_ID)) {
                final long messageId = intent.getLongExtra(StringSet.KEY_ANCHOR_MESSAGE_ID, 0L);
                if (messageId <= 0) intent.removeExtra(StringSet.KEY_ANCHOR_MESSAGE_ID);
            }
        }
        final Bundle args = intent != null && intent.getExtras() != null ? intent.getExtras() : new Bundle();
        return SendbirdUIKit.getFragmentFactory().newChannelFragment(args.getString(StringSet.KEY_CHANNEL_URL, ""), args);
    }

    /**
     * This builder makes {@link Intent} for ChannelActivity.
     *
     * since 2.1.0
     */
    public static class IntentBuilder {
        @NonNull
        private final Context context;
        @NonNull
        private final String channelUrl;
        private long startingPoint = Long.MAX_VALUE;

        @NonNull
        private final Class<? extends ChannelActivity> customClass;

        @StyleRes
        private final int themeResId;

        /**
         * Create an intent for a {@link ChannelActivity}.
         *
         * @param context A Context of the application package implementing this class.
         * @param channelUrl The url of the channel will be implemented.
         * since 2.1.0
         */
        public IntentBuilder(@NonNull Context context, @NonNull String channelUrl) {
            this(context, ChannelActivity.class, channelUrl);
        }

        /**
         * Create an intent for a {@link ChannelActivity}.
         *
         * @param context A Context of the application package implementing this class.
         * @param customClass The activity class that is to be used for the intent.
         * @param channelUrl The url of the channel will be implemented.
         * since 2.1.0
         */
        public IntentBuilder(@NonNull Context context, @NonNull Class<? extends ChannelActivity> customClass, @NonNull String channelUrl) {
            this(context, customClass, channelUrl, SendbirdUIKit.getDefaultThemeMode().getResId());
        }

        /**
         * Create an intent for a {@link ChannelActivity}.
         *
         * @param context A Context of the application package implementing this class.
         * @param customClass The activity class that is to be used for the intent.
         * @param channelUrl The url of the channel will be implemented.
         * @param themeResId the resource identifier for custom theme.
         * since 3.5.6
         */
        public IntentBuilder(@NonNull Context context, @NonNull Class<? extends ChannelActivity> customClass, @NonNull String channelUrl, @StyleRes int themeResId) {
            this.context = context;
            this.channelUrl = channelUrl;
            this.customClass = customClass;
            this.themeResId = themeResId;
        }

        /**
         * Sets the timestamp to load the messages with.
         *
         * @param startingPoint A timestamp to load initially.
         * @return This Builder object to allow for chaining of calls to set methods.
         * since 2.1.0
         */
        @NonNull
        public IntentBuilder setStartingPoint(long startingPoint) {
            this.startingPoint = startingPoint;
            return this;
        }

        /**
         * Creates an {@link Intent} with the arguments supplied to this builder.
         *
         * @return The ChannelActivity {@link Intent} applied to the {@link Bundle}.
         * since 2.1.0
         */
        @NonNull
        public Intent build() {
            Intent intent = new Intent(context, customClass);
            intent.putExtra(StringSet.KEY_CHANNEL_URL, channelUrl);
            intent.putExtra(StringSet.KEY_STARTING_POINT, startingPoint);
            intent.putExtra(StringSet.KEY_THEME_RES_ID, themeResId);
            return intent;
        }
    }
}
