package com.juggle.chat.qrcode;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;

import com.jet.im.kit.SendbirdUIKit;
import com.jet.im.kit.activities.ChannelActivity;
import com.jet.im.kit.activities.PersonInfoActivity;
import com.jet.im.kit.modules.components.StateHeaderComponent;
import com.juggle.chat.R;
import com.juggle.chat.bean.GroupDetailBean;
import com.juggle.chat.bean.GroupMemberBean;
import com.juggle.chat.bean.HttpResult;
import com.juggle.chat.bean.UserInfoBean;
import com.juggle.chat.component.HeadComponent;
import com.juggle.chat.http.CustomCallback;
import com.juggle.chat.http.ServiceManager;
import com.juggle.chat.qrcode.barcodescanner.BarcodeResult;
import com.juggle.chat.qrcode.barcodescanner.CaptureManager;
import com.juggle.chat.qrcode.barcodescanner.DecoratedBarcodeView;
import com.juggle.chat.settings.UserDetailActivity;
import com.juggle.chat.utils.NetworkUtils;
import com.juggle.chat.utils.PhotoUtils;
import com.juggle.chat.utils.ToastUtils;
import com.juggle.im.model.Conversation;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

import okhttp3.RequestBody;

/** 扫一扫界面 */
public class ScanActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = "ScanActivity";
    private static final String ACTION = "action";
    private static final String LOGIN = "login";
    private static final String CODE = "code";
    private static final String JOIN_GROUP = "join_group";
    private static final String GROUP_ID = "group_id";
    private static final String ADD_FRIEND = "add_friend";
    private static final String USER_ID = "user_id";
    private CaptureManager capture;
    private DecoratedBarcodeView barcodeScannerView;
    private TextView lightControlTv;
    private TextView selectPicTv;
    private TextView tipsTv;
    private PhotoUtils photoUtils;

    private boolean isCameraLightOn = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initView(savedInstanceState);

        photoUtils =
                new PhotoUtils(
                        new PhotoUtils.OnPhotoResultListener() {
                            @Override
                            public void onPhotoResult(Uri uri) {
                                String result = QRCodeUtils.analyzeImage(uri.getPath());
                                handleQrCode(result);
                            }

                            @Override
                            public void onPhotoCancel() {}
                        });
    }

    private void initView(Bundle savedInstanceState) {
        barcodeScannerView = initializeContent();
        capture = new CaptureManager(this, barcodeScannerView);
        capture.initializeFromIntent(getIntent(), savedInstanceState);
        capture.setOnCaptureResultListener(
                new CaptureManager.OnCaptureResultListener() {
                    @Override
                    public void onCaptureResult(BarcodeResult result) {
                        handleQrCode(result.toString());
                    }
                });

        barcodeScannerView.getViewFinder().networkChange(!NetworkUtils.isNetWorkAvailable(this));
        if (!NetworkUtils.isNetWorkAvailable(this)) {
            capture.stopDecode();
        } else {
            capture.decode();
        }
        barcodeScannerView.setTorchListener(
                new DecoratedBarcodeView.TorchListener() {
                    @Override
                    public void onTorchOn() {
                        lightControlTv.setText(R.string.zxing_close_light);
                        isCameraLightOn = true;
                    }

                    @Override
                    public void onTorchOff() {
                        lightControlTv.setText(R.string.zxing_open_light);
                        isCameraLightOn = false;
                    }
                });
        lightControlTv = findViewById(R.id.zxing_open_light);
        lightControlTv.setOnClickListener(this);
//        selectPicTv = findViewById(R.id.zxing_select_pic);
//        selectPicTv.setOnClickListener(this);
        tipsTv = findViewById(R.id.zxing_user_tips);
    }

    /**
     * Override to use a different layout.
     *
     * @return the DecoratedBarcodeView
     */
    protected DecoratedBarcodeView initializeContent() {
        StateHeaderComponent headerComponent = new StateHeaderComponent();
        headerComponent.getParams().setTitle(getString(R.string.text_scan_qrcode));
        headerComponent.getParams().setUseLeftButton(true);
        headerComponent.getParams().setUseRightButton(false);
        headerComponent.getParams().setLeftButtonIcon(getDrawable(R.drawable.icon_back));
        headerComponent.getParams().setLeftButtonIconTint(SendbirdUIKit.getDefaultThemeMode().getPrimaryTintColorStateList(this));
//        headerComponent.getParams().setRightButtonText(getString(R.string.text_album));
//        headerComponent.setOnRightButtonClickListener(v -> {
//            scanFromAlbum();
//        });
        headerComponent.setOnLeftButtonClickListener(v -> {
            finish();
        });

        setContentView(R.layout.zxing_capture);
        FrameLayout parent = findViewById(R.id.headerComponent);
        View header = headerComponent.onCreateView(this, getLayoutInflater(), parent, null);
        parent.addView(header);
        return (DecoratedBarcodeView) findViewById(R.id.zxing_barcode_scanner);
    }

    /** 切换摄像头照明 */
    private void switchCameraLight() {
        if (isCameraLightOn) {
            barcodeScannerView.setTorchOff();
        } else {
            barcodeScannerView.setTorchOn();
        }
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.zxing_open_light) {
            switchCameraLight();
        }
//        else if (v.getId() == R.id.zxing_select_pic) {
//            scanFromAlbum();
//        }
    }

    /** 从相册中选中 */
    public void scanFromAlbum() {
        photoUtils.selectPicture(this);
    }

    /**
     * 处理二维码结果，并跳转到相应界面
     *
     * @param qrCodeText
     */
    private void handleQrCode(String qrCodeText) {
        if (TextUtils.isEmpty(qrCodeText)) {
            ToastUtils.show(getString(R.string.zxing_qr_can_not_recognized));
            return;
        }

        try {
            JSONObject jsonObject = new JSONObject(qrCodeText);
            String action = null;
            if (jsonObject.has(ACTION)) {
                action = jsonObject.optString(ACTION);
            }
            if (TextUtils.isEmpty(action)) {
                return;
            }
            if (action.equals(LOGIN)) {
                String code = jsonObject.optString(CODE);
                HashMap<String, Object> map = new HashMap<>();
                map.put("id", code);
                RequestBody body = ServiceManager.createJsonRequest(map);
                ServiceManager.loginService().qrcodeConfirm(body).enqueue(new CustomCallback<HttpResult<Void>, Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        finish();
                    }
                });
            } else if (action.equals(JOIN_GROUP)) {
                String groupId = jsonObject.optString(GROUP_ID);
                ServiceManager.getGroupsService().getGroupDetail(groupId).enqueue(new CustomCallback<HttpResult<GroupDetailBean>, GroupDetailBean>() {
                    @Override
                    public void onSuccess(GroupDetailBean groupDetailBean) {
                        //Not member
                        if (groupDetailBean.getMyRole() == 3) {

                        } else {
                            startActivity(ChannelActivity.newIntent(ScanActivity.this, Conversation.ConversationType.GROUP.getValue(), groupId));
                            finish();
                        }
                    }
                });
            } else if (action.equals(ADD_FRIEND)) {
                String userId = jsonObject.optString(USER_ID);
                ServiceManager.getUserService().getUserInfo(userId).enqueue(new CustomCallback<HttpResult<UserInfoBean>, UserInfoBean>() {
                    @Override
                    public void onSuccess(UserInfoBean userInfoBean) {
                        GroupMemberBean bean = new GroupMemberBean();
                        bean.setUserId(userId);
                        bean.setNickname(userInfoBean.getNickname());
                        bean.setAvatar(userInfoBean.getAvatar());
                        startActivity(UserDetailActivity.newIntent(ScanActivity.this, bean));
                        finish();
                    }
                });
            }
        } catch (JSONException e) {
            Log.e(TAG, "JSONException: " + e.getMessage());
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        capture.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        capture.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        capture.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        capture.onSaveInstanceState(outState);
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        capture.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return barcodeScannerView.onKeyDown(keyCode, event) || super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        photoUtils.onActivityResult(this, requestCode, resultCode, data);
    }
}
