package com.juggle.chat.contacts.add;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.jet.im.kit.activities.BaseActivity;
import com.juggle.chat.R;


/**
 * 创建群组页面
 *
 */
public class AddFriendListActivity extends BaseActivity {

    @NonNull
    public static Intent newIntent(@NonNull Context context) {
        Intent intent = new Intent(context, AddFriendListActivity.class);
        Bundle bundle = new Bundle();
        intent.putExtras(bundle);
        return intent;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.demo_activity);

        Fragment fragment = createFragment();
        FragmentManager manager = getSupportFragmentManager();
        manager.popBackStack();
        manager.beginTransaction().replace(R.id.fl_fragment_container, fragment).commit();
    }

    @NonNull
    protected Fragment createFragment() {
        return new AddFriendListFragment();
    }
}
