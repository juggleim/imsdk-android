package com.juggle.chat.settings;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.juggle.chat.R;
import com.juggle.chat.bean.GroupMemberBean;

public class UserDetailActivity extends AppCompatActivity {
    private final static String USER_ID = "userId";
    private final static String NAME = "name";
    private final static String PORTRAIT = "portrait";
    public static Intent newIntent(@NonNull Context context, GroupMemberBean member) {
        Intent intent = new Intent(context, UserDetailActivity.class);
        intent.putExtra(USER_ID, member.getUserId());
        intent.putExtra(NAME, member.getNickname());
        intent.putExtra(PORTRAIT, member.getAvatar());
        return intent;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.demo_activity);

        String userId = getIntent().getStringExtra(USER_ID);
        String name = getIntent().getStringExtra(NAME);
        String portrait = getIntent().getStringExtra(PORTRAIT);

        Fragment fragment = new UserDetailFragment(userId, name, portrait);
        FragmentManager manager = getSupportFragmentManager();
        manager.popBackStack();
        manager.beginTransaction().replace(R.id.fl_fragment_container, fragment).commit();
    }
}
