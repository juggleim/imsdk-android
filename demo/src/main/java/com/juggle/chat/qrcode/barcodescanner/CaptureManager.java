package com.juggle.chat.qrcode.barcodescanner;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.util.Log;
import android.view.Display;
import android.view.Surface;
import android.view.Window;
import android.view.WindowManager;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.multidex.BuildConfig;

import com.google.zxing.ResultMetadataType;
import com.google.zxing.ResultPoint;
import com.juggle.chat.R;
import com.juggle.chat.qrcode.client.BeepManager;
import com.juggle.chat.qrcode.client.InactivityTimer;
import com.juggle.chat.qrcode.client.Intents;
import com.juggle.chat.utils.OSUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Manages barcode scanning for a ScanActivity. This class may be used to have a custom Activity
 * (e.g. with a customized look and feel, or a different superclass), but not the barcode scanning
 * process itself.
 *
 * <p>This is intended for an Activity that is dedicated to capturing a single barcode and returning
 * it via setResult(). For other use cases, use DefaultBarcodeScannerView or BarcodeView directly.
 *
 * <p>The following is managed by this class: - Orientation lock - InactivityTimer - BeepManager -
 * Initializing from an Intent (via IntentIntegrator) - Setting the result and finishing the
 * Activity when a barcode is scanned - Displaying camera errors
 */
public class CaptureManager {
    private static final String TAG = CaptureManager.class.getSimpleName();
    public static final String QR_RESULT = "qr_result";

    private static int cameraPermissionReqCode = 250;

    private static String PREFIX_HTTP = "http://";
    private static String PREFIX_HTTPS = "https://";

    private Activity activity;
    private DecoratedBarcodeView barcodeView;
    private int orientationLock = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED;
    private static final String SAVED_ORIENTATION_LOCK = "SAVED_ORIENTATION_LOCK";
    private boolean returnBarcodeImagePath = false;

    private boolean destroyed = false;

    private InactivityTimer inactivityTimer;
    private BeepManager beepManager;

    private Handler handler;

    private OnCaptureResultListener resultListener;

    private BarcodeCallback callback =
            new BarcodeCallback() {
                @Override
                public void barcodeResult(final BarcodeResult result) {
                    barcodeView.pause();
                    beepManager.playBeepSoundAndVibrate();

                    handler.post(
                            new Runnable() {
                                @Override
                                public void run() {
                                    returnResult(result);
                                }
                            });
                }

                @Override
                public void possibleResultPoints(List<ResultPoint> resultPoints) {}
            };

    private final CameraPreview.StateListener stateListener =
            new CameraPreview.StateListener() {
                @Override
                public void previewSized() {}

                @Override
                public void previewStarted() {}

                @Override
                public void previewStopped() {}

                @Override
                public void cameraError(Exception error) {
                    displayFrameworkBugMessageAndExit();
                }
            };

    public CaptureManager(Activity activity, DecoratedBarcodeView barcodeView) {
        this.activity = activity;
        this.barcodeView = barcodeView;
        barcodeView.getBarcodeView().addStateListener(stateListener);

        handler = new Handler();

        inactivityTimer =
                new InactivityTimer(
                        activity,
                        new Runnable() {
                            @Override
                            public void run() {
                                Log.d(TAG, "Finishing due to inactivity");
                                finish();
                            }
                        });

        beepManager = new BeepManager(activity);
    }

    /**
     * Perform initialization, according to preferences set in the intent.
     *
     * @param intent the intent containing the scanning preferences
     * @param savedInstanceState saved state, containing orientation lock
     */
    public void initializeFromIntent(Intent intent, Bundle savedInstanceState) {
        Window window = activity.getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        if (savedInstanceState != null) {
            // If the screen was locked and unlocked again, we may start in a different orientation
            // (even one not allowed by the manifest). In this case we restore the orientation we
            // were
            // previously locked to.
            this.orientationLock =
                    savedInstanceState.getInt(
                            SAVED_ORIENTATION_LOCK, ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
        }

        if (intent != null) {
            // Only lock the orientation if it's not locked to something else yet
            boolean orientationLocked =
                    intent.getBooleanExtra(Intents.Scan.ORIENTATION_LOCKED, true);
            if (orientationLocked) {
                lockOrientation();
            }

            if (Intents.Scan.ACTION.equals(intent.getAction())) {
                barcodeView.initializeFromIntent(intent);
            }

            if (!intent.getBooleanExtra(Intents.Scan.BEEP_ENABLED, true)) {
                beepManager.setBeepEnabled(false);
            }

            if (intent.hasExtra(Intents.Scan.TIMEOUT)) {
                Runnable runnable =
                        new Runnable() {
                            @Override
                            public void run() {
                                returnResultTimeout();
                            }
                        };
                handler.postDelayed(runnable, intent.getLongExtra(Intents.Scan.TIMEOUT, 0L));
            }

            if (intent.getBooleanExtra(Intents.Scan.BARCODE_IMAGE_ENABLED, false)) {
                returnBarcodeImagePath = true;
            }
        }
    }

    /** Lock display to current orientation. */
    protected void lockOrientation() {
        // Only get the orientation if it's not locked to one yet.
        if (this.orientationLock == ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED) {
            // Adapted from http://stackoverflow.com/a/14565436
            Display display = activity.getWindowManager().getDefaultDisplay();
            int rotation = display.getRotation();
            int baseOrientation = activity.getResources().getConfiguration().orientation;
            int orientation = 0;
            if (baseOrientation == Configuration.ORIENTATION_LANDSCAPE) {
                if (rotation == Surface.ROTATION_0 || rotation == Surface.ROTATION_90) {
                    orientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
                } else {
                    orientation = ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE;
                }
            } else if (baseOrientation == Configuration.ORIENTATION_PORTRAIT) {
                if (rotation == Surface.ROTATION_0 || rotation == Surface.ROTATION_270) {
                    orientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
                } else {
                    orientation = ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT;
                }
            }

            this.orientationLock = orientation;
        }
        //noinspection ResourceType
        activity.setRequestedOrientation(this.orientationLock);
    }

    /** Start decoding. */
    public void decode() {
        barcodeView.decodeSingle(callback);
    }

    public void stopDecode() {
        barcodeView.stopDecoding();
    }

    /** Call from Activity#onResume(). */
    public void onResume() {
        if (Build.VERSION.SDK_INT >= 23) {
            openCameraWithPermission();
        } else {
            barcodeView.resume();
        }
        inactivityTimer.start();
    }

    private boolean askedPermission = false;

    @TargetApi(23)
    private void openCameraWithPermission() {
        if (ContextCompat.checkSelfPermission(this.activity, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
            barcodeView.resume();
        } else if (!askedPermission) {
            ActivityCompat.requestPermissions(
                    this.activity,
                    new String[] {Manifest.permission.CAMERA},
                    cameraPermissionReqCode);
            askedPermission = true;
        } else {
            // Wait for permission result
        }
    }

    /**
     * Call from Activity#onRequestPermissionsResult
     *
     * @param requestCode The request code passed in {@link
     *     ActivityCompat#requestPermissions(Activity, String[], int)}.
     * @param permissions The requested permissions.
     * @param grantResults The grant results for the corresponding permissions which is either
     *     {@link PackageManager#PERMISSION_GRANTED} or {@link PackageManager#PERMISSION_DENIED}.
     *     Never null.
     */
    public void onRequestPermissionsResult(
            int requestCode, String permissions[], int[] grantResults) {
        if (requestCode == cameraPermissionReqCode) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // permission was granted
                barcodeView.resume();
            } else {
                // TODO: display better error message.
                //                displayFrameworkBugMessageAndExit();
                gotoCameraPermissionSettingPage();
            }
        }
    }

    private void gotoCameraPermissionSettingPage() {
        if (activity.isFinishing() || this.destroyed) {
            return;
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle(activity.getString(R.string.app_name));
        builder.setMessage(activity.getString(R.string.zxing_msg_camera_permission_grant_needed));
        builder.setPositiveButton(
                com.jet.im.kit.R.string.j_confirm,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        showPermissionSettingPage();
                        finish();
                    }
                });
        builder.setOnCancelListener(
                new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        finish();
                    }
                });
        builder.show();
    }

    private void showPermissionSettingPage() {
        if (activity.isFinishing() || this.destroyed) {
            return;
        }
        try {
            if (OSUtils.isEmui()) {
                showHWPermissionSettingPage();
            } else if (OSUtils.isFlyme()) {
                showMeiZuPermissionSettingPage();
            } else if (OSUtils.isMiui()) {
                showXiaomiPermissionSettingPage();
            } else if (OSUtils.isOppo()) {
                showOppoPermissionSettingPage();
            } else {
                showDefaultPermissionSettingPage();
            }
        } catch (Exception e) {
            showDefaultPermissionSettingPage();
        }
    }

    private void showDefaultPermissionSettingPage() {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", activity.getPackageName(), null);
        intent.setData(uri);
        activity.startActivity(intent);
    }

    private void showHWPermissionSettingPage() {
        Intent intent = new Intent();
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra("packageName", BuildConfig.APPLICATION_ID);
        ComponentName comp =
                new ComponentName(
                        "com.huawei.systemmanager", "com.huawei.permissionmanager.ui.MainActivity");
        intent.setComponent(comp);
        activity.startActivity(intent);
    }

    private void showMeiZuPermissionSettingPage() {
        Intent intent = new Intent("com.meizu.safe.security.SHOW_APPSEC");
        intent.addCategory(Intent.CATEGORY_DEFAULT);
        intent.putExtra("packageName", BuildConfig.APPLICATION_ID);
        activity.startActivity(intent);
    }

    private void showXiaomiPermissionSettingPage() {
        Intent localIntent = new Intent("miui.intent.action.APP_PERM_EDITOR");
        localIntent.setClassName(
                "com.miui.securitycenter",
                "com.miui.permcenter.permissions.PermissionsEditorActivity");
        localIntent.putExtra("extra_pkgname", activity.getPackageName());
        activity.startActivity(localIntent);
    }

    private void showOppoPermissionSettingPage() {
        Intent intent = new Intent();
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra("packageName", BuildConfig.APPLICATION_ID);
        ComponentName comp =
                new ComponentName(
                        "com.color.safecenter",
                        "com.color.safecenter.permission.PermissionManagerActivity");
        intent.setComponent(comp);
        activity.startActivity(intent);
    }

    /** Call from Activity#onPause(). */
    public void onPause() {
        barcodeView.pause();

        inactivityTimer.cancel();
    }

    /** Call from Activity#onDestroy(). */
    public void onDestroy() {
        destroyed = true;
        inactivityTimer.cancel();
    }

    /** Call from Activity#onSaveInstanceState(). */
    public void onSaveInstanceState(Bundle outState) {
        outState.putInt(SAVED_ORIENTATION_LOCK, this.orientationLock);
    }

    /**
     * Create a intent to return as the Activity result.
     *
     * @param rawResult the BarcodeResult, must not be null.
     * @param barcodeImagePath a path to an exported file of the Barcode Image, can be null.
     * @return the Intent
     */
    public static Intent resultIntent(BarcodeResult rawResult, String barcodeImagePath) {
        Intent intent = new Intent(Intents.Scan.ACTION);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        intent.putExtra(Intents.Scan.RESULT, rawResult.toString());
        intent.putExtra(Intents.Scan.RESULT_FORMAT, rawResult.getBarcodeFormat().toString());
        byte[] rawBytes = rawResult.getRawBytes();
        if (rawBytes != null && rawBytes.length > 0) {
            intent.putExtra(Intents.Scan.RESULT_BYTES, rawBytes);
        }
        Map<ResultMetadataType, ?> metadata = rawResult.getResultMetadata();
        if (metadata != null) {
            if (metadata.containsKey(ResultMetadataType.UPC_EAN_EXTENSION)) {
                intent.putExtra(
                        Intents.Scan.RESULT_UPC_EAN_EXTENSION,
                        metadata.get(ResultMetadataType.UPC_EAN_EXTENSION).toString());
            }
            Number orientation = (Number) metadata.get(ResultMetadataType.ORIENTATION);
            if (orientation != null) {
                intent.putExtra(Intents.Scan.RESULT_ORIENTATION, orientation.intValue());
            }
            String ecLevel = (String) metadata.get(ResultMetadataType.ERROR_CORRECTION_LEVEL);
            if (ecLevel != null) {
                intent.putExtra(Intents.Scan.RESULT_ERROR_CORRECTION_LEVEL, ecLevel);
            }
            @SuppressWarnings("unchecked")
            Iterable<byte[]> byteSegments =
                    (Iterable<byte[]>) metadata.get(ResultMetadataType.BYTE_SEGMENTS);
            if (byteSegments != null) {
                int i = 0;
                for (byte[] byteSegment : byteSegments) {
                    intent.putExtra(Intents.Scan.RESULT_BYTE_SEGMENTS_PREFIX + i, byteSegment);
                    i++;
                }
            }
        }
        if (barcodeImagePath != null) {
            intent.putExtra(Intents.Scan.RESULT_BARCODE_IMAGE_PATH, barcodeImagePath);
        }
        return intent;
    }

    /**
     * Save the barcode image to a temporary file stored in the application's cache, and return its
     * path. Only does so if returnBarcodeImagePath is enabled.
     *
     * @param rawResult the BarcodeResult, must not be null
     * @return the path or null
     */
    private String getBarcodeImagePath(BarcodeResult rawResult) {
        String barcodeImagePath = null;
        if (returnBarcodeImagePath) {
            Bitmap bmp = rawResult.getBitmap();
            try {
                File bitmapFile =
                        File.createTempFile("barcodeimage", ".jpg", activity.getCacheDir());
                FileOutputStream outputStream = new FileOutputStream(bitmapFile);
                bmp.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
                outputStream.close();
                barcodeImagePath = bitmapFile.getAbsolutePath();
            } catch (IOException e) {
                Log.w(TAG, "Unable to create temporary file and store bitmap! " + e);
            }
        }
        return barcodeImagePath;
    }

    private void finish() {
        activity.finish();
    }

    protected void returnResultTimeout() {
        Intent intent = new Intent(Intents.Scan.ACTION);
        intent.putExtra(Intents.Scan.TIMEOUT, true);
        activity.setResult(Activity.RESULT_CANCELED, intent);
        finish();
    }

    protected void returnResult(BarcodeResult rawResult) {
//        SLog.d(TAG, "scanner returnResult:" + rawResult.toString());
        if (resultListener != null) {
            resultListener.onCaptureResult(rawResult);
        } else {
            Intent intent = resultIntent(rawResult, getBarcodeImagePath(rawResult));
            activity.setResult(Activity.RESULT_OK, intent);
        }
    }

    protected void displayFrameworkBugMessageAndExit() {
        if (activity.isFinishing() || this.destroyed) {
            return;
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle(activity.getString(R.string.app_name));
        builder.setMessage(activity.getString(R.string.zxing_msg_camera_framework_bug));
        builder.setPositiveButton(
                com.jet.im.kit.R.string.j_confirm,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                });
        builder.setOnCancelListener(
                new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        finish();
                    }
                });
        builder.show();
    }

    public static int getCameraPermissionReqCode() {
        return cameraPermissionReqCode;
    }

    public static void setCameraPermissionReqCode(int cameraPermissionReqCode) {
        CaptureManager.cameraPermissionReqCode = cameraPermissionReqCode;
    }

    /**
     * Set capture result listener.
     *
     * @param listener
     */
    public void setOnCaptureResultListener(OnCaptureResultListener listener) {
        this.resultListener = listener;
    }

    public interface OnCaptureResultListener {
        void onCaptureResult(BarcodeResult result);
    }
}
