package com.jet.im.kit.fragments;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.PermissionGroupInfo;
import android.content.pm.PermissionInfo;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.provider.Settings;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContract;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;

import com.jet.im.kit.R;
import com.jet.im.kit.log.Logger;
import com.jet.im.kit.utils.ContextUtils;
import com.jet.im.kit.utils.PermissionUtils;

import java.util.List;
import java.util.Locale;
import java.util.Map;

public abstract class PermissionFragment extends BaseFragment {
    interface PermissionHandler {
        void onPermissionGranted();
    }

    @Nullable
    private PermissionHandler handler;

    @NonNull
    private final ActivityResultLauncher<PermissionInformation> appSettingLauncher = registerForActivityResult(new AppSettingActivityResult(), information -> {
        if (getContext() == null) return;

        final boolean hasPermission = PermissionUtils.hasPermissions(getContext(), information.permission);
        Logger.d("___ hasPermission=%s", hasPermission);
        if (hasPermission) {
            notifyPermissionResult(information.handler);
        }
    });
    @NonNull
    private final ActivityResultLauncher<String[]> requestPermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), new ActivityResultCallback<Map<String, Boolean>>() {
        @Override
        public void onActivityResult(@NonNull Map<String, Boolean> permissionResults) {
            if (getContext() == null) return;
            if (PermissionUtils.getNotGrantedPermissions(permissionResults).isEmpty()) {
                notifyPermissionResult(handler);
                handler = null;
            }
        }
    });

    private void notifyPermissionResult(@Nullable PermissionHandler handler) {
        if (handler != null) {
            handler.onPermissionGranted();
        }
    }

    void requestPermission(@NonNull String[] permissions, @NonNull PermissionHandler handler) {
        if (getContext() == null || getActivity() == null) return;
        this.handler = handler;
        // 1. check permission
        final boolean hasPermission = PermissionUtils.hasPermissions(getContext(), permissions);
        if (hasPermission) {
            notifyPermissionResult(handler);
            return;
        }

        // 2. determine whether rationale popup should show
        final List<String> deniedList = PermissionUtils.getExplicitDeniedPermissionList(getActivity(), permissions);
        if (!deniedList.isEmpty()) {
            showPermissionRationalePopup(deniedList.get(0), handler);
            return;
        }
        // 3. request permission
        requestPermissionLauncher.launch(permissions);
    }

    @Nullable
    private static Drawable getPermissionDrawable(@NonNull Activity activity, @NonNull String permission) {
        Drawable drawable = null;
        try {
            PackageManager pm = activity.getPackageManager();
            PermissionInfo permissionInfo = pm.getPermissionInfo(permission, 0);
            if (permissionInfo.group == null) return null;
            PermissionGroupInfo groupInfo = pm.getPermissionGroupInfo(permissionInfo.group, 0);
            drawable = ResourcesCompat.getDrawable(pm.getResourcesForApplication("android"), groupInfo.icon, activity.getTheme());
        } catch (Exception e) {
            Logger.w(e);
        }
        return drawable;
    }

    @NonNull
    private static String getPermissionGuideMessage(@NonNull Context context, @NonNull String permission) {
        int textResId;
        if (Manifest.permission.CAMERA.equals(permission)) {
            textResId = R.string.sb_text_need_to_allow_permission_camera;
        } else if (Manifest.permission.RECORD_AUDIO.equals(permission)) {
            textResId = R.string.sb_text_need_to_allow_permission_record_audio;
        } else {
            textResId = R.string.sb_text_need_to_allow_permission_storage;
        }
        return String.format(Locale.US, context.getString(textResId), ContextUtils.getApplicationName(context));
    }

    private void showPermissionRationalePopup(@NonNull String permission, @NonNull PermissionHandler handler) {
        Drawable icon = getPermissionDrawable(requireActivity(), permission);
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle(requireContext().getString(R.string.sb_text_dialog_permission_title));
        builder.setMessage(getPermissionGuideMessage(requireContext(), permission));
        if (icon != null) {
            builder.setIcon(icon);
        }
        builder.setPositiveButton(R.string.sb_text_go_to_settings, (dialogInterface, i) -> {
            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            intent.addCategory(Intent.CATEGORY_DEFAULT);
            intent.setData(Uri.parse("package:" + requireContext().getPackageName()));
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
            intent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
            appSettingLauncher.launch(new PermissionInformation(intent, permission, handler));
        });
        AlertDialog dialog = builder.create();
        dialog.show();
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(ContextCompat.getColor(requireContext(), R.color.secondary_300));
    }

    private static final class PermissionInformation {
        @NonNull
        private Intent intent;
        @NonNull
        private final String permission;
        @NonNull
        private final PermissionHandler handler;

        public PermissionInformation(@NonNull Intent intent, @NonNull String permission, @NonNull PermissionHandler handler) {
            this.intent = intent;
            this.permission = permission;
            this.handler = handler;
        }
    }

    private static final class AppSettingActivityResult extends ActivityResultContract<PermissionInformation, PermissionInformation> {
        private PermissionInformation information;

        @NonNull
        @Override
        public Intent createIntent(@NonNull Context context, @NonNull PermissionInformation information) {
            this.information = information;
            return information.intent;
        }

        @NonNull
        @Override
        public PermissionInformation parseResult(int resultCode, @Nullable Intent intent) {
            if (intent != null) this.information.intent = intent;
            return information;
        }
    }
}
