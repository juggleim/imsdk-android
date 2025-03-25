package com.juggle.chat.contacts.group;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.juggle.chat.R;

public class GroupMemberListActivity extends AppCompatActivity {
    private static final String GROUP_ID = "groupId";
    public static Intent newIntent(@NonNull Context context, String groupId) {
        Intent intent = new Intent(context, GroupMemberListActivity.class);
        intent.putExtra(GROUP_ID, groupId);
        return intent;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.demo_activity);

        String groupId = getIntent().getStringExtra(GROUP_ID);
        Fragment fragment = new GroupMemberListFragment(groupId);
        FragmentManager manager = getSupportFragmentManager();
        manager.popBackStack();
        manager.beginTransaction().replace(R.id.fl_fragment_container, fragment).commit();
    }
}
