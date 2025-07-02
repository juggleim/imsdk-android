package com.jet.im.kit.call;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.juggle.im.JIM;
import com.juggle.im.call.CallConst;
import com.juggle.im.call.ICallSession;

public class BaseCallActivity extends Activity {
    private long time = 0;
    private Runnable updateTimeRunnable;
    protected Handler handler;
    protected ICallSession mCallSession;

    public void setupTime(final TextView timeView) {
        try {
            if (updateTimeRunnable != null) {
                handler.removeCallbacks(updateTimeRunnable);
            }
            timeView.setVisibility(View.VISIBLE);
            updateTimeRunnable = new UpdateTimeRunnable(timeView);
            handler.post(updateTimeRunnable);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void cancelTime(){
        if (handler!=null && updateTimeRunnable !=null){
            handler.removeCallbacks(updateTimeRunnable);
        }
    }

    public long getTime() {
        return time;
    }

    private class UpdateTimeRunnable implements Runnable {
        private TextView timeView;

        public UpdateTimeRunnable(TextView timeView) {
            this.timeView = timeView;
        }

        @Override
        public void run() {
            time++;
            if (time >= 3600) {
                timeView.setText(String.format("%d:%02d:%02d", time / 3600, (time % 3600) / 60, (time % 60)));
            } else {
                timeView.setText(String.format("%02d:%02d", (time % 3600) / 60, (time % 60)));
            }
            handler.postDelayed(this, 1000);
        }
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        handler = new Handler();

        Intent intent = getIntent();
        String callId = intent.getStringExtra("callId");
        mCallSession = JIM.getInstance().getCallManager().getCallSession(callId);
        if (mCallSession == null) {
            finish();
            return;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        try {
            if (mCallSession != null) {
                if (mCallSession.getMediaType() == CallConst.CallMediaType.VIDEO) {
//                    mCallSession.startPreview();
//                    RongCallClient.getInstance().startCapture();
                }
//                RongCallProxy.getInstance().setCallListener(this);
//                if (shouldRestoreFloat) {
//                    CallFloatBoxView.hideFloatBox();
//                    NotificationUtil.clearNotification(this, BaseCallActivity.CALL_NOTIFICATION_ID);
//                }
                long activeTime = mCallSession != null ? mCallSession.getConnectTime() : 0;
                time = activeTime == 0 ? 0 : (System.currentTimeMillis() - activeTime) / 1000;
//                shouldRestoreFloat = true;
//                if (time > 0) {
//                    CallKitUtils.shouldShowFloat = true;
//                }
//                if (checkingOverlaysPermission) {
//                    checkDrawOverlaysPermission(false);
//                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        try {
            handler.removeCallbacks(updateTimeRunnable);
//            unregisterReceiver(mRingModeReceiver);
//            if (mMediaPlayer != null) {
//                if (mMediaPlayer.isPlaying()) {
//                    mMediaPlayer.stop();
//                }
//                mMediaPlayer.release();
//                mMediaPlayer = null;
//            }

            // 退出此页面后应设置成正常模式，否则按下音量键无法更改其他音频类型的音量
//            AudioManager am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
//            if (am != null) {
//                am.setMode(AudioManager.MODE_NORMAL);
//                if (onAudioFocusChangeListener != null) {
//                    am.abandonAudioFocus(onAudioFocusChangeListener);
//                }
//            }
        } catch (IllegalStateException e) {
            e.printStackTrace();
//            Log.i(MEDIAPLAYERTAG,"--- onDestroy IllegalStateException---");
        }
        super.onDestroy();
//        unRegisterHeadsetplugReceiver();
//        if (wakeLock != null && wakeLock.isHeld()) {
//            wakeLock.release();
//        }
//
//        if (screenLock != null && screenLock.isHeld()) {
//            try {
//                screenLock.setReferenceCounted(false);
//                screenLock.release();
//            } catch (Exception e) {
//
//            }
//        }
    }
}
