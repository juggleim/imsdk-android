package com.juggle.chat.settings;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.jet.im.kit.SendbirdUIKit;
import com.juggle.chat.R;
import com.juggle.chat.bean.HttpResult;
import com.juggle.chat.bean.UserInfoRequest;
import com.juggle.chat.component.HeadComponent;
import com.juggle.chat.http.CustomCallback;
import com.juggle.chat.http.ServiceManager;

public class NicknameSettingActivity extends AppCompatActivity {
    public static Intent newIntent(@NonNull Context context) {
        return new Intent(context, NicknameSettingActivity.class);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_nickname_setting);

        EditText editText = findViewById(R.id.etName);
        editText.setText(SendbirdUIKit.nickname);

        HeadComponent headComponent = findViewById(R.id.head_component);
        headComponent.setRightClickListener(v -> {
            UserInfoRequest user = new UserInfoRequest();
            user.setUserId(SendbirdUIKit.userId);
            String nickname = editText.getText().toString();
            user.setNickname(nickname);
            ServiceManager.getUserService().updateUserInfo(user).enqueue(new CustomCallback<HttpResult<Object>, Object>() {
                @Override
                public void onSuccess(Object o) {
                    SendbirdUIKit.nickname = nickname;
                    finish();
                }
            });
        });
    }
}
