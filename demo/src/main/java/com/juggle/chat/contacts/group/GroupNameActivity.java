package com.juggle.chat.contacts.group;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.juggle.chat.R;
import com.juggle.chat.bean.GroupBean;
import com.juggle.chat.bean.HttpResult;
import com.juggle.chat.component.HeadComponent;
import com.juggle.chat.http.CustomCallback;
import com.juggle.chat.http.ServiceManager;

public class GroupNameActivity extends AppCompatActivity {
    private static final String GROUP_ID = "groupId";
    private static final String NAME = "name";
    private static final String PORTRAIT = "portrait";

    public static Intent newIntent(@NonNull Context context, String groupId, String groupName, String portrait) {
        Intent intent = new Intent(context, GroupNameActivity.class);
        intent.putExtra(GROUP_ID, groupId);
        intent.putExtra(NAME, groupName);
        intent.putExtra(PORTRAIT, portrait);
        return intent;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String groupId = getIntent().getStringExtra(GROUP_ID);
        String groupName = getIntent().getStringExtra(NAME);
        String portrait = getIntent().getStringExtra(PORTRAIT);

        setContentView(R.layout.activity_name_edit);

        EditText editText = findViewById(R.id.etName);
        editText.setText(groupName);

        HeadComponent headComponent = findViewById(R.id.head_component);
        headComponent.setTitleText(getString(R.string.text_update_group_name));
        headComponent.setRightClickListener(v -> {
            GroupBean group = new GroupBean();
            group.setGroup_id(groupId);
            group.setGroup_name(editText.getText().toString());
            group.setGroup_portrait(portrait);
            ServiceManager.getGroupsService().updateGroupInfo(group).enqueue(new CustomCallback<HttpResult<Object>, Object>() {
                @Override
                public void onSuccess(Object o) {
                    finish();
                }
            });
        });
    }
}
