package com.jet.im.kit.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.transition.Slide;
import android.view.Gravity;
import android.view.Window;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StyleRes;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.sendbird.android.message.BaseMessage;
import com.jet.im.kit.R;
import com.jet.im.kit.SendbirdUIKit;
import com.jet.im.kit.consts.StringSet;

import java.util.Objects;

/**
 * Activity displays message thread from a parent message.
 *
 * since 3.3.0
 */
public class MessageThreadActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        getWindow().requestFeature(Window.FEATURE_CONTENT_TRANSITIONS);
        getWindow().setEnterTransition(new Slide(Gravity.END));
        getWindow().setExitTransition(new Slide(Gravity.START));

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
     * It will be called when the {@link MessageThreadActivity} is being created.
     * The data contained in Intent is delivered to Fragment's Bundle.
     *
     * @return {@link com.jet.im.kit.fragments.MessageThreadFragment}
     * since 3.3.0
     */
    @NonNull
    protected Fragment createFragment() {
        final Intent intent = getIntent();
        final Bundle args = intent != null && intent.getExtras() != null ? intent.getExtras() : new Bundle();
        return SendbirdUIKit.getFragmentFactory().newMessageThreadFragment(
            args.getString(StringSet.KEY_CHANNEL_URL, ""),
            Objects.requireNonNull(BaseMessage.buildFromSerializedData(args.getByteArray(StringSet.KEY_PARENT_MESSAGE))),
            args
        );
    }

    /**
     * This builder makes {@link Intent} for MessageThreadActivity.
     *
     * since 3.3.0
     */
    public static class IntentBuilder {
        @NonNull
        private final Context context;
        @NonNull
        private final String channelUrl;
        @NonNull
        private final BaseMessage parentMessage;
        private long startingPoint = 0;
        @NonNull
        private final Class<? extends MessageThreadActivity> customClass;

        @StyleRes
        private final int themeResId;

        /**
         * Create an intent for a {@link MessageThreadActivity}.
         *
         * @param context A Context of the application package implementing this class.
         * @param channelUrl The url of the channel will be implemented.
         * @param parentMessage The parent message of thread.
         * since 3.3.0
         */
        public IntentBuilder(@NonNull Context context, @NonNull String channelUrl, @NonNull BaseMessage parentMessage) {
            this(context, MessageThreadActivity.class, channelUrl, parentMessage);
        }

        /**
         * Create an intent for a {@link MessageThreadActivity}.
         *
         * @param context A Context of the application package implementing this class.
         * @param customClass The activity class that is to be used for the intent.
         * @param channelUrl The url of the channel will be implemented.
         * @param parentMessage The parent message of thread.
         * since 3.3.0
         */
        public IntentBuilder(@NonNull Context context, @NonNull Class<? extends MessageThreadActivity> customClass, @NonNull String channelUrl, @NonNull BaseMessage parentMessage) {
            this(context, customClass, channelUrl, parentMessage, SendbirdUIKit.getDefaultThemeMode().getResId());
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
        public IntentBuilder(@NonNull Context context, @NonNull Class<? extends MessageThreadActivity> customClass, @NonNull String channelUrl, @NonNull BaseMessage parentMessage, @StyleRes int themeResId) {
            this.context = context;
            this.channelUrl = channelUrl;
            this.customClass = customClass;
            this.parentMessage = parentMessage;
            this.themeResId = themeResId;
        }

        /**
         * Sets the timestamp to load the messages with.
         *
         * @param startingPoint A timestamp to load initially.
         * @return This Builder object to allow for chaining of calls to set methods.
         * since 3.3.0
         */
        @NonNull
        public IntentBuilder setStartingPoint(long startingPoint) {
            this.startingPoint = startingPoint;
            return this;
        }

        /**
         * Creates an {@link Intent} with the arguments supplied to this builder.
         *
         * @return The MessageThreadActivity {@link Intent} applied to the {@link Bundle}.
         * since 3.3.0
         */
        @NonNull
        public Intent build() {
            Intent intent = new Intent(context, customClass);
            intent.putExtra(StringSet.KEY_CHANNEL_URL, channelUrl);
            intent.putExtra(StringSet.KEY_PARENT_MESSAGE, this.parentMessage.serialize());
            intent.putExtra(StringSet.KEY_STARTING_POINT, startingPoint);
            intent.putExtra(StringSet.KEY_THEME_RES_ID, themeResId);
            return intent;
        }
    }
}
