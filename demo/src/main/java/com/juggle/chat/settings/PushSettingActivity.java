package com.juggle.chat.settings;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.juggle.chat.R;

public class PushSettingActivity extends AppCompatActivity {
    public static Intent newIntent(@NonNull Context context) {
        return new Intent(context, PushSettingActivity.class);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_push_setting);

        ConstraintLayout pushLanguageSetting = findViewById(R.id.itemPushLanguageSetting);
        pushLanguageSetting.setOnClickListener(v -> {
            Intent intent = PushLanguageSettingActivity.newIntent(PushSettingActivity.this);
            startActivity(intent);
        });
    }
}
