package com.jet.im.kit.utils;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.jet.im.kit.log.Logger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class PermissionUtils {

    public final static String[] CAMERA_PERMISSION = getCameraPermission();
    public final static String[] GET_CONTENT_PERMISSION = getGetContentPermission();
    public final static String[] RECORD_AUDIO_PERMISSION = getRecordAudioPermission();

    private PermissionUtils() {
    }

    private static String[] getRecordAudioPermission() {
        return new String[]{Manifest.permission.RECORD_AUDIO};
    }

    private static String[] getCameraPermission() {
        String[] permissions = new String[]{Manifest.permission.CAMERA,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE};
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
            permissions = new String[]{Manifest.permission.CAMERA};
        }
        return permissions;
    }

    private static String[] getGetContentPermission() {
        String[] permissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE};
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions = new String[]{};
        } else if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
            permissions = new String[]{Manifest.permission.READ_EXTERNAL_STORAGE};
        }
        return permissions;
    }

    public static boolean hasPermissions(@NonNull Context context, @NonNull String... permissions) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            for (String permission : permissions) {
                if (ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }

    @NonNull
    public static String[] getNotGrantedPermissions(@NonNull Context context, @NonNull String... permissions) {
        List<String> notGrantedPermissions = new ArrayList<>();
        for (String perm : permissions) {
            if (!PermissionUtils.hasPermissions(context, perm)) {
                notGrantedPermissions.add(perm);
            }
        }
        return notGrantedPermissions.toArray(new String[]{});
    }

    @NonNull
    public static List<String> getNotGrantedPermissions(@NonNull Map<String, Boolean> permissionResults) {
        final Set<String> keys = permissionResults.keySet();
        final List<String> notGrantedList = new ArrayList<>();
        for (String permission : keys) {
            Logger.d("permissionResults.get(%s) : %s", permission, permissionResults.get(permission));
            if (Boolean.FALSE.equals(permissionResults.get(permission))) {
                notGrantedList.add(permission);
            }
        }
        return notGrantedList;
    }

    @NonNull
    public static List<String> getExplicitDeniedPermissionList(@NonNull Activity activity, @NonNull Collection<String> permissions) {
        List<String> result = new ArrayList<>();
        for (String permission : permissions) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)) {
                result.add(permission);
            }
        }
        return result;
    }

    @NonNull
    public static List<String> getExplicitDeniedPermissionList(@NonNull Activity activity, @NonNull String... permissions) {
        return getExplicitDeniedPermissionList(activity, Arrays.asList(permissions));
    }
}
