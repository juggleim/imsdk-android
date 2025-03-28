package com.juggle.chat.settings;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.jet.im.kit.SendbirdUIKit;
import com.jet.im.kit.utils.TextUtils;
import com.juggle.chat.R;
import com.juggle.chat.bean.HttpResult;
import com.juggle.chat.bean.QRCodeBean;
import com.juggle.chat.databinding.ActivityQrcodeDisplayBinding;
import com.juggle.chat.http.CustomCallback;
import com.juggle.chat.http.ServiceManager;
import com.juggle.im.model.Conversation;

public class QRCodeDisplayActivity extends AppCompatActivity {
    public static final int REQUEST_CODE_ASK_PERMISSIONS = 100;
    private final static String CONVERSATION_TYPE = "conversationType";
    private final static String CONVERSATION_ID = "conversationId";
    private final static String NAME = "name";
    private final static String PORTRAIT = "portrait";
    private final static String COUNT = "count";
    private Conversation.ConversationType mConversationType;
    private String mConversationId;
    private String mName;
    private String mPortrait;
    private int mCount;
    private ActivityQrcodeDisplayBinding mBinding;

    private LinearLayout qrCodeCardLl;
    private ImageView portraitIv;
    private TextView mainInfoTv;
    private TextView subInfoTv;
    private ImageView qrCodeIv;
    private TextView qrCodeDescribeTv;
    private TextView qrNoCodeDescribeTv;

    public static Intent newIntent(@NonNull Context context, Conversation.ConversationType conversationType, String conversationId, String name, String portrait, int count) {
        Intent intent = new Intent(context, QRCodeDisplayActivity.class);
        intent.putExtra(CONVERSATION_TYPE, conversationType.getValue());
        intent.putExtra(CONVERSATION_ID, conversationId);
        intent.putExtra(NAME, name);
        intent.putExtra(PORTRAIT, portrait);
        intent.putExtra(COUNT, count);
        return intent;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        int type = getIntent().getIntExtra(CONVERSATION_TYPE, 0);
        mConversationType = Conversation.ConversationType.setValue(type);
        mConversationId = getIntent().getStringExtra(CONVERSATION_ID);
        mName = getIntent().getStringExtra(NAME);
        mPortrait = getIntent().getStringExtra(PORTRAIT);
        mCount = getIntent().getIntExtra(COUNT, 0);

        mBinding = ActivityQrcodeDisplayBinding.inflate(getLayoutInflater());
        mBinding.headerView.setLeftButtonImageResource(com.jet.im.kit.R.drawable.icon_arrow_left);
        mBinding.headerView.setLeftButtonTint(SendbirdUIKit.getDefaultThemeMode().getPrimaryTintColorStateList(this));
        mBinding.headerView.setOnLeftButtonClickListener(v -> finish());
        mBinding.headerView.setUseRightButton(false);

        setContentView(mBinding.getRoot());

        initView();
        refresh();
    }

    private void initView() {
        qrNoCodeDescribeTv = findViewById(R.id.profile_tv_qr_card_info_no_code_describe);
        // 二维码描述
        qrCodeDescribeTv = findViewById(R.id.profile_tv_qr_card_info_describe);
        if (mConversationType == Conversation.ConversationType.GROUP) {
            mBinding.headerView.getTitleTextView().setText(getString(com.jet.im.kit.R.string.text_group_qrcode));
            qrCodeDescribeTv.setText(R.string.text_qrcode_group_tips);
        } else if (mConversationType == Conversation.ConversationType.PRIVATE) {
            mBinding.headerView.getTitleTextView().setText(getString(R.string.text_my_qrcode));
            qrCodeDescribeTv.setText(R.string.text_qrcode_private_tips);
        }
        // 二维码卡片父容器
        qrCodeCardLl = findViewById(R.id.profile_fl_card_capture_area_container);
        // 二维码信息所属头像
        portraitIv = findViewById(R.id.profile_iv_card_info_portrait);
        if (TextUtils.isNotEmpty(mPortrait)) {
            Glide.with(this)
                    .load(mPortrait)
                    .into(portraitIv);
        } else {
            Glide.with(this)
                    .load(R.drawable.icon_default_group)
                    .into(portraitIv);
        }
        // 二维码信息所属名称
        mainInfoTv = findViewById(R.id.profile_tv_qr_info_main);
        mainInfoTv.setText(mName);
        // 二维码信息所属副信息
        subInfoTv = findViewById(R.id.profile_tv_qr_info_sub);
        if (mConversationType == Conversation.ConversationType.GROUP) {
            subInfoTv.setText(getString(R.string.text_member_count, mCount));
            subInfoTv.setVisibility(View.VISIBLE);
        } else {
            subInfoTv.setVisibility(View.GONE);
        }
        // 二维码图片
        qrCodeIv = findViewById(R.id.profile_iv_qr_code);
//        // 保存图片
//        findViewById(R.id.profile_tv_qr_save_phone).setOnClickListener(v -> {
//            saveQRCodeToLocal();
//        });
//        // 分享至 SealTalk
//        findViewById(R.id.profile_tv_qr_share_to_juggle).setOnClickListener(v -> {
//            shareToJuggle();
//        });
    }

    private void refresh() {
        if (mConversationType == Conversation.ConversationType.GROUP) {
            ServiceManager.getGroupsService().getQRCode(mConversationId).enqueue(new CustomCallback<HttpResult<QRCodeBean>, QRCodeBean>() {
                @Override
                public void onSuccess(QRCodeBean qrCodeBean) {
                    String qrcode = qrCodeBean.getQrCode();
                    setQRCode(qrcode);
                }
            });
        } else if (mConversationType == Conversation.ConversationType.PRIVATE) {
            ServiceManager.getUserService().getQRCode().enqueue(new CustomCallback<HttpResult<QRCodeBean>, QRCodeBean>() {
                @Override
                public void onSuccess(QRCodeBean qrCodeBean) {
                    String qrcode = qrCodeBean.getQrCode();
                    setQRCode(qrcode);
                }
            });

        }
    }

    private void setQRCode(String qrCode) {
        byte[] decodedString = Base64.decode(qrCode, Base64.DEFAULT);
        Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
        qrCodeIv.setImageBitmap(decodedByte);
    }

//    private void saveQRCodeToLocal() {
//        if (!checkHasStoragePermission()) {
//            return;
//        }
//        saveBitmap(getViewBitmap(qrCodeCardLl));
//    }
//
//    private void shareToJuggle() {
//
//    }
//
//    private boolean checkHasStoragePermission() {
//        // 从6.0系统(API 23)开始，访问外置存储需要动态申请权限
//        if (Build.VERSION.SDK_INT >= 23) {
//            int checkPermission = checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE);
//            if (checkPermission != PackageManager.PERMISSION_GRANTED) {
//                requestPermissions(
//                        new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE},
//                        REQUEST_CODE_ASK_PERMISSIONS);
//                return false;
//            }
//        }
//        return true;
//    }
//
//    private Bitmap getViewBitmap(View view) {
//        Bitmap bitmap =
//                Bitmap.createBitmap(view.getWidth(), view.getHeight(), Bitmap.Config.ARGB_8888);
//        Canvas canvas = new Canvas(bitmap);
//        Drawable bgDrawable = view.getBackground();
//        canvas.drawColor(Color.WHITE); // 默认为白色背景
//        if (bgDrawable != null) {
//            bgDrawable.draw(canvas);
//        }
//
//        view.draw(canvas);
//        return bitmap;
//    }
//
//    private void saveBitmap(Bitmap bitmap) {
//        String fileName = System.currentTimeMillis() + ".png";
//
//
//    }
}
