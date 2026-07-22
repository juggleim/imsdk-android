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

    /** ImageSuccessReturn */
    public static final int INTENT_CROP = 2;
    /** SuccessReturn */
    public static final int INTENT_TAKE = 3;
    /** SuccessReturn */
    public static final int INTENT_SELECT = 4;

    public static final String CROP_FILE_NAME = "crop_file.jpg";

    // Image
    public static final int NO_CROP = 0x1772;
    private int mType;
    // RMediaStoreSaveUriHandle
    private static Uri lastCropUriForR;

    /** PhotoUtils */
    private OnPhotoResultListener onPhotoResultListener;

    public PhotoUtils(OnPhotoResultListener onPhotoResultListener) {
        this.onPhotoResultListener = onPhotoResultListener;
    }

    public PhotoUtils(OnPhotoResultListener onPhotoResultListener, int type) {
        this.onPhotoResultListener = onPhotoResultListener;
        mType = type;
    }

    /**
     *
     *
     * @param
     * @return
     */
    public void takePicture(Activity activity) {
        try {
            // SelectImageImageDelete
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
     *
     *
     * @param
     * @return
     */
    public void takePicture(Fragment fragment) {
        try {
            // SelectImageImageDelete
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
     * SelectImage Image，image/*，Set ：image/jpeg
     *
     * @param activity Activity
     */
    @SuppressLint("InlinedApi")
    public void selectPicture(Activity activity) {
        try {
            // SelectImageImageDelete
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
     * SelectImage Image，image/*，Set ：image/jpeg
     *
     * @param fragment Fragment
     */
    @SuppressLint("InlinedApi")
    public void selectPicture(Fragment fragment) {
        try {
            // SelectImageImageDelete
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
     * uri
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
     * Fileuri
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

    // R，CreateFileMediaStore
    private Uri createCropImageUriForR(Context context) {
        if (context == null) {
            return null;
        }
        try {
            File imgFile = new File(getCropFilePath(context));
            //  MediaStore API file Saveuri（AppPermission， MediaStore API）
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

    // RMediaStoreFile
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
                // ：File，
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
     * ReturnHandle
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
                //
            case INTENT_TAKE:
                if (new File(buildLocalFileUri().getPath()).exists()) {
                    if (mType == NO_CROP) {
                        //
                        onPhotoResultListener.onPhotoResult(buildLocalFileUri());
                        return;
                    }
                    if (corp(activity, buildUri(activity))) {
                        return;
                    }
                    onPhotoResultListener.onPhotoCancel();
                }
                break;
                // SelectImage
            case INTENT_SELECT:
                if (data != null && data.getData() != null) {
                    Uri imageUri = data.getData();
                    //
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

                // Image
            case INTENT_CROP:
                onPhotoResultListener.onPhotoResult(buildCropUri(activity));
                break;
        }
    }

    /**
     * ReturnHandle
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
                //
            case INTENT_TAKE:
                //
                if (mType == NO_CROP) {
                    onPhotoResultListener.onPhotoResult(buildUri(fragment.getActivity()));
                    return;
                }
                if (corp(fragment, buildUri(fragment.getActivity()))) {
                    return;
                }
                onPhotoResultListener.onPhotoCancel();
                break;
                // SelectImage
            case INTENT_SELECT:
                if (data != null && data.getData() != null) {
                    Uri imageUri = data.getData();
                    //
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

                // Image
            case INTENT_CROP:
                onPhotoResultListener.onPhotoResult(buildCropUri(activity));
                break;
        }
    }

    /**
     * DeleteFile
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

    // Handle
    private void onCleared(Activity activity) {
        clearCropFile(buildUri(activity));
        clearCropFile(buildLocalFileUri());
        clearCropFile(buildCropUri(activity));
    }

    /**
     * [CallbackListener]
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
