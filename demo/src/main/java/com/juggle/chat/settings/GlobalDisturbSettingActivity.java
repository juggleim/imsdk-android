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
import com.juggle.im.interfaces.IMessageManager;
import com.juggle.im.model.TimePeriod;

import java.util.ArrayList;
import java.util.List;

public class GlobalDisturbSettingActivity extends AppCompatActivity {

    private CheckBox mCbAllow;
    private CheckBox mCb08_12;
    private CheckBox mCb19_20;
    private CheckBox mCb23_06;
    private CheckBox mCbDisturb;
    private StatusFrameView mStatusFrameView;

    public static Intent newIntent(@NonNull Context context) {
        return new Intent(context, GlobalDisturbSettingActivity.class);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_global_disturb_setting);

        ConstraintLayout itemAllow = findViewById(R.id.itemAllow);
        ConstraintLayout item08_12 = findViewById(R.id.item_8_to_12);
        ConstraintLayout item19_20 = findViewById(R.id.item_19_to_20);
        ConstraintLayout item23_06 = findViewById(R.id.item_23_to_06);
        ConstraintLayout itemDisturb = findViewById(R.id.item_disturb);

        itemAllow.setOnClickListener(v -> {
            setData(CheckType.ALLOW);
        });
        item08_12.setOnClickListener(v -> {
            setData(CheckType.T8_12);
        });
        item19_20.setOnClickListener(v -> {
            setData(CheckType.T19_20);
        });
        item23_06.setOnClickListener(v -> {
            setData(CheckType.T23_06);
        });
        itemDisturb.setOnClickListener(v -> {
            setData(CheckType.DISTURB);
        });

        mCbAllow = findViewById(R.id.cb_allow);
        mCb08_12 = findViewById(R.id.cb_8_to_12);
        mCb19_20 = findViewById(R.id.cb_19_to_20);
        mCb23_06 = findViewById(R.id.cb_23_to_06);
        mCbDisturb = findViewById(R.id.cb_disturb);
        mCbAllow.setBackgroundResource(com.jet.im.kit.R.drawable.selector_radio_button_light);
        mCb08_12.setBackgroundResource(com.jet.im.kit.R.drawable.selector_radio_button_light);
        mCb19_20.setBackgroundResource(com.jet.im.kit.R.drawable.selector_radio_button_light);
        mCb23_06.setBackgroundResource(com.jet.im.kit.R.drawable.selector_radio_button_light);
        mCbDisturb.setBackgroundResource(com.jet.im.kit.R.drawable.selector_radio_button_light);
        setCheck(CheckType.ALLOW);

        final FrameLayout innerContainer = new FrameLayout(this);
        innerContainer.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        mStatusFrameView = new StatusFrameView(this, null, com.jet.im.kit.R.attr.sb_component_status);
        LinearLayout listView = findViewById(R.id.list_view);
        listView.addView(innerContainer);
        innerContainer.addView(mStatusFrameView);

        getData();
    }

    private void setData(CheckType type) {
        boolean isMute = true;
        List<TimePeriod> periods = new ArrayList<>();
        TimePeriod period = new TimePeriod();
        switch (type) {
            case ALLOW:
                isMute = false;
                break;
            case T8_12:
                period.setStartTime("08:00");
                period.setEndTime("12:00");
                periods.add(period);
                break;
            case T19_20:
                period.setStartTime("19:00");
                period.setEndTime("20:00");
                periods.add(period);
                break;
            case T23_06:
                period.setStartTime("23:00");
                period.setEndTime("06:00");
                periods.add(period);
                break;
            case DISTURB:
                break;
        }
        mStatusFrameView.setStatus(StatusFrameView.Status.LOADING);
        JIM.getInstance().getMessageManager().setMute(isMute, periods, new IMessageManager.ISimpleCallback() {
            @Override
            public void onSuccess() {
                mStatusFrameView.setStatus(StatusFrameView.Status.NONE);
                setCheck(type);
            }

            @Override
            public void onError(int errorCode) {
                mStatusFrameView.setStatus(StatusFrameView.Status.NONE);
            }
        });
    }

    private void getData() {
        mStatusFrameView.setStatus(StatusFrameView.Status.LOADING);
        JIM.getInstance().getMessageManager().getMuteStatus(new IMessageManager.IGetMuteStatusCallback() {
            @Override
            public void onSuccess(boolean isMute, String timezone, List<TimePeriod> periods) {
                mStatusFrameView.setStatus(StatusFrameView.Status.NONE);
                if (!isMute) {
                    setCheck(CheckType.ALLOW);
                } else {
                    if (periods != null && !periods.isEmpty()) {
                        TimePeriod period = periods.get(0);
                        if (period.getStartTime().equals("08:00")) {
                            setCheck(CheckType.T8_12);
                        } else if (period.getStartTime().equals("19:00")) {
                            setCheck(CheckType.T19_20);
                        } else if (period.getStartTime().equals("23:00")) {
                            setCheck(CheckType.T23_06);
                        } else {
                            setCheck(CheckType.DISTURB);
                        }
                    } else {
                        setCheck(CheckType.DISTURB);
                    }
                }
            }

            @Override
            public void onError(int errorCode) {
                mStatusFrameView.setStatus(StatusFrameView.Status.NONE);
            }
        });

    }

    private void setCheck(CheckType checkType) {
        mCbAllow.setChecked(false);
        mCb08_12.setChecked(false);
        mCb19_20.setChecked(false);
        mCb23_06.setChecked(false);
        mCbDisturb.setChecked(false);
        switch (checkType) {
            case ALLOW:
                mCbAllow.setChecked(true);
                break;
            case T8_12:
                mCb08_12.setChecked(true);
                break;
            case T19_20:
                mCb19_20.setChecked(true);
                break;
            case T23_06:
                mCb23_06.setChecked(true);
                break;
            case DISTURB:
                mCbDisturb.setChecked(true);
                break;
        }
    }

    private enum CheckType {
        ALLOW, T8_12, T19_20, T23_06, DISTURB
    }
}
