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

public class GroupMemberListActivity extends BaseActivity {
    private static final String GROUP_ID = "groupId";
    private static final String TYPE = "type";
    public static Intent newIntent(@NonNull Context context, String groupId, int type) {
        Intent intent = new Intent(context, GroupMemberListActivity.class);
        intent.putExtra(GROUP_ID, groupId);
        intent.putExtra(TYPE, type);
        return intent;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.demo_activity);

        String groupId = getIntent().getStringExtra(GROUP_ID);
        int type = getIntent().getIntExtra(TYPE, 0);
        Fragment fragment = new GroupMemberListFragment(groupId, type);
        FragmentManager manager = getSupportFragmentManager();
        manager.popBackStack();
        manager.beginTransaction().replace(R.id.fl_fragment_container, fragment).commit();
    }
}
