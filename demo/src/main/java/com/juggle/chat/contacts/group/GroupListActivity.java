package com.juggle.chat.contacts.group;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.jet.im.kit.activities.BaseActivity;
import com.juggle.chat.R;

public class GroupListActivity extends BaseActivity {
    public static Intent newIntent(@NonNull Context context) {
        return new Intent(context, GroupListActivity.class);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.demo_activity);

        Fragment fragment = new GroupListFragment();
        FragmentManager manager = getSupportFragmentManager();
        manager.popBackStack();
        manager.beginTransaction().replace(R.id.fl_fragment_container, fragment).commit();
    }
}
