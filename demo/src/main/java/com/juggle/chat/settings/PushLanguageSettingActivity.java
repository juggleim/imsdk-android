package com.juggle.chat.settings;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.jet.im.kit.widgets.StatusFrameView;
import com.juggle.chat.R;
import com.juggle.im.JIM;
import com.juggle.im.JIMConst;
import com.juggle.im.interfaces.IConnectionManager;

public class PushLanguageSettingActivity extends AppCompatActivity {

    private CheckBox mCbChinese;
    private CheckBox mCbEnglish;
    private StatusFrameView mStatusFrameView;

    public static Intent newIntent(@NonNull Context context) {
        return new Intent(context, PushLanguageSettingActivity.class);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_push_language_setting);

        ConstraintLayout itemChinese = findViewById(R.id.itemChinese);
        ConstraintLayout itemEnglish = findViewById(R.id.itemEnglish);

        mCbChinese = findViewById(R.id.cb_chinese);
        mCbEnglish = findViewById(R.id.cb_english);
        mCbChinese.setBackgroundResource(com.jet.im.kit.R.drawable.selector_radio_button_light);
        mCbEnglish.setBackgroundResource(com.jet.im.kit.R.drawable.selector_radio_button_light);
        setChineseCheck(true);

        itemChinese.setOnClickListener(v -> {
            if (mCbChinese.isChecked()) {
                return;
            }
            setChinese(true);
        });

        itemEnglish.setOnClickListener(v -> {
            if (mCbEnglish.isChecked()) {
                return;
            }
            setChinese(false);
        });

        final FrameLayout innerContainer = new FrameLayout(this);
        innerContainer.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        mStatusFrameView = new StatusFrameView(this, null, com.jet.im.kit.R.attr.sb_component_status);
        LinearLayout listView = findViewById(R.id.list_view);
        listView.addView(innerContainer);
        innerContainer.addView(mStatusFrameView);

        getLanguage();
    }

    private void setChinese(boolean isChinese) {
        String language;
        if (isChinese) {
            language = "zh-Hans-CN";
        } else {
            language = "en_US";
        }
        mStatusFrameView.setStatus(StatusFrameView.Status.LOADING);
        JIM.getInstance().getConnectionManager().setLanguage(language, new IConnectionManager.ISimpleCallback() {
            @Override
            public void onSuccess() {
                mStatusFrameView.setStatus(StatusFrameView.Status.NONE);
                setChineseCheck(isChinese(language));
            }

            @Override
            public void onError(int errorCode) {
                mStatusFrameView.setStatus(StatusFrameView.Status.NONE);
            }
        });
    }

    private void getLanguage() {
        mStatusFrameView.setStatus(StatusFrameView.Status.LOADING);
        JIM.getInstance().getConnectionManager().getLanguage(new JIMConst.IResultCallback<String>() {
            @Override
            public void onSuccess(String data) {
                mStatusFrameView.setStatus(StatusFrameView.Status.NONE);
                setChineseCheck(isChinese(data));
            }

            @Override
            public void onError(int errorCode) {
                mStatusFrameView.setStatus(StatusFrameView.Status.NONE);
            }
        });
    }

    private void setChineseCheck(boolean isChinese) {
        if (isChinese) {
            mCbChinese.setChecked(true);
            mCbEnglish.setChecked(false);
        } else {
            mCbChinese.setChecked(false);
            mCbEnglish.setChecked(true);
        }
    }

    private boolean isChinese(String language) {
        return !language.startsWith("en");
    }
}
