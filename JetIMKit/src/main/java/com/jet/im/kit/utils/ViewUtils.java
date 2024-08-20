package com.jet.im.kit.utils;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.style.TextAppearanceSpan;
import android.util.Pair;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.DimenRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.content.res.AppCompatResources;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestBuilder;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.jet.im.kit.R;
import com.jet.im.kit.SendbirdUIKit;
import com.jet.im.kit.consts.StringSet;
import com.jet.im.kit.interfaces.OnItemClickListener;
import com.jet.im.kit.internal.model.GlideCachedUrlLoader;
import com.jet.im.kit.internal.ui.messages.OgtagView;
import com.jet.im.kit.internal.ui.messages.VoiceMessageView;
import com.jet.im.kit.internal.ui.widgets.RoundCornerView;
import com.jet.im.kit.internal.ui.widgets.VoiceProgressView;
import com.jet.im.kit.log.Logger;
import com.jet.im.kit.model.FileInfo;
import com.jet.im.kit.model.MessageUIConfig;
import com.jet.im.kit.model.TextUIConfig;
import com.jet.im.kit.vm.PendingMessageRepository;
import com.juggle.im.JIM;
import com.juggle.im.model.Message;
import com.juggle.im.model.UserInfo;
import com.juggle.im.model.messages.ImageMessage;
import com.juggle.im.model.messages.TextMessage;
import com.juggle.im.model.messages.VoiceMessage;
import com.sendbird.android.message.BaseMessage;
import com.sendbird.android.message.FileMessage;
import com.sendbird.android.message.OGMetaData;
import com.sendbird.android.message.Thumbnail;
import com.sendbird.android.user.Sender;
import com.sendbird.android.user.User;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

/**
 * The helper class for the drawing views in the UIKit.
 * It is used to draw common UI from each custom component.
 */
public class ViewUtils {
    private final static int MINIMUM_THUMBNAIL_WIDTH = 100;
    private final static int MINIMUM_THUMBNAIL_HEIGHT = 100;
    public static final Pattern MENTION = Pattern.compile("[" + SendbirdUIKit.getUserMentionConfig().getTrigger() + "][{](.*?)([}])");

    public static void drawUnknownMessage(@NonNull TextView view, boolean isMine) {
        int unknownHintAppearance;
        if (isMine) {
            unknownHintAppearance = SendbirdUIKit.isDarkMode() ? R.style.SendbirdBody3OnLight02 : R.style.SendbirdBody3OnDark02;
        } else {
            unknownHintAppearance = SendbirdUIKit.isDarkMode() ? R.style.SendbirdBody3OnDark03 : R.style.SendbirdBody3OnLight02;
        }

        final int sizeOfFirstLine = 23;
        String unknownHintText = view.getContext().getResources().getString(R.string.sb_text_channel_unknown_type_text);
        final Spannable spannable = new SpannableString(unknownHintText);
        spannable.setSpan(new TextAppearanceSpan(view.getContext(), unknownHintAppearance), sizeOfFirstLine, spannable.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        view.setText(spannable);
    }

    public static void drawTextMessage(
            @NonNull TextView textView,
            @Nullable BaseMessage message,
            @Nullable MessageUIConfig uiConfig,
            boolean enableMention,
            @Nullable TextUIConfig mentionedCurrentUserUIConfig,
            @Nullable OnItemClickListener<User> mentionClickListener) {

    }

    public static void drawTextMessage(
            @NonNull TextView textView,
            @Nullable Message message,
            @Nullable MessageUIConfig uiConfig,
            boolean enableMention,
            @Nullable TextUIConfig mentionedCurrentUserUIConfig,
            @Nullable OnItemClickListener<User> mentionClickListener
    ) {
        if (message == null) {
            return;
        }

        if (MessageUtils.isUnknownType(message)) {
            drawUnknownMessage(textView, MessageUtils.isMine(message));
            return;
        }
        final Context context = textView.getContext();
        final CharSequence text = getDisplayableText(
                context,
                message,
                uiConfig,
                mentionedCurrentUserUIConfig,
                true,
                mentionClickListener,
                enableMention
        );
        textView.setText(text);
    }

    @NonNull
    public static CharSequence getDisplayableText(
            @NonNull Context context,
            @NonNull Message message,
            @Nullable MessageUIConfig uiConfig,
            @Nullable TextUIConfig mentionedCurrentUserUIConfig,
            boolean mentionClickable,
            @Nullable OnItemClickListener<User> mentionClickListener,
            boolean enabledMention
    ) {
        String displayedMessage = message.getContent() instanceof TextMessage ? ((TextMessage) message.getContent()).getContent() : "";
        final SpannableString text = new SpannableString(displayedMessage);
        if (uiConfig != null) {
            final TextUIConfig messageTextUIConfig = MessageUtils.isMine(message) ? uiConfig.getMyMessageTextUIConfig() : uiConfig.getOtherMessageTextUIConfig();
            messageTextUIConfig.bind(context, text, 0, text.length());
        }

        CharSequence displayText = text;
        return displayText;
    }

    @Nullable
    private static User getMentionedUser(@NonNull BaseMessage message, @NonNull String targetUserId) {
        final List<User> mentionedUserList = message.getMentionedUsers();
        for (User user : mentionedUserList) {
            if (user.getUserId().equals(targetUserId)) {
                return user;
            }
        }
        return null;
    }

    public static void drawOgtag(@NonNull ViewGroup parent, @Nullable OGMetaData ogMetaData) {
        if (ogMetaData == null) {
            return;
        }

        parent.removeAllViews();
        OgtagView ogtagView = OgtagView.inflate(parent.getContext(), parent);
        ogtagView.drawOgtag(ogMetaData);
        parent.setOnClickListener(v -> {
            if (ogMetaData.getUrl() == null) {
                return;
            }

            Intent intent = IntentUtils.getWebViewerIntent(ogMetaData.getUrl());
            try {
                ogtagView.getContext().startActivity(intent);
            } catch (ActivityNotFoundException e) {
                Logger.e(e);
            }
        });
    }

    public static void drawNickname(
            @NonNull TextView tvNickname,
            @Nullable BaseMessage message,
            @Nullable MessageUIConfig uiConfig,
            boolean isOperator
    ) {
        if (message == null) {
            return;
        }

        final Sender sender = message.getSender();
        final Spannable nickname = new SpannableString(UserUtils.getDisplayName(tvNickname.getContext(), sender));
        if (uiConfig != null) {
            final boolean isMine = MessageUtils.isMine(message);
            final TextUIConfig textUIConfig = isOperator ? uiConfig.getOperatorNicknameTextUIConfig() : (isMine ? uiConfig.getMyNicknameTextUIConfig() : uiConfig.getOtherNicknameTextUIConfig());
            textUIConfig.bind(tvNickname.getContext(), nickname, 0, nickname.length());
        }

        tvNickname.setText(nickname);
    }

    public static void drawNickname(
            @NonNull TextView tvNickname,
            @Nullable Message message,
            @Nullable MessageUIConfig uiConfig,
            boolean isOperator
    ) {
        if (message == null) {
            return;
        }

        final UserInfo sender = JIM.getInstance().getUserInfoManager().getUserInfo(message.getSenderUserId());
        final Spannable nickname = new SpannableString(UserUtils.getDisplayName(tvNickname.getContext(), sender));
        if (uiConfig != null) {
            final boolean isMine = MessageUtils.isMine(message);
            final TextUIConfig textUIConfig = isOperator ? uiConfig.getOperatorNicknameTextUIConfig() : (isMine ? uiConfig.getMyNicknameTextUIConfig() : uiConfig.getOtherNicknameTextUIConfig());
            textUIConfig.bind(tvNickname.getContext(), nickname, 0, nickname.length());
        }

        tvNickname.setText(nickname);
    }

    public static void drawNotificationProfile(@NonNull ImageView ivProfile, @Nullable BaseMessage message) {
        int iconTint = SendbirdUIKit.isDarkMode() ? R.color.onlight_01 : R.color.ondark_01;
        int backgroundTint = R.color.background_300;
        int inset = ivProfile.getContext().getResources().getDimensionPixelSize(R.dimen.sb_size_6);
        final Drawable profile = DrawableUtils.createOvalIconWithInset(ivProfile.getContext(), backgroundTint, R.drawable.icon_channels, iconTint, inset);
        ivProfile.setImageDrawable(profile);
    }

    public static void drawProfile(@NonNull ImageView ivProfile, @Nullable BaseMessage message) {
        if (message == null) {
            return;
        }
        Sender sender = message.getSender();

        String url = "";
        String plainUrl = "";
        if (sender != null && !TextUtils.isEmpty(sender.getProfileUrl())) {
            url = sender.getProfileUrl();
            plainUrl = sender.getPlainProfileImageUrl();
        }

        drawProfile(ivProfile, url, plainUrl);
    }

    public static void drawProfile(@NonNull ImageView ivProfile, @Nullable Message message) {
        if (message == null) {
            return;
        }
        final UserInfo sender = JIM.getInstance().getUserInfoManager().getUserInfo(message.getSenderUserId());

        String url = "";
        String plainUrl = "";
        if (sender != null && !TextUtils.isEmpty(sender.getPortrait())) {
            url = sender.getPortrait();
            plainUrl = sender.getPortrait();
        }

        drawProfile(ivProfile, url, plainUrl);
    }

    public static void drawProfile(@NonNull ImageView ivProfile, @Nullable String url, @Nullable String plainUrl) {
        int iconTint = SendbirdUIKit.isDarkMode() ? R.color.onlight_01 : R.color.ondark_01;
        int backgroundTint = R.color.background_300;
        Drawable errorDrawable = DrawableUtils.createOvalIcon(ivProfile.getContext(), backgroundTint, R.drawable.icon_user, iconTint);

        if (url == null || plainUrl == null) return;
        GlideCachedUrlLoader.load(Glide.with(ivProfile.getContext()), url, String.valueOf(plainUrl.hashCode())).diskCacheStrategy(DiskCacheStrategy.ALL).error(errorDrawable).apply(RequestOptions.circleCropTransform()).into(ivProfile);
    }

    public static void drawThumbnail(@NonNull RoundCornerView view, @NonNull FileMessage message) {
        drawThumbnail(
                view,
                message.getRequestId(),
                getUrl(message),
                message.getPlainUrl(),
                message.getType(),
                null,
                R.dimen.sb_size_48
        );
    }

    public static void drawThumbnail(@NonNull RoundCornerView view, @NonNull ImageMessage image, @NonNull Message message) {
        if (TextUtils.isEmpty(image.getThumbnailUrl())) {
            return;
        }
        drawThumbnail(
                view,
                message.getMessageId(),
                image.getThumbnailUrl(),
                image.getUrl(),
                StringSet.image,
                null,
                R.dimen.sb_size_48
        );
    }

    public static void drawQuotedMessageThumbnail(@NonNull RoundCornerView view, @NonNull FileMessage message, @Nullable RequestListener<Drawable> requestListener) {
        drawThumbnail(
                view,
                message.getRequestId(),
                getUrl(message),
                message.getPlainUrl(),
                message.getType(),
                requestListener,
                R.dimen.sb_size_24
        );
    }

    private static String getUrl(@NonNull FileMessage message) {
        String url = message.getUrl();
        if (TextUtils.isEmpty(url) && message.getMessageCreateParams() != null && message.getMessageCreateParams().getFile() != null) {
            url = message.getMessageCreateParams().getFile().getAbsolutePath();
        }

        return url;
    }

    public static void drawThumbnail(
            @NonNull RoundCornerView view,
            @NonNull String requestId,
            @NonNull String url,
            @NonNull String plainUrl,
            @NonNull String fileType,
            @Nullable RequestListener<Drawable> requestListener,
            @DimenRes int iconSize
    ) {
        Context context = view.getContext();
        RequestOptions options = new RequestOptions().diskCacheStrategy(DiskCacheStrategy.ALL);
        RequestBuilder<Drawable> builder = Glide.with(context).asDrawable().apply(options);
//        int width;
//        int height;
////        FileInfo fileInfo = PendingMessageRepository.getInstance().getFileInfo(requestId);
////        if (fileInfo != null) {
////            width = fileInfo.getThumbnailWidth();
////            height = fileInfo.getThumbnailHeight();
////            builder = builder.override(width, height);
////            if (!TextUtils.isEmpty(fileInfo.getThumbnailPath())) {
////                url = fileInfo.getThumbnailPath();
////            }
////        } else {
////            BitmapFactory.Options opt = new BitmapFactory.Options();
////            opt.inJustDecodeBounds = true;
////            BitmapFactory.decodeFile(url, opt);
////            width = opt.outWidth;
////            height = opt.outHeight;
////            width = Math.max(MINIMUM_THUMBNAIL_WIDTH, width);
////            height = Math.max(MINIMUM_THUMBNAIL_HEIGHT, height);
////            builder = builder.override(width, height);
////        }
        if (fileType.toLowerCase().contains(StringSet.image) && !fileType.toLowerCase().contains(StringSet.gif)) {
            view.getContent().setScaleType(ImageView.ScaleType.CENTER);
            int thumbnailIconTint = SendbirdUIKit.isDarkMode() ? R.color.ondark_02 : R.color.onlight_02;
            builder = builder.placeholder(DrawableUtils.setTintList(ImageUtils.resize(context.getResources(), AppCompatResources.getDrawable(context, R.drawable.icon_photo), iconSize, iconSize), AppCompatResources.getColorStateList(context, thumbnailIconTint))).error(DrawableUtils.setTintList(ImageUtils.resize(context.getResources(), AppCompatResources.getDrawable(context, R.drawable.icon_thumbnail_none), iconSize, iconSize), AppCompatResources.getColorStateList(context, thumbnailIconTint)));
        }

        final String cacheKey = generateThumbnailCacheKey(requestId, plainUrl);
        GlideCachedUrlLoader.load(builder, url, cacheKey).centerCrop().listener(new RequestListener<Drawable>() {
            @Override
            public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                if (requestListener != null) {
                    requestListener.onLoadFailed(e, model, target, isFirstResource);
                }
                return false;
            }

            @Override
            public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                view.getContent().setScaleType(ImageView.ScaleType.CENTER_CROP);
                if (requestListener != null) {
                    requestListener.onResourceReady(resource, model, target, dataSource, isFirstResource);
                }
                return false;
            }
        }).into(view.getContent());
    }

    private static String generateThumbnailCacheKey(@NonNull String requestId, @NonNull String plainUrl) {
        if (TextUtils.isNotEmpty(requestId)) {
            return "thumbnail_" + requestId;
        }
        return String.valueOf(plainUrl.hashCode());
    }

    public static void drawThumbnailIcon(@NonNull ImageView imageView, @NonNull FileMessage message) {
        drawThumbnailIcon(imageView, message.getType());
    }

    public static void drawThumbnailIcon(@NonNull ImageView imageView, @NonNull String fileType) {
        Context context = imageView.getContext();
        int backgroundTint = R.color.ondark_01;
        int iconTint = R.color.onlight_02;
        if (fileType.toLowerCase().contains(StringSet.gif)) {
            imageView.setImageDrawable(DrawableUtils.createOvalIcon(context, backgroundTint, R.drawable.icon_gif, iconTint));
        } else if (fileType.toLowerCase().contains(StringSet.video)) {
            imageView.setImageDrawable(DrawableUtils.createOvalIcon(context, backgroundTint, R.drawable.icon_play, iconTint));
        } else {
            imageView.setImageResource(android.R.color.transparent);
        }
    }

    public static void drawFileIcon(@NonNull ImageView imageView, @NonNull FileMessage fileMessage) {
        drawFileIcon(imageView, fileMessage.getType());
    }

    public static void drawFileIcon(@NonNull ImageView imageView, @NonNull String fileType) {
        Context context = imageView.getContext();
        int backgroundTint = SendbirdUIKit.isDarkMode() ? R.color.background_600 : R.color.background_50;
        int iconTint = SendbirdUIKit.getDefaultThemeMode().getPrimaryTintResId();
        int inset = (int) context.getResources().getDimension(R.dimen.sb_size_4);
        Drawable background = DrawableUtils.setTintList(context, R.drawable.sb_rounded_rectangle_light_corner_10, backgroundTint);
        if (fileType.toLowerCase().startsWith(StringSet.audio)) {
            Drawable icon = DrawableUtils.setTintList(imageView.getContext(), R.drawable.icon_file_audio, iconTint);
            imageView.setImageDrawable(DrawableUtils.createLayerIcon(background, icon, inset));
        } else {
            Drawable icon = DrawableUtils.setTintList(imageView.getContext(), R.drawable.icon_file_document, iconTint);
            imageView.setImageDrawable(DrawableUtils.createLayerIcon(background, icon, inset));
        }
    }

    public static void drawFileMessageIconToReply(@NonNull ImageView imageView, @NonNull FileMessage fileMessage) {
        drawFileMessageIconToReply(imageView, fileMessage.getType());
    }

    public static void drawFileMessageIconToReply(@NonNull ImageView imageView, @NonNull String fileType) {
        Context context = imageView.getContext();
        int backgroundTint = SendbirdUIKit.isDarkMode() ? R.color.background_500 : R.color.background_100;
        int iconTint = SendbirdUIKit.isDarkMode() ? R.color.ondark_02 : R.color.onlight_02;
        int inset = (int) context.getResources().getDimension(R.dimen.sb_size_8);
        Drawable background = DrawableUtils.setTintList(context, R.drawable.sb_rounded_rectangle_light_corner_10, backgroundTint);

        if (fileType.toLowerCase().startsWith(StringSet.audio)) {
            Drawable icon = DrawableUtils.setTintList(imageView.getContext(), R.drawable.icon_file_audio, iconTint);
            imageView.setImageDrawable(DrawableUtils.createLayerIcon(background, icon, inset));
        } else if ((fileType.startsWith(StringSet.image) && !fileType.contains(StringSet.svg)) || fileType.toLowerCase().contains(StringSet.gif) || fileType.toLowerCase().contains(StringSet.video)) {
            imageView.setImageResource(android.R.color.transparent);
        } else {
            Drawable icon = DrawableUtils.setTintList(imageView.getContext(), R.drawable.icon_file_document, iconTint);
            imageView.setImageDrawable(DrawableUtils.createLayerIcon(background, icon, inset));
        }
    }


    public static void drawSentAt(@NonNull TextView tvSentAt, @Nullable BaseMessage message, @Nullable MessageUIConfig uiConfig) {
        if (message == null) {
            return;
        }

        final Spannable sentAt = new SpannableString(DateUtils.formatTime(tvSentAt.getContext(), message.getCreatedAt()));
        if (uiConfig != null) {
            final boolean isMine = MessageUtils.isMine(message);
            final TextUIConfig textUIConfig = isMine ? uiConfig.getMySentAtTextUIConfig() : uiConfig.getOtherSentAtTextUIConfig();
            textUIConfig.bind(tvSentAt.getContext(), sentAt, 0, sentAt.length());
        }
        tvSentAt.setText(sentAt);
    }

    public static void drawSentAt(@NonNull TextView tvSentAt, @Nullable Message message, @Nullable MessageUIConfig uiConfig) {
        if (message == null) {
            return;
        }

        final Spannable sentAt = new SpannableString(DateUtils.formatTime(tvSentAt.getContext(), message.getTimestamp()));
        if (uiConfig != null) {
            final boolean isMine = MessageUtils.isMine(message);
            final TextUIConfig textUIConfig = isMine ? uiConfig.getMySentAtTextUIConfig() : uiConfig.getOtherSentAtTextUIConfig();
            textUIConfig.bind(tvSentAt.getContext(), sentAt, 0, sentAt.length());
        }
        tvSentAt.setText(sentAt);
    }

    public static void drawParentMessageSentAt(@NonNull TextView tvSentAt, @Nullable BaseMessage message, @Nullable MessageUIConfig uiConfig) {
        if (message == null) {
            return;
        }

        final Context context = tvSentAt.getContext();
        final long createdAt = message.getCreatedAt();
        final String sentAtTime = DateUtils.formatTime(context, createdAt);
        final String sentAtDate = DateUtils.isThisYear(createdAt) ? DateUtils.formatDate2(createdAt) : DateUtils.formatDate4(createdAt);
        final Spannable sentAt = new SpannableString(sentAtDate + " " + sentAtTime);
        if (uiConfig != null) {
            final boolean isMine = MessageUtils.isMine(message);
            final TextUIConfig textUIConfig = isMine ? uiConfig.getMySentAtTextUIConfig() : uiConfig.getOtherSentAtTextUIConfig();
            textUIConfig.bind(context, sentAt, 0, sentAt.length());
        }
        tvSentAt.setText(sentAt);
    }

    public static void drawFilename(@NonNull TextView tvFilename, @Nullable FileMessage message, @Nullable MessageUIConfig uiConfig) {
        if (message == null) {
            return;
        }

        drawFilename(tvFilename, message.getName(), MessageUtils.isMine(message), uiConfig);
    }

    public static void drawFilename(@NonNull TextView tvFilename, @Nullable Message message, @Nullable com.juggle.im.model.messages.FileMessage fileMessage, @Nullable MessageUIConfig uiConfig) {
        if (message == null || fileMessage == null) {
            return;
        }
        drawFilename(tvFilename, fileMessage.getName(), MessageUtils.isMine(message), uiConfig);
    }

    public static void drawFilename(@NonNull TextView tvFilename, @NonNull String fileName, boolean isMine, @Nullable MessageUIConfig uiConfig) {
        final Spannable filename = new SpannableString(fileName);
        if (uiConfig != null) {
            final TextUIConfig textUIConfig = isMine ? uiConfig.getMyMessageTextUIConfig() : uiConfig.getOtherMessageTextUIConfig();
            textUIConfig.bind(tvFilename.getContext(), filename, 0, filename.length());
        }

        tvFilename.setText(filename);
    }

    public static void drawVoiceMessage(@NonNull VoiceMessageView voiceMessageView, @NonNull FileMessage message) {
        voiceMessageView.drawVoiceMessage(message);
    }

    public static void drawVoiceMessage(@NonNull VoiceMessageView voiceMessageView, @NonNull VoiceMessage voiceMessage, @NonNull Message message) {
        voiceMessageView.drawVoiceMessage(message, voiceMessage);
    }

    public static void drawTimeline(@NonNull TextView timelineView, int milliseconds) {
        long minutes = TimeUnit.MILLISECONDS.toMinutes(milliseconds);
        long sec = TimeUnit.MILLISECONDS.toSeconds(milliseconds) - TimeUnit.MINUTES.toSeconds(minutes);
        timelineView.setText(String.format(Locale.getDefault(), "%02d:%02d", minutes, sec));
    }

    public static void drawVoicePlayerProgress(@NonNull VoiceProgressView progressView, int milliseconds, int duration) {
        if (duration == 0) return;
        int progress = milliseconds * 1000 / duration;
        int prevProgress = progressView.getProgress();

        if (prevProgress <= progress) {
            progressView.drawProgressWithAnimation(progress);
        } else {
            progressView.drawProgress(progress);
        }
    }
}
