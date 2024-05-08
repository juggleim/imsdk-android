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
import com.jet.im.kit.consts.CreatableChannelType;
import com.jet.im.kit.consts.StringSet;

/**
 * Activity displays a list of users and provide a current user to create a channel.
 */
public class CreateChannelActivity extends AppCompatActivity {
    /**
     * Create an intent for a {@link CreateChannelActivity}.
     *
     * @param context A Context of the application package implementing this class.
     * @return CreateChannelActivity Intent.
     */
    @NonNull
    public static Intent newIntent(@NonNull Context context) {
        return newIntent(context, SendbirdUIKit.getDefaultThemeMode().getResId());
    }

    /**
     * Create an intent for a {@link CreateChannelActivity}.
     *
     * @param context    A Context of the application package implementing this class.
     * @param themeResId the resource identifier for custom theme.
     * @return CreateChannelActivity Intent.
     * since 3.5.6
     */
    @NonNull
    public static Intent newIntent(@NonNull Context context, @StyleRes int themeResId) {
        Intent intent = new Intent(context, CreateChannelActivity.class);
        intent.putExtra(StringSet.KEY_THEME_RES_ID, themeResId);
        return intent;
    }


    /**
     * Create an intent for a {@link CreateChannelActivity}.
     *
     * @param context A Context of the application package implementing this class.
     * @param type The creatable channel type. see the {@link CreatableChannelType}.
     * @return CreateChannelActivity Intent.
     *
     * since 1.2.0
     */
    @NonNull
    public static Intent newIntent(@NonNull Context context, @NonNull CreatableChannelType type) {
        return newIntentFromCustomActivity(context, CreateChannelActivity.class, type);
    }

    /**
     * Create an intent for a {@link CreateChannelActivity}.
     *
     * @param context    A Context of the application package implementing this class.
     * @param type The creatable channel type. see the {@link CreatableChannelType}.
     * @param themeResId the resource identifier for custom theme.
     * @return CreateChannelActivity Intent.
     * since 3.5.6
     */
    @NonNull
    public static Intent newIntent(@NonNull Context context, @NonNull CreatableChannelType type, @StyleRes int themeResId) {
        return newIntentFromCustomActivity(context, CreateChannelActivity.class, type, themeResId);
    }


    /**
     * Create an intent for a custom activity. The custom activity must inherit {@link CreateChannelActivity}.
     *
     * @param context A Context of the application package implementing this class.
     * @param cls The activity class that is to be used for the intent.
     * @param type The creatable channel type. see the {@link CreatableChannelType}.
     * @return Returns a newly created Intent that can be used to launch the activity.
     * since 1.2.0
     */
    @NonNull
    public static Intent newIntentFromCustomActivity(@NonNull Context context, @NonNull Class<? extends CreateChannelActivity> cls, @NonNull CreatableChannelType type) {
        return newIntentFromCustomActivity(context, cls, type, SendbirdUIKit.getDefaultThemeMode().getResId());
    }

    /**
     * Create an intent for a custom activity. The custom activity must inherit {@link CreateChannelActivity}.
     *
     * @param context    A Context of the application package implementing this class.
     * @param cls        The activity class that is to be used for the intent.
     * @param type The creatable channel type. see the {@link CreatableChannelType}.
     * @param themeResId the resource identifier for custom theme.
     * @return Returns a newly created Intent that can be used to launch the activity.
     * since 3.5.6
     */
    @NonNull
    public static Intent newIntentFromCustomActivity(@NonNull Context context, @NonNull Class<? extends CreateChannelActivity> cls, @NonNull CreatableChannelType type, @StyleRes int themeResId) {
        Intent intent = new Intent(context, cls);
        intent.putExtra(StringSet.KEY_SELECTED_CHANNEL_TYPE, type);
        intent.putExtra(StringSet.KEY_THEME_RES_ID, themeResId);
        return intent;
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
     * It will be called when the {@link CreateChannelActivity} is being created.
     * The data contained in Intent is delivered to Fragment's Bundle.
     *
     * @return {@link com.jet.im.kit.fragments.CreateChannelFragment}
     * since 3.0.0
     */
    @NonNull
    protected Fragment createFragment() {
        final Bundle args = getIntent() != null && getIntent().getExtras() != null ? getIntent().getExtras() : new Bundle();
        CreatableChannelType creatableChannelType = CreatableChannelType.Normal;
        if (args.containsKey(StringSet.KEY_SELECTED_CHANNEL_TYPE)) {
            creatableChannelType = (CreatableChannelType) args.getSerializable(StringSet.KEY_SELECTED_CHANNEL_TYPE);
            if (creatableChannelType == null) creatableChannelType = CreatableChannelType.Normal;
        }
        return SendbirdUIKit.getFragmentFactory().newCreateChannelFragment(creatableChannelType, args);
    }
}
