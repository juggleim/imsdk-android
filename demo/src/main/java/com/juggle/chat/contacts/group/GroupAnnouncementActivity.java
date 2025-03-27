package com.juggle.chat.contacts.group;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.juggle.chat.R;
import com.juggle.chat.bean.GroupAnnouncementBean;
import com.juggle.chat.bean.HttpResult;
import com.juggle.chat.component.HeadComponent;
import com.juggle.chat.http.CustomCallback;
import com.juggle.chat.http.ServiceManager;

import java.util.HashMap;

import okhttp3.RequestBody;

public class GroupAnnouncementActivity extends AppCompatActivity {
    private static final String GROUP_ID = "groupId";
    private String mGroupId;
    private EditText mEditText;

    public static Intent newIntent(@NonNull Context context, String groupId) {
        Intent intent = new Intent(context, GroupAnnouncementActivity.class);
        intent.putExtra(GROUP_ID, groupId);
        return intent;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mGroupId = getIntent().getStringExtra(GROUP_ID);
        setContentView(R.layout.activity_group_announcement);
        mEditText = findViewById(R.id.profile_et_group_notice);

        HeadComponent headComponent = findViewById(R.id.head_component);
        headComponent.setRightClickListener(v -> {
            HashMap<String, Object> paramsMap = new HashMap<>();
            paramsMap.put("group_id", mGroupId);
            paramsMap.put("content", mEditText.getText().toString());
            RequestBody body = ServiceManager.createJsonRequest(paramsMap);

            ServiceManager.getGroupsService().updateGroupAnnouncement(body).enqueue(new CustomCallback<HttpResult<Object>, Object>() {
                @Override
                public void onSuccess(Object o) {
                    finish();
                }
            });
        });

        loadData();
    }

    private void loadData() {
        ServiceManager.getGroupsService().getGroupAnnouncement(mGroupId).enqueue(new CustomCallback<HttpResult<GroupAnnouncementBean>, GroupAnnouncementBean>() {
            @Override
            public void onSuccess(GroupAnnouncementBean groupAnnouncementBean) {
                mEditText.setText(groupAnnouncementBean.getContent());
            }
        });
    }
}
