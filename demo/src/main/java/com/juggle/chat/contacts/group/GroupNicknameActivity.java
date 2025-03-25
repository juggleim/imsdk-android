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

import java.util.HashMap;

import okhttp3.RequestBody;

public class GroupNicknameActivity extends AppCompatActivity {
    private static final String GROUP_ID = "groupId";
    private static final String NICKNAME = "nickname";

    public static Intent newIntent(@NonNull Context context, String groupId, String nickname) {
        Intent intent = new Intent(context, GroupNicknameActivity.class);
        intent.putExtra(GROUP_ID, groupId);
        intent.putExtra(NICKNAME, nickname);
        return intent;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String groupId = getIntent().getStringExtra(GROUP_ID);
        String nickname = getIntent().getStringExtra(NICKNAME);

        setContentView(R.layout.activity_name_edit);

        EditText editText = findViewById(R.id.etName);
        editText.setText(nickname);

        HeadComponent headComponent = findViewById(R.id.head_component);
        headComponent.setTitleText(getString(R.string.text_update_group_nickname));
        headComponent.setRightClickListener(v -> {
            HashMap<String, Object> paramsMap = new HashMap<>();
            paramsMap.put("group_id", groupId);
            paramsMap.put("grp_display_name", editText.getText().toString());
            RequestBody body = ServiceManager.createJsonRequest(paramsMap);

            ServiceManager.getGroupsService().updateGroupDisplayName(body).enqueue(new CustomCallback<HttpResult<Object>, Object>() {
                @Override
                public void onSuccess(Object o) {
                    finish();
                }
            });
        });
    }
}
