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
import com.jet.im.kit.consts.StringSet;

/**
 * Activity displays forms to need to create open channel.
 * since 3.2.0
 */
public class CreateOpenChannelActivity extends AppCompatActivity {

    /**
     * Create an intent for a {@link CreateOpenChannelActivity}.
     *
     * @param context    A Context of the application package implementing this class.
     * @return CreateOpenChannelActivity Intent.
     * since 3.2.0
     */
    @NonNull
    public static Intent newIntent(@NonNull Context context) {
        return newIntent(context, SendbirdUIKit.getDefaultThemeMode().getResId());
    }

    /**
     * Create an intent for a {@link CreateOpenChannelActivity}.
     *
     * @param context    A Context of the application package implementing this class.
     * @param themeResId the resource identifier for custom theme.
     * @return CreateOpenChannelActivity Intent.
     * since 3.5.6
     */
    @NonNull
    public static Intent newIntent(@NonNull Context context, @StyleRes int themeResId) {
        Intent intent = new Intent(context, CreateChannelActivity.class);
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
     * It will be called when the {@link CreateOpenChannelActivity} is being created.
     * The data contained in Intent is delivered to Fragment's Bundle.
     *
     * @return {@link com.jet.im.kit.fragments.CreateOpenChannelFragment}
     * since 3.2.0
     */
    @NonNull
    protected Fragment createFragment() {
        final Bundle args = getIntent() != null && getIntent().getExtras() != null ? getIntent().getExtras() : new Bundle();
        return SendbirdUIKit.getFragmentFactory().newCreateOpenChannelFragment(args);
    }
}
