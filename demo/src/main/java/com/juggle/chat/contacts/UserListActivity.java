package com.juggle.chat.contacts;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.juggle.chat.R;

public class UserListActivity extends AppCompatActivity {

    private static final String TYPE = "type";

    public static Intent newIntent(@NonNull Context context, int type) {
        Intent intent = new Intent(context, UserListActivity.class);
        intent.putExtra(TYPE, type);
        return intent;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.demo_activity);

        int type = getIntent().getIntExtra(TYPE, 0);
        Fragment fragment = new UserListFragment(type);
        FragmentManager manager = getSupportFragmentManager();
        manager.popBackStack();
        manager.beginTransaction().replace(R.id.fl_fragment_container, fragment).commit();
    }
}
