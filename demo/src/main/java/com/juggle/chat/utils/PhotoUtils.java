package com.juggle.chat.utils;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;

import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import com.juggle.chat.R;

import java.io.File;
import java.util.List;

public class PhotoUtils {

    private final String tag = PhotoUtils.class.getSimpleName();

    /** 裁剪图片成功后返回 */
    public static final int INTENT_CROP = 2;
    /** 拍照成功后返回 */
    public static final int INTENT_TAKE = 3;
    /** 拍照成功后返回 */
    public static final int INTENT_SELECT = 4;

    public static final String CROP_FILE_NAME = "crop_file.jpg";

    // 不需要裁剪图片
    public static final int NO_CROP = 0x1772;
    private int mType;
    // R以及以上版本通过MediaStore保存Uri来处理裁剪后路径
    private static Uri lastCropUriForR;

    /** PhotoUtils对象 */
    private OnPhotoResultListener onPhotoResultListener;

    public PhotoUtils(OnPhotoResultListener onPhotoResultListener) {
        this.onPhotoResultListener = onPhotoResultListener;
    }

    public PhotoUtils(OnPhotoResultListener onPhotoResultListener, int type) {
        this.onPhotoResultListener = onPhotoResultListener;
        mType = type;
    }

    /**
     * 拍照
     *
     * @param
     * @return
     */
    public void takePicture(Activity activity) {
        try {
            // 每次选择图片吧之前的图片删除
            onCleared(activity);

            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            intent.setAction(MediaStore.ACTION_IMAGE_CAPTURE);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, buildUri(activity));
            if (!isIntentAvailable(activity, intent)) {
                return;
            }
            activity.startActivityForResult(intent, INTENT_TAKE);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 拍照
     *
     * @param
     * @return
     */
    public void takePicture(Fragment fragment) {
        try {
            // 每次选择图片吧之前的图片删除
            onCleared(fragment.getActivity());

            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            intent.setAction(MediaStore.ACTION_IMAGE_CAPTURE);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, buildUri(fragment.getActivity()));
            if (!isIntentAvailable(fragment.getActivity(), intent)) {
                return;
            }
            fragment.startActivityForResult(intent, INTENT_TAKE);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 选择一张图片 图片类型，这里是image/*，当然也可以设置限制 如：image/jpeg等
     *
     * @param activity Activity
     */
    @SuppressLint("InlinedApi")
    public void selectPicture(Activity activity) {
        try {
            // 每次选择图片吧之前的图片删除
            onCleared(activity);

            Intent intent = new Intent(Intent.ACTION_PICK, null);
            intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");

            if (!isIntentAvailable(activity, intent)) {
                return;
            }
            activity.startActivityForResult(intent, INTENT_SELECT);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 选择一张图片 图片类型，这里是image/*，当然也可以设置限制 如：image/jpeg等
     *
     * @param fragment Fragment
     */
    @SuppressLint("InlinedApi")
    public void selectPicture(Fragment fragment) {
        try {
            // 每次选择图片吧之前的图片删除
            onCleared(fragment.getActivity());

            Intent intent = new Intent(Intent.ACTION_PICK, null);
            intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");

            if (!isIntentAvailable(fragment.getActivity(), intent)) {
                return;
            }
            fragment.startActivityForResult(intent, INTENT_SELECT);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 构建uri
     *
     * @param activity
     * @return
     */
    private Uri buildUri(Activity activity) {
//        if (Build.VERSION.SDK_INT >= 29) {
//            File file =
//                    new File(
//                            activity.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
//                                    + File.separator
//                                    + CROP_FILE_NAME);
//            Uri uri =
//                    FileProvider.getUriForFile(
//                            activity,
//                            activity.getPackageName()
//                                    + "activity.getResources()
//                                            .getString(R.string.rc_authorities_fileprovider)",
//                            file);
//            return uri;
//        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
//            Uri uri =
//                    FileProvider.getUriForFile(
//                            activity,
//                            activity.getPackageName()
//                                    + activity.getResources()
//                                            .getString(R.string.rc_authorities_fileprovider),
//                            new File(
//                                    Environment.getExternalStorageDirectory().getPath()
//                                            + File.separator
//                                            + CROP_FILE_NAME));
//            return uri;
//        } else {
            return Uri.fromFile(Environment.getExternalStorageDirectory())
                    .buildUpon()
                    .appendPath(CROP_FILE_NAME)
                    .build();
//        }
    }

    /**
     * 构建本地文件uri
     *
     * @return
     */
    private Uri buildLocalFileUri() {
        return Uri.fromFile(Environment.getExternalStorageDirectory())
                .buildUpon()
                .appendPath(CROP_FILE_NAME)
                .build();
    }

    /**
     * @param intent
     * @return
     */
    protected boolean isIntentAvailable(Activity activity, Intent intent) {
        if (activity == null || intent == null) {
            return false;
        }
        PackageManager packageManager = activity.getPackageManager();
        List<ResolveInfo> list =
                packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
        return list.size() > 0;
    }

    private boolean corp(Activity activity, Uri uri) {
        if (activity == null) {
            return false;
        }
        Intent cropIntent = buildCorpIntent(activity, uri);
        if (!isIntentAvailable(activity, cropIntent)) {
            return false;
        }
        try {
            activity.startActivityForResult(cropIntent, INTENT_CROP);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private boolean corp(Fragment fragment, Uri uri) {
        if (fragment.getActivity() == null) {
            return false;
        }
        Intent cropIntent = buildCorpIntent(fragment.getActivity(), uri);
        if (!isIntentAvailable(fragment.getActivity(), cropIntent)) {
            return false;
        }
        try {
            fragment.startActivityForResult(cropIntent, INTENT_CROP);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private Intent buildCorpIntent(Activity activity, Uri uri) {
        Intent cropIntent = new Intent("com.android.camera.action.CROP");
        cropIntent.setDataAndType(uri, "image/*");
        cropIntent.putExtra("crop", "true");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            cropIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        }
        cropIntent.putExtra("aspectX", 1);
        cropIntent.putExtra("aspectY", 1);
        cropIntent.putExtra("outputX", 200);
        cropIntent.putExtra("outputY", 200);
        cropIntent.putExtra("return-data", false);
        cropIntent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            lastCropUriForR = createCropImageUriForR(activity);
            cropIntent.putExtra(MediaStore.EXTRA_OUTPUT, lastCropUriForR);
        } else {
            cropIntent.putExtra(MediaStore.EXTRA_OUTPUT, createCropImageUri(activity));
        }
        return cropIntent;
    }

    private Uri buildCropUri(Context context) {
        if (context == null) {
            return null;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            File cropFile = queryCropFileForR(context, lastCropUriForR);
            if (cropFile == null) {
                return null;
            }
            return Uri.parse(cropFile.getAbsolutePath());
        } else {
            return createCropImageUri(context);
        }
    }

    private String getCropFilePath(Context context) {
        return context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
                + File.separator
                + CROP_FILE_NAME;
    }

    // R以及以上版本，先创建文件再插入到MediaStore
    private Uri createCropImageUriForR(Context context) {
        if (context == null) {
            return null;
        }
        try {
            File imgFile = new File(getCropFilePath(context));
            // 通过 MediaStore API 插入file 为了拿到系统裁剪要保存到的uri（因为App没有权限不能访问公共存储空间，需要通过 MediaStore API来操作）
            ContentValues values = new ContentValues();
            values.put(MediaStore.Images.Media.DATA, imgFile.getAbsolutePath());
            values.put(MediaStore.Images.Media.DISPLAY_NAME, CROP_FILE_NAME);
            values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
            return context.getContentResolver()
                    .insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
        } catch (Exception e) {
            return null;
        }
    }

    private Uri createCropImageUri(Context context) {
        return context != null ? Uri.fromFile(new File(getCropFilePath(context))) : null;
    }

    // R以及以上版本通过MediaStore查询裁剪后的文件
    private File queryCropFileForR(Context context, Uri uri) {
        if (context == null || uri == null) {
            return null;
        }
        Cursor cursor = null;
        try {
            String[] proj = {MediaStore.Images.Media.DATA};
            cursor = context.getContentResolver().query(uri, proj, null, null, null);
            if (cursor == null) {
                return null;
            }
            if (cursor.moveToFirst()) {
                int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                String path = cursor.getString(columnIndex);
                // 优化：对文件路径进行清理，防止路径遍历攻击
                path = sanitizeFilename(path);
                return new File(path);
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return null;
    }

    private String sanitizeFilename(String displayName) {
        if (displayName == null) {
            return null;
        } else {
            String[] badCharacters = new String[]{"..", "/"};
            String[] segments = displayName.split("/");
            String fileName = segments[segments.length - 1];
            String[] var4 = badCharacters;
            int var5 = badCharacters.length;

            for(int var6 = 0; var6 < var5; ++var6) {
                String suspString = var4[var6];
                fileName = fileName.replace(suspString, "_");
            }

            if (!displayName.equals(fileName)) {
            }

            return fileName;
        }
    }

    /**
     * 返回结果处理
     *
     * @param requestCode
     * @param resultCode
     * @param data
     */
    public void onActivityResult(Activity activity, int requestCode, int resultCode, Intent data) {
        if (onPhotoResultListener == null) {
            Log.e(tag, "onPhotoResultListener is not null");
            return;
        }
        if (resultCode != Activity.RESULT_OK) {
            return;
        }
        switch (requestCode) {
                // 拍照
            case INTENT_TAKE:
                if (new File(buildLocalFileUri().getPath()).exists()) {
                    if (mType == NO_CROP) {
                        // 不需要裁剪
                        onPhotoResultListener.onPhotoResult(buildLocalFileUri());
                        return;
                    }
                    if (corp(activity, buildUri(activity))) {
                        return;
                    }
                    onPhotoResultListener.onPhotoCancel();
                }
                break;
                // 选择图片
            case INTENT_SELECT:
                if (data != null && data.getData() != null) {
                    Uri imageUri = data.getData();
                    // 不需要裁剪
                    if (mType == NO_CROP) {
                        onPhotoResultListener.onPhotoResult(imageUri);
                        return;
                    }
                    if (corp(activity, imageUri)) {
                        return;
                    }
                }
                onPhotoResultListener.onPhotoCancel();
                break;

                // 裁剪图片
            case INTENT_CROP:
                onPhotoResultListener.onPhotoResult(buildCropUri(activity));
                break;
        }
    }

    /**
     * 返回结果处理
     *
     * @param requestCode
     * @param resultCode
     * @param data
     */
    public void onActivityResult(Fragment fragment, int requestCode, int resultCode, Intent data) {
        if (onPhotoResultListener == null) {
            Log.e(tag, "onPhotoResultListener is not null");
            return;
        }
        Activity activity = fragment.getActivity();
        if (activity == null) {
            return;
        }
        if (resultCode != Activity.RESULT_OK) {
            return;
        }
        switch (requestCode) {
                // 拍照
            case INTENT_TAKE:
                // 不需要裁剪
                if (mType == NO_CROP) {
                    onPhotoResultListener.onPhotoResult(buildUri(fragment.getActivity()));
                    return;
                }
                if (corp(fragment, buildUri(fragment.getActivity()))) {
                    return;
                }
                onPhotoResultListener.onPhotoCancel();
                break;
                // 选择图片
            case INTENT_SELECT:
                if (data != null && data.getData() != null) {
                    Uri imageUri = data.getData();
                    // 不需要裁剪
                    if (mType == NO_CROP) {
                        onPhotoResultListener.onPhotoResult(imageUri);
                        return;
                    }
                    if (corp(fragment, imageUri)) {
                        return;
                    }
                }
                onPhotoResultListener.onPhotoCancel();
                break;

                // 裁剪图片
            case INTENT_CROP:
                onPhotoResultListener.onPhotoResult(buildCropUri(activity));
                break;
        }
    }

    /**
     * 删除文件
     *
     * @param uri
     * @return
     */
    public boolean clearCropFile(Uri uri) {
        if (uri == null) {
            return false;
        }

        File file = new File(uri.getPath());
        if (file.exists()) {
            boolean result = file.delete();
            if (result) {
                Log.i(tag, "Cached crop file cleared.");
            } else {
                Log.e(tag, "Failed to clear cached crop file.");
            }
            return result;
        } else {
            Log.w(tag, "Trying to clear cached crop file but it does not exist.");
        }

        return false;
    }

    // 处理资源清理回收等操作
    private void onCleared(Activity activity) {
        clearCropFile(buildUri(activity));
        clearCropFile(buildLocalFileUri());
        clearCropFile(buildCropUri(activity));
    }

    /**
     * [回调监听类]
     *
     * @author huxinwu
     * @version 1.0
     * @date 2015-1-7
     */
    public interface OnPhotoResultListener {
        void onPhotoResult(Uri uri);

        void onPhotoCancel();
    }

    public OnPhotoResultListener getOnPhotoResultListener() {
        return onPhotoResultListener;
    }

    public void setOnPhotoResultListener(OnPhotoResultListener onPhotoResultListener) {
        this.onPhotoResultListener = onPhotoResultListener;
    }
}
