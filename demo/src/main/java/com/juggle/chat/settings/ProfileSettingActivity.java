package com.juggle.chat.settings;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.jet.im.kit.SendbirdUIKit;
import com.jet.im.kit.activities.BaseActivity;
import com.jet.im.kit.interfaces.OnItemClickListener;
import com.jet.im.kit.log.Logger;
import com.jet.im.kit.model.DialogListItem;
import com.jet.im.kit.utils.ContextUtils;
import com.jet.im.kit.utils.DialogUtils;
import com.jet.im.kit.utils.FileUtils;
import com.jet.im.kit.utils.IntentUtils;
import com.jet.im.kit.utils.PermissionUtils;
import com.juggle.chat.R;
import com.juggle.chat.bean.HttpResult;
import com.juggle.chat.bean.UserInfoRequest;
import com.juggle.chat.http.CustomCallback;
import com.juggle.chat.http.ServiceManager;
import com.juggle.im.JIM;
import com.juggle.im.JIMConst;

import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class ProfileSettingActivity extends BaseActivity {
    private Uri mMediaUri = null;
    private ActivityResultLauncher<Intent> mTakeCameraLauncher;
    private ActivityResultLauncher<Intent> mGetContentLauncher;
    private ActivityResultLauncher<Intent> mAppSettingLauncher;
    private ActivityResultLauncher<String[]> mPermissionLauncher;

    public static Intent newIntent(@NonNull Context context) {
        return new Intent(context, ProfileSettingActivity.class);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mTakeCameraLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    int resultCode = result.getResultCode();
                    if (resultCode != Activity.RESULT_OK) {
                        return;
                    }
                    if (mMediaUri != null) {
                        String path = FileUtils.uriToPath(this, mMediaUri);
                        updateUserProfileImage(path);
                    }
                }
        );
        mGetContentLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    Intent intent = result.getData();
                    int resultCode = result.getResultCode();
                    if (resultCode != Activity.RESULT_OK || intent == null) {
                        return;
                    }
                    Uri mediaUri = intent.getData();
                    if (mediaUri != null) {
                        String path = FileUtils.uriToPath(this, mediaUri);
                        updateUserProfileImage(path);
                    }
                }
        );
        mAppSettingLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    boolean hasPermission = PermissionUtils.hasPermissions(this, PermissionUtils.CAMERA_PERMISSION);
                    if (hasPermission) {
                        showMediaSelectDialog();
                    }
                }
        );
        mPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestMultiplePermissions(),
                result -> {
                    if (PermissionUtils.getNotGrantedPermissions(result).isEmpty()) {
                        showMediaSelectDialog();
                    }
                }
        );

        setContentView(R.layout.activity_profile_setting);

        ConstraintLayout itemPortrait = findViewById(R.id.itemPortrait);
        ConstraintLayout itemNickname = findViewById(R.id.itemNickname);

        itemPortrait.setOnClickListener(v -> {
            editPortrait();
        });
        itemNickname.setOnClickListener(v -> {
            editNickname();
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        updateAvatar();
        TextView tvNickname = findViewById(R.id.tvNickname);
        tvNickname.setText(SendbirdUIKit.nickname);
    }

    private void updateAvatar() {
        ImageView ivProfileView = findViewById(R.id.ivProfileView);
        Glide.with(this)
                .load(SendbirdUIKit.avatar)
                .circleCrop()
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(ivProfileView);
    }

    private void editNickname() {
        Intent intent = NicknameSettingActivity.newIntent(this);
        startActivity(intent);
    }

    private void editPortrait() {
        boolean hasPermission = PermissionUtils.hasPermissions(this, PermissionUtils.CAMERA_PERMISSION);
        if (hasPermission) {
            showMediaSelectDialog();
            return;
        }
        requestPermission(PermissionUtils.CAMERA_PERMISSION);
    }

    private void requestPermission(String[] permissions) {
        boolean hasPermission = PermissionUtils.hasPermissions(this, permissions);
        if (hasPermission) {
            showMediaSelectDialog();
            return;
        }

        List<String> deniedList = PermissionUtils.getExplicitDeniedPermissionList(this, permissions);
        if (!deniedList.isEmpty()) {
            showPermissionRationalePopup(deniedList.get(0));
            return;
        }

        mPermissionLauncher.launch(permissions);
    }

    private void showPermissionRationalePopup(String permission) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(com.jet.im.kit.R.string.sb_text_dialog_permission_title));
        builder.setMessage(getPermissionGuideMessage(this, permission));
        builder.setPositiveButton(com.jet.im.kit.R.string.sb_text_go_to_settings, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                intent.addCategory(Intent.CATEGORY_DEFAULT);
                intent.setData(Uri.parse("package:" + getPackageName()));
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                intent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
                mAppSettingLauncher.launch(intent);
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
        dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                .setTextColor(ContextCompat.getColor(this, com.jet.im.kit.R.color.secondary_main));
    }

    private String getPermissionGuideMessage(Context context, String permission) {
        int textResId;
        if (Objects.equals(permission, Manifest.permission.CAMERA)) {
            textResId = com.jet.im.kit.R.string.sb_text_need_to_allow_permission_camera;
        } else {
            textResId = com.jet.im.kit.R.string.sb_text_need_to_allow_permission_storage;
        }
        return String.format(Locale.US, context.getString(textResId), ContextUtils.getApplicationName(this));
    }

    private void showMediaSelectDialog() {
        DialogListItem[] items = {
            new DialogListItem(com.jet.im.kit.R.string.sb_text_channel_settings_change_channel_image_camera),
            new DialogListItem(com.jet.im.kit.R.string.sb_text_channel_settings_change_channel_image_gallery)
        };
        DialogUtils.showListDialog(this, getString(com.jet.im.kit.R.string.sb_text_channel_settings_change_channel_image), items, new OnItemClickListener<DialogListItem>() {
            @Override
            public void onItemClick(@NonNull View view, int position, @NonNull DialogListItem data) {
                try {
                    int key = data.getKey();
                    if (key == com.jet.im.kit.R.string.sb_text_channel_settings_change_channel_image_camera) {
                        takeCamera();
                    } else if (key == com.jet.im.kit.R.string.sb_text_channel_settings_change_channel_image_gallery) {
                        takePhoto();
                    }
                } catch (Exception e) {
                    Logger.e(e);
                }
            }
        });
    }

    private void takeCamera() {
        mMediaUri = FileUtils.createImageFileUri(this);
        if (mMediaUri == null) {
            return;
        }
        Uri uri = mMediaUri;
        Intent intent = IntentUtils.getCameraIntent(this, uri);
        if (IntentUtils.hasIntent(this, intent)) {
            mTakeCameraLauncher.launch(intent);
        }
    }

    private void takePhoto() {
        Intent intent = IntentUtils.getImageGalleryIntent();
        mGetContentLauncher.launch(intent);
    }

    private void updateUserProfileImage(String path) {
        JIM.getInstance().getMessageManager().uploadImage(path, new JIMConst.IResultCallback<String>() {
            @Override
            public void onSuccess(String data) {
                UserInfoRequest userInfoRequest = new UserInfoRequest();
                userInfoRequest.setUserId(SendbirdUIKit.userId);
                userInfoRequest.setAvatar(data);
                userInfoRequest.setNickname(SendbirdUIKit.nickname);
                ServiceManager.getUserService().updateUserInfo(userInfoRequest).enqueue(new CustomCallback<HttpResult<Object>, Object>() {
                    @Override
                    public void onSuccess(Object o) {
                        SendbirdUIKit.avatar = data;
                        updateAvatar();
                    }
                });
            }

            @Override
            public void onError(int errorCode) {

            }
        });

    }
}
