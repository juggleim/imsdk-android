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
import com.juggle.chat.chatroom.ChatRoomListFragment;

public class ChatroomListActivity extends AppCompatActivity {
    public static Intent newIntent(@NonNull Context context) {
        return new Intent(context, ChatroomListActivity.class);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.demo_activity);

        Fragment fragment = new ChatRoomListFragment();
        FragmentManager manager = getSupportFragmentManager();
        manager.popBackStack();;
        manager.beginTransaction().replace(R.id.fl_fragment_container, fragment).commit();
    }
}
