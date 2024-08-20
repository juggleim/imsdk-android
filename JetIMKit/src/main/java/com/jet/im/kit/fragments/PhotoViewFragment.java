package com.jet.im.kit.fragments;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestBuilder;
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.load.resource.gif.GifDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.github.chrisbanes.photoview.PhotoViewAttacher;
import com.juggle.im.model.Conversation;
import com.sendbird.android.channel.BaseChannel;
import com.sendbird.android.channel.ChannelType;
import com.sendbird.android.channel.GroupChannel;
import com.sendbird.android.channel.OpenChannel;
import com.sendbird.android.exception.SendbirdException;
import com.jet.im.kit.R;
import com.jet.im.kit.SendbirdUIKit;
import com.jet.im.kit.consts.StringSet;
import com.jet.im.kit.databinding.SbFragmentPhotoViewBinding;
import com.jet.im.kit.interfaces.LoadingDialogHandler;
import com.jet.im.kit.internal.model.GlideCachedUrlLoader;
import com.jet.im.kit.internal.tasks.JobResultTask;
import com.jet.im.kit.internal.tasks.TaskQueue;
import com.jet.im.kit.log.Logger;
import com.jet.im.kit.utils.DateUtils;
import com.jet.im.kit.utils.DialogUtils;
import com.jet.im.kit.utils.MessageUtils;
import com.jet.im.kit.utils.TextUtils;
import com.jet.im.kit.vm.FileDownloader;

public class PhotoViewFragment extends PermissionFragment implements PermissionFragment.PermissionHandler, LoadingDialogHandler {
    @NonNull
    private final String[] REQUIRED_PERMISSIONS = {Manifest.permission.WRITE_EXTERNAL_STORAGE};

    private SbFragmentPhotoViewBinding binding;
    private Conversation channel;

    private boolean loadComplete = false;

    @Nullable
    private String fileName;
    @Nullable
    private String channelUrl;
    @Nullable
    private String url;
    @Nullable
    private String plainUrl;
    @Nullable
    private String requestId;
    @Nullable
    private String mimeType;
    @Nullable
    private String senderNickname;
    private long createdAt;
    private long messageId;
    private boolean isDeletableMessage;
    @Nullable
    private ChannelType channelType = ChannelType.GROUP;
    @Nullable
    private LoadingDialogHandler loadingDialogHandler;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = SbFragmentPhotoViewBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        Logger.d("PhotoViewFragment::onViewCreated()");
        binding.ivClose.setOnClickListener(v -> shouldActivityFinish());

        Bundle args = getArguments();
        if (args != null) {
            String senderId = args.getString(StringSet.KEY_SENDER_ID);
            fileName = args.getString(StringSet.KEY_MESSAGE_FILENAME);
            channelUrl = args.getString(StringSet.KEY_CHANNEL_URL);
            url = args.getString(StringSet.KEY_IMAGE_URL);
            plainUrl = args.getString(StringSet.KEY_IMAGE_PLAIN_URL);
            requestId = args.getString(StringSet.KEY_REQUEST_ID);
            mimeType = args.getString(StringSet.KEY_MESSAGE_MIMETYPE);
            senderNickname = args.getString(StringSet.KEY_MESSAGE_SENDER_NAME);
            createdAt = args.getLong(StringSet.KEY_MESSAGE_CREATEDAT);
            messageId = args.getLong(StringSet.KEY_MESSAGE_ID);
            isDeletableMessage = args.getBoolean(StringSet.KEY_DELETABLE_MESSAGE, MessageUtils.isMine(senderId));

            if (args.containsKey(StringSet.KEY_CHANNEL_TYPE)) {
                channelType = (ChannelType) args.getSerializable(StringSet.KEY_CHANNEL_TYPE);
            }
        }

        if (loadingDialogHandler == null) {
            loadingDialogHandler = this;
        }

        if (TextUtils.isEmpty(channelUrl)) return;

//        if (channelType == ChannelType.GROUP) {
//            GroupChannel.getChannel(channelUrl, (channel, e) -> {
//                PhotoViewFragment.this.channel = channel;
//                onDrawPage();
//            });
//        } else {
//            OpenChannel.getChannel(channelUrl, (channel, e) -> {
//                PhotoViewFragment.this.channel = channel;
//                onDrawPage();
//            });
//        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!isFragmentAlive()) return;
        requireActivity().getWindow().setNavigationBarColor(ContextCompat.getColor(requireContext(), R.color.background_700));
        requireActivity().getWindow().getDecorView().setSystemUiVisibility(
            View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
                View.SYSTEM_UI_FLAG_FULLSCREEN |
                View.SYSTEM_UI_FLAG_LOW_PROFILE |
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
    }

    private <T> RequestBuilder<T> makeRequestBuilder(@NonNull String url, @NonNull String cacheKey, @NonNull Class<T> clazz) {
        final View loading = binding.loading;
        final RequestManager glide = Glide.with(this);

        return GlideCachedUrlLoader.load(glide.as(clazz), url, cacheKey).diskCacheStrategy(DiskCacheStrategy.ALL).listener(new RequestListener<T>() {
            @Override
            public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<T> target, boolean isFirstResource) {
                if (!isFragmentAlive()) return false;
                requireActivity().runOnUiThread(() -> loading.setVisibility(View.GONE));
                return false;
            }

            @Override
            public boolean onResourceReady(T resource, Object model, Target<T> target, DataSource dataSource, boolean isFirstResource) {
                if (!isFragmentAlive()) return false;
                requireActivity().runOnUiThread(() -> {
                    loadComplete = true;
                    loading.setVisibility(View.GONE);
                });
                return false;
            }
        });
    }

    protected void onDrawPage() {
        if (!isFragmentAlive()) return;
        Logger.d("PhotoViewFragment::onDrawPage() - nickname:" + senderNickname);
        final ImageView ivPhoto = binding.ivPhoto;
        final ImageView ivDelete = binding.ivDelete;
        final ImageView ivDownload = binding.ivDownload;
        final TextView tvTitle = binding.tvTitle;
        final TextView tvCreatedAt = binding.tvCreatedAt;
        final View loading = binding.loading;
        final String url = this.url;
        final String plainUrl = this.plainUrl == null ? "" : this.plainUrl;
        final String requestId = this.requestId == null ? "" : this.requestId;

        tvTitle.setText(senderNickname);
        tvCreatedAt.setText(DateUtils.formatTime(requireContext(), this.createdAt));
        loading.setVisibility(View.VISIBLE);

        if (url != null) {
            if (mimeType != null && mimeType.toLowerCase().contains(StringSet.gif)) {
                makeRequestBuilder(url, generateCacheKey(plainUrl, requestId), GifDrawable.class).into(ivPhoto);
            } else {
                makeRequestBuilder(url, generateCacheKey(plainUrl, requestId), Bitmap.class).into(ivPhoto);
            }
        }

        if (channel != null && isDeletableMessage) {
            ivDelete.setVisibility(View.VISIBLE);
//            ivDelete.setOnClickListener(v -> {
//                    if (!loadComplete || getContext() == null) return;
//
//                    DialogUtils.showWarningDialog(
//                        requireContext(),
//                        getString(R.string.sb_text_dialog_delete_file_message),
//                        getString(R.string.sb_text_button_delete),
//                        v1 -> channel.deleteMessage(messageId, e -> {
//                            if (e != null) {
//                                toastError(R.string.sb_text_error_delete_message);
//                                return;
//                            }
//                            if (isFragmentAlive()) {
//                                shouldActivityFinish();
//                            }
//                        }),
//                        getString(R.string.sb_text_button_cancel),
//                        cancel -> Logger.dev("cancel"));
//                }
//            );
        } else {
            ivDelete.setVisibility(View.GONE);
            ivDelete.setOnClickListener(null);
        }

        ivDownload.setOnClickListener(v -> {
            if (!loadComplete) return;
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
                download();
            } else {
                requestPermission(REQUIRED_PERMISSIONS, (PermissionHandler) this);
            }
        });

        PhotoViewAttacher attacher = new PhotoViewAttacher(ivPhoto);
        attacher.setOnPhotoTapListener((view, x, y) -> togglePhotoActionBar());
    }

    @NonNull
    private String generateCacheKey(@NonNull String plainUrl, @NonNull String requestId) {
        return (TextUtils.isNotEmpty(requestId)) ? requestId : String.valueOf(plainUrl.hashCode());
    }

    private void togglePhotoActionBar() {
        View vgHeader = binding.vgHeader;
        View vgBottom = binding.vgBottom;

        if (vgHeader.getVisibility() == View.GONE) {
            vgHeader.animate()
                .setDuration(300)
                .alpha(1.0f)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationStart(Animator animation) {
                        super.onAnimationStart(animation);
                        vgHeader.setVisibility(View.VISIBLE);
                    }
                });
        } else {
            vgHeader.animate()
                .setDuration(300)
                .alpha(0.0f)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        vgHeader.setVisibility(View.GONE);
                    }
                });
        }

        if (vgBottom.getVisibility() == View.GONE) {
            vgBottom.animate()
                .setDuration(300)
                .alpha(1.0f)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationStart(Animator animation) {
                        super.onAnimationStart(animation);
                        vgBottom.setVisibility(View.VISIBLE);
                    }
                });
        } else {
            vgBottom.animate()
                .setDuration(300)
                .alpha(0.0f)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        vgBottom.setVisibility(View.GONE);
                    }
                });
        }
    }

    @Override
    public void onPermissionGranted() {
        download();
    }

    private void download() {
        if (loadingDialogHandler != null) {
            loadingDialogHandler.shouldShowLoadingDialog();
        }
        TaskQueue.addTask(new JobResultTask<Boolean>() {
            @Override
            @Nullable
            public Boolean call() throws Exception {
                if (!isFragmentAlive()) return null;
                if (url == null || mimeType == null || fileName == null) return null;
                FileDownloader.getInstance().saveFile(requireContext(), url, mimeType, fileName);
                Logger.dev("++ file name : %s", fileName);
                return true;
            }

            @Override
            public void onResultForUiThread(@Nullable Boolean result, @Nullable SendbirdException e) {
                if (loadingDialogHandler != null) {
                    loadingDialogHandler.shouldDismissLoadingDialog();
                }

                if (e != null) {
                    Logger.e(e);
                }
                if (result != null && result) {
                    toastSuccess(R.string.sb_text_toast_success_download_file);
                } else {
                    toastError(R.string.sb_text_error_download_file);
                }
            }
        });
    }

    private void setLoadingDialogHandler(@Nullable LoadingDialogHandler loadingDialogHandler) {
        this.loadingDialogHandler = loadingDialogHandler;
    }

    /**
     * It will be called when the loading dialog needs displaying.
     *
     * @return True if the callback has consumed the event, false otherwise.
     * since 1.2.5
     */
    @Override
    public boolean shouldShowLoadingDialog() {
        showWaitingDialog();
        return true;
    }

    /**
     * It will be called when the loading dialog needs dismissing.
     *
     * since 1.2.5
     */
    @Override
    public void shouldDismissLoadingDialog() {
        dismissWaitingDialog();
    }

    public static class Builder {
        private final Bundle bundle = new Bundle();
        private LoadingDialogHandler loadingDialogHandler;

        public Builder(@Nullable String senderId, @Nullable String fileName, @Nullable String channelUrl,
                       @Nullable String url, @Nullable String plainUrl, @Nullable String requestId, @Nullable String mimeType, @Nullable String senderNickname, long createdAt,
                       long messageId, @Nullable ChannelType channelType, @Nullable SendbirdUIKit.ThemeMode themeMode, boolean isDeletableMessage) {
            bundle.putString(StringSet.KEY_SENDER_ID, senderId);
            bundle.putString(StringSet.KEY_MESSAGE_FILENAME, fileName);
            bundle.putString(StringSet.KEY_CHANNEL_URL, channelUrl);
            bundle.putString(StringSet.KEY_IMAGE_URL, url);
            bundle.putString(StringSet.KEY_IMAGE_PLAIN_URL, plainUrl);
            bundle.putString(StringSet.KEY_REQUEST_ID, requestId);
            bundle.putString(StringSet.KEY_MESSAGE_MIMETYPE, mimeType);
            bundle.putString(StringSet.KEY_MESSAGE_SENDER_NAME, senderNickname);
            bundle.putLong(StringSet.KEY_MESSAGE_CREATEDAT, createdAt);
            bundle.putLong(StringSet.KEY_MESSAGE_ID, messageId);
            bundle.putSerializable(StringSet.KEY_CHANNEL_TYPE, channelType);
            bundle.putBoolean(StringSet.KEY_DELETABLE_MESSAGE, isDeletableMessage);
        }

        /**
         * Sets the custom loading dialog handler
         *
         * @param loadingDialogHandler Interface definition for a callback to be invoked before when the loading dialog is called.
         * @see LoadingDialogHandler
         * since 1.2.5
         */
        @NonNull
        public Builder setLoadingDialogHandler(@Nullable LoadingDialogHandler loadingDialogHandler) {
            this.loadingDialogHandler = loadingDialogHandler;
            return this;
        }

        @NonNull
        public PhotoViewFragment build() {
            PhotoViewFragment fragment = new PhotoViewFragment();
            fragment.setArguments(bundle);
            fragment.setLoadingDialogHandler(loadingDialogHandler);
            return fragment;
        }
    }
}
