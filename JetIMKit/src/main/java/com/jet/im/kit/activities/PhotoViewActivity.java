package com.jet.im.kit.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StyleRes;
import androidx.fragment.app.FragmentManager;

import com.juggle.im.JIM;
import com.juggle.im.model.Message;
import com.juggle.im.model.UserInfo;
import com.juggle.im.model.messages.ImageMessage;
import com.sendbird.android.channel.ChannelType;
import com.jet.im.kit.R;
import com.jet.im.kit.SendbirdUIKit;
import com.jet.im.kit.consts.StringSet;
import com.jet.im.kit.fragments.PhotoViewFragment;
import com.jet.im.kit.utils.MessageUtils;

/**
 * Activity displays a image file.
 */
public class PhotoViewActivity extends BaseActivity {
    @NonNull
    public static Intent newIntent(@NonNull Context context, @NonNull ImageMessage imageMessage, @NonNull Message message) {
        UserInfo sender = JIM.getInstance().getUserInfoManager().getUserInfo(message.getSenderUserId());
        if (sender == null) {
            sender = new UserInfo();
            sender.setUserId(message.getSenderUserId());
            sender.setUserName("");
            sender.setPortrait("");
        }
        return newIntent(context,
                message.getClientMsgNo(),
                message.getConversation().getConversationId(),
                imageMessage.getUrl(),
                imageMessage.getThumbnailUrl(),
                message.getMessageId(),
                imageMessage.getUrl(),
                "image/jpeg",
                message.getTimestamp(),
                message.getSenderUserId(),
                sender.getUserName(),
                true,
                SendbirdUIKit.getDefaultThemeMode().getResId()
                );
    }

    @NonNull
    private static Intent newIntent(
            @NonNull Context context,
            long messageId,
            @NonNull String channelUrl,
            @NonNull String imageUrl,
            @NonNull String plainUrl,
            @NonNull String requestId,
            @NonNull String fileName,
            @NonNull String fileType,
            long createdAt,
            @NonNull String senderId,
            @NonNull String senderName,
            boolean isDeletable,
            @StyleRes int themeResId
    ) {
        Intent intent = new Intent(context, PhotoViewActivity.class);
        intent.putExtra(StringSet.KEY_MESSAGE_ID, messageId);
        intent.putExtra(StringSet.KEY_MESSAGE_FILENAME, fileName);
        intent.putExtra(StringSet.KEY_CHANNEL_URL, channelUrl);
        intent.putExtra(StringSet.KEY_IMAGE_URL, imageUrl);
        intent.putExtra(StringSet.KEY_IMAGE_PLAIN_URL, plainUrl);
        intent.putExtra(StringSet.KEY_REQUEST_ID, requestId);
        intent.putExtra(StringSet.KEY_MESSAGE_MIMETYPE, fileType);
        intent.putExtra(StringSet.KEY_MESSAGE_CREATEDAT, createdAt);
        intent.putExtra(StringSet.KEY_SENDER_ID, senderId);
        intent.putExtra(StringSet.KEY_MESSAGE_SENDER_NAME, senderName);
//        intent.putExtra(StringSet.KEY_CHANNEL_TYPE, channelType);
        intent.putExtra(StringSet.KEY_DELETABLE_MESSAGE, isDeletable);
        intent.putExtra(StringSet.KEY_THEME_RES_ID, themeResId);
        return intent;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        int themeResId = getIntent().getIntExtra(StringSet.KEY_THEME_RES_ID, SendbirdUIKit.getDefaultThemeMode().getResId());
        setTheme(themeResId);
        setContentView(R.layout.sb_activity);

        final Intent intent = getIntent();
        final long messageId = intent.getLongExtra(StringSet.KEY_MESSAGE_ID, 0L);
        final String senderId = intent.getStringExtra(StringSet.KEY_SENDER_ID);
        final String channelUrl = intent.getStringExtra(StringSet.KEY_CHANNEL_URL);
        final String fileName = intent.getStringExtra(StringSet.KEY_MESSAGE_FILENAME);
        final String url = intent.getStringExtra(StringSet.KEY_IMAGE_URL);
        final String plainUrl = intent.getStringExtra(StringSet.KEY_IMAGE_PLAIN_URL);
        final String requestId = intent.getStringExtra(StringSet.KEY_REQUEST_ID);
        final String mimeType = intent.getStringExtra(StringSet.KEY_MESSAGE_MIMETYPE);
        final String senderNickname = intent.getStringExtra(StringSet.KEY_MESSAGE_SENDER_NAME);
        final long createdAt = intent.getLongExtra(StringSet.KEY_MESSAGE_CREATEDAT, 0L);
        final ChannelType channelType = (ChannelType) intent.getSerializableExtra(StringSet.KEY_CHANNEL_TYPE);
        final boolean isDeletable = intent.getBooleanExtra(StringSet.KEY_DELETABLE_MESSAGE, MessageUtils.isMine(senderId));

        final PhotoViewFragment fragment = new PhotoViewFragment.Builder(senderId, fileName,
                channelUrl, url, plainUrl, requestId, mimeType, senderNickname, createdAt,
                messageId, channelType, SendbirdUIKit.getDefaultThemeMode(), isDeletable)
                .build();

        final FragmentManager manager = getSupportFragmentManager();
        manager.popBackStack();
        manager.beginTransaction()
                .replace(R.id.sb_fragment_container, fragment)
                .commit();
    }
}
