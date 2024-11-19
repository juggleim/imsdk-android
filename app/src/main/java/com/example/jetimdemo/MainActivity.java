package com.example.jetimdemo;

import android.content.Context;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.jetimdemo.databinding.ActivityMainBinding;
import com.juggle.im.JIM;
import com.juggle.im.JIMConst;
import com.juggle.im.interfaces.IChatroomManager;
import com.juggle.im.interfaces.IConnectionManager;
import com.juggle.im.interfaces.IConversationManager;
import com.juggle.im.interfaces.IMessageManager;
import com.juggle.im.internal.uploader.FileUtil;
import com.juggle.im.model.Conversation;
import com.juggle.im.model.ConversationInfo;
import com.juggle.im.model.GroupMessageReadInfo;
import com.juggle.im.model.MediaMessageContent;
import com.juggle.im.model.Message;
import com.juggle.im.model.MessageContent;
import com.juggle.im.model.MessageQueryOptions;
import com.juggle.im.model.SearchConversationsResult;
import com.juggle.im.model.messages.FileMessage;
import com.juggle.im.model.messages.ImageMessage;
import com.juggle.im.model.messages.SnapshotPackedVideoMessage;
import com.juggle.im.model.messages.TextMessage;
import com.juggle.im.model.messages.ThumbnailPackedImageMessage;
import com.juggle.im.model.messages.VideoMessage;
import com.juggle.im.model.messages.VoiceMessage;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements IChatroomManager.IChatroomListener, IChatroomManager.IChatroomAttributesListener {

    private final String TOKEN1 = "CgZhcHBrZXkaIDAr072n8uOcw5YBeKCcQ+QCw4m6YWhgt99U787/dEJS";
    private final String TOKEN2 = "CgZhcHBrZXkaINodQgLnbhTbt0SzC8b/JFwjgUAdIfUZTEFK8DvDLgM1";
    private final String TOKEN3 = "CgZhcHBrZXkaINMDzs7BBTTZTwjKtM10zyxL4DBWFuZL6Z/OAU0Iajpv";
    private final String TOKEN4 = "CgZhcHBrZXkaIDHZwzfny4j4GiJye8y8ehU5fpJ+wVOGI3dCsBMfyLQv";
    private final String TOKEN5 = "CgZhcHBrZXkaIOx2upLCsmsefp8U/KNb52UGnAEu/xf+im3QaUd0HTC2";
    //nsw3sue72begyv7y,AVaoVF4zG
    private final String TOKEN6 = "ChBuc3czc3VlNzJiZWd5djd5GiAH3t-KKHZ0UOZNG6mfNL8m2hAUbN4RYH0iskZQTm6M7Q==";
    private final String TOKEN1181 = "ChBuc3czc3VlNzJiZWd5djd5GiB3vwQOFxILM02aHvzk0yXsSWIyWy-vkA4CLovMyoelAQ==";
    private final String TOKEN1182 = "ChBuc3czc3VlNzJiZWd5djd5GiDuv7mgMhk4e9roYlO9WeWer6_KZGn-hpJGuiMKsCI7Yw==";

    private AppBarConfiguration appBarConfiguration;
    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        HandlerThread sendThread = new HandlerThread("DEMO_TEST");
        sendThread.start();
        JIM.getInstance().setCallbackHandler(new Handler(sendThread.getLooper()));
        JIM.getInstance().getConnectionManager().addConnectionStatusListener("mainActivity", new IConnectionManager.IConnectionStatusListener() {
            @Override
            public void onStatusChange(JIMConst.ConnectionStatus status, int code, String extra) {
                Log.i("demo", "main activity onStatusChange status is " + status + " code is " + code);
                if (status == JIMConst.ConnectionStatus.CONNECTED) {
                    Handler mainHandler = new Handler(Looper.getMainLooper());
                    mainHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {

                            JIM.getInstance().getCallManager().startSingleCall("asdfsadf", null);




                        }
                    }, 500);


                }
            }

            @Override
            public void onDbOpen() {

            }

            @Override
            public void onDbClose() {

            }
        });
        JIM.getInstance().getConnectionManager().connect(TOKEN1181);
        JIM.getInstance().getMessageManager().addReadReceiptListener("main", new IMessageManager.IMessageReadReceiptListener() {
            @Override
            public void onMessagesRead(Conversation conversation, List<String> messageIds) {
                Log.d("demo", "onMessageRead, count is " + messageIds.size() + ", conversationType is " + conversation.getConversationType() + ", conversationId is " + conversation.getConversationId());
            }

            @Override
            public void onGroupMessagesRead(Conversation conversation, Map<String, GroupMessageReadInfo> messages) {
                Log.d("demo", "onGroupMessagesRead, conversationType is " + conversation.getConversationType() + ", id is " + conversation.getConversationId() + ", count is " + messages.size());
            }
        });
        JIM.getInstance().getMessageManager().addSyncListener("main", new IMessageManager.IMessageSyncListener() {
            @Override
            public void onMessageSyncComplete() {
                Log.d("demo", "onMessageSyncComplete");
            }
        });
        JIM.getInstance().getConversationManager().addSyncListener("main", new IConversationManager.IConversationSyncListener() {
            @Override
            public void onConversationSyncComplete() {
                Log.d("demo", "onConversationSyncComplete");
            }
        });
        JIM.getInstance().getMessageManager().addListener("main", new IMessageManager.IMessageListener() {
            @Override
            public void onMessageReceive(Message message) {
                Log.d("demo", "onMessageReceive type is " + message.getContentType() + " message is " + message);
                MessageContent c = message.getContent();
                if (c instanceof TextMessage) {
                    TextMessage t = (TextMessage) c;
                    Log.i("demo", "text message, extra is " + t.getExtra());
                } else if (c instanceof ImageMessage) {
                    ImageMessage i = (ImageMessage) c;
                    Log.i("demo", "image message, extra is " + i.getExtra());
                } else if (c instanceof FileMessage) {
                    FileMessage f = (FileMessage) c;
                    Log.i("demo", "file message, extra is " + f.getExtra());
                } else if (c instanceof VoiceMessage) {
                    VoiceMessage v = (VoiceMessage) c;
                    Log.i("demo", "voice message, extra is " + v.getExtra());
                }
                if(message.getContent() instanceof MediaMessageContent){
                    JIM.getInstance().getMessageManager().downloadMediaMessage(message.getMessageId(), new IMessageManager.IDownloadMediaMessageCallback() {
                        @Override
                        public void onProgress(int progress, Message message) {
                            Log.d("yuto","progress:"+progress);
                        }

                        @Override
                        public void onSuccess(Message message) {
                            Log.d("yuto","onSuccess:"+((MediaMessageContent)message.getContent()).getLocalPath());
                        }

                        @Override
                        public void onError(int errorCode) {
                            Log.d("yuto","onError:"+errorCode);
                        }

                        @Override
                        public void onCancel(Message message) {

                        }
                    });
                }
            }

            @Override
            public void onMessageRecall(Message message) {
                Log.d("demo", "onMessageRecall, messageId is " + message.getMessageId());
            }

            @Override
            public void onMessageDelete(Conversation conversation, List<Long> clientMsgNos) {
                Log.d("zzb", "onMessageDelete, conversation is " + conversation.getConversationId() + ", clientMsgNo is " + clientMsgNos);
            }

            @Override
            public void onMessageClear(Conversation conversation, long timestamp, String senderId) {
                Log.d("zzb", "onMessageClear, conversation is " + conversation.getConversationId() + ", timestamp is " + timestamp + ", senderId is " + senderId);
            }
        });
        JIM.getInstance().getConversationManager().addListener("main", new IConversationManager.IConversationListener() {
            @Override
            public void onConversationInfoAdd(List<ConversationInfo> conversationInfoList) {
                Log.i("demo", "onConversationInfoAdd, count is " + conversationInfoList.size());
            }

            @Override
            public void onConversationInfoUpdate(List<ConversationInfo> conversationInfoList) {
                Log.i("demo", "onConversationInfoUpdate, count is " + conversationInfoList.size());
            }

            @Override
            public void onConversationInfoDelete(List<ConversationInfo> conversationInfoList) {
                Log.i("demo", "onConversationInfoDelete, count is " + conversationInfoList.size());
            }

            @Override
            public void onTotalUnreadMessageCountUpdate(int count) {
                Log.i("demo", "onTotalUnreadMessageCountUpdate, count is " + count);
            }
        });
        JIM.getInstance().getChatroomManager().addListener("main", this);
        JIM.getInstance().getChatroomManager().addAttributesListener("main", this);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);

        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        appBarConfiguration = new AppBarConfiguration.Builder(navController.getGraph()).build();
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);

        binding.fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createConversation();
            }
        });
    }

    private void createConversation() {
        Conversation conversation = new Conversation(Conversation.ConversationType.GROUP, "test14");
        JIM.getInstance().getConversationManager().createConversationInfo(conversation, new IConversationManager.ICreateConversationInfoCallback() {
            @Override
            public void onSuccess(ConversationInfo conversationInfo) {
                Toast.makeText(getApplicationContext(), "createConversationInfo success", Toast.LENGTH_SHORT).show();

            }

            @Override
            public void onError(int errorCode) {
                Toast.makeText(getApplicationContext(), "createConversationInfo error", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void sendMessages() {
        Conversation c = new Conversation(Conversation.ConversationType.GROUP, "groupid1");
        TextMessage t = new TextMessage("111");
        t.setExtra("extra");
        ImageMessage i = new ImageMessage();
        i.setHeight(600);
        i.setWidth(800);
        i.setUrl("http://url.url");
        i.setThumbnailUrl("http://thumb.url");
        i.setExtra("extra");
        FileMessage f = new FileMessage();
        f.setName("fileName");
        f.setUrl("http:/url.url");
        f.setSize(1024);
        f.setType("text");
        f.setExtra("extra");
        VoiceMessage v = new VoiceMessage();
        v.setUrl("http:/url.url");
        v.setDuration(1024);
        v.setExtra("extra");
        VideoMessage video = new VideoMessage();
        video.setUrl("http://video.com");
        video.setSnapshotUrl("http://snapshot.com");
        video.setHeight(400);
        video.setWidth(600);
        video.setExtra("extra");

        IMessageManager.ISendMessageCallback callback = new IMessageManager.ISendMessageCallback() {
            @Override
            public void onSuccess(Message message) {
                Log.i("TAG", "send message success");
            }

            @Override
            public void onError(Message message, int errorCode) {
                Log.i("TAG", "send message error, code is " + errorCode);
                Message mm = JIM.getInstance().getMessageManager().resendMessage(message, new IMessageManager.ISendMessageCallback() {
                    @Override
                    public void onSuccess(Message message) {
                        Log.i("TAG", "send message success");
                    }

                    @Override
                    public void onError(Message message, int errorCode) {
                        Log.i("TAG", "send message error, code is " + errorCode);
                    }
                });
                Log.i("TAG", "resend ");
            }
        };
        Message m = JIM.getInstance().getMessageManager().sendMessage(t, c, callback);
        Log.i("TAG", "after send, clientMsgNo is " + m.getClientMsgNo());
//        Thread.sleep(500);
//        JetIM.getInstance().getMessageManager().sendMessage(i, c, callback);
//        Thread.sleep(500);
//        JetIM.getInstance().getMessageManager().sendMessage(f, c, callback);
//        Thread.sleep(500);
//        JetIM.getInstance().getMessageManager().sendMessage(v, c, callback);
//        Thread.sleep(500);
//        JetIM.getInstance().getMessageManager().sendMessage(video, c, callback);

    }

    private void sendMediaMessage() {
        String filePath = getFilesDir().getAbsolutePath() + File.separator + "xhup.png";
        String filePath2 = getFilesDir().getAbsolutePath() + File.separator + "VID_20240129092043475.mp4";
        Conversation c = new Conversation(Conversation.ConversationType.GROUP, "groupid1");
        ImageMessage image = new ImageMessage();
        image.setHeight(600);
        image.setWidth(800);
        image.setSize(116 * 1024);
        image.setLocalPath(filePath);
        image.setThumbnailLocalPath(filePath);
        ThumbnailPackedImageMessage tpImage = ThumbnailPackedImageMessage.messageWithImage(filePath);
        tpImage.setHeight(600);
        tpImage.setWidth(800);
        tpImage.setSize(116 * 1024);
        VideoMessage video = new VideoMessage();
        video.setHeight(400);
        video.setWidth(600);
        video.setLocalPath(filePath2);
        video.setSnapshotLocalPath(filePath);
        SnapshotPackedVideoMessage spVideo = SnapshotPackedVideoMessage.messageWithVideo(filePath2, getThumbnailVideoFile(getApplicationContext(), filePath2));
        spVideo.setHeight(400);
        spVideo.setWidth(600);
        spVideo.setName(FileUtil.getFileName(filePath2));
        FileMessage file = new FileMessage();
        file.setName("xhup.png");
        file.setLocalPath(filePath);
        file.setSize(116 * 1024);
        file.setType("png");
        VoiceMessage voice = new VoiceMessage();
        voice.setLocalPath(filePath);
        voice.setDuration(15);
        Message m = JIM.getInstance().getMessageManager().sendMediaMessage(image, c, new IMessageManager.ISendMediaMessageCallback() {
            @Override
            public void onProgress(int progress, Message message) {
                Log.i("sendMediaMessage", "onProgress, clientMsgNo is " + message.getClientMsgNo() + ", progress is " + progress);
            }

            @Override
            public void onSuccess(Message message) {
                Log.i("sendMediaMessage", "onSuccess, clientMsgNo is " + message.getClientMsgNo() + ", messageId is " + message.getMessageId());
            }

            @Override
            public void onError(Message message, int errorCode) {
                Log.i("sendMediaMessage", "onError, clientMsgNo is " + message.getClientMsgNo() + ", errorCode is " + errorCode);
            }

            @Override
            public void onCancel(Message message) {
                Log.i("sendMediaMessage", "onCancel, clientMsgNo is " + message.getClientMsgNo());
            }
        });
        Log.i("sendMediaMessage", "after send, clientMsgNo is " + m.getClientMsgNo());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        return NavigationUI.navigateUp(navController, appBarConfiguration)
                || super.onSupportNavigateUp();
    }

    public static Bitmap getThumbnailVideoFile(Context context, String videoPath) {
        if (context == null || TextUtils.isEmpty(videoPath)) return null;
        File video = new File(videoPath);
        if (!video.exists()) return null;

        MediaMetadataRetriever mmr = null;
        try {
            mmr = new MediaMetadataRetriever();
            mmr.setDataSource(video.getPath());

            return mmr.getFrameAtTime();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (mmr != null) {
                try {
                    mmr.release();
                } catch (IOException ignore) {
                }
            }
        }
        return null;
    }

    @Override
    public void onAttributesUpdate(String chatroomId, Map<String, String> attributes) {
        Log.i("demo", "onAttributesUpdate, chatroomId is " + chatroomId + ", attributes is " + attributes);
    }

    @Override
    public void onAttributesDelete(String chatroomId, Map<String, String> attributes) {
        Log.i("demo", "onAttributesDelete, chatroomId is " + chatroomId + ", attributes is " + attributes);
    }

    @Override
    public void onChatroomJoin(String chatroomId) {
        Log.i("demo", "onChatroomJoin, chatroomId is " + chatroomId);


        Handler mainHandler = new Handler(Looper.getMainLooper());
        mainHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                JIM.getInstance().getChatroomManager().getAllAttributes("chatroom1001", new IChatroomManager.IChatroomAttributesCallback() {
                    @Override
                    public void onComplete(int errorCode, Map<String, String> attributes) {
                        int i = 1;
                    }
                });
            }
        }, 500);
//        Map<String, String> attributes = new HashMap<>();
//        attributes.put("key1", "value1");
//        attributes.put("key2", "value2");
//        JIM.getInstance().getChatroomManager().setAttributes("chatroom1001", attributes, new IChatroomManager.IChatroomAttributesUpdateCallback() {
//            @Override
//            public void onComplete(int errorCode, Map<String, Integer> failedKeys) {
//                int i = 1;
//            }
//        });
        List<String> keys = new ArrayList<>();
        keys.add("Key2");
        JIM.getInstance().getChatroomManager().removeAttributes("chatroom1001", keys, new IChatroomManager.IChatroomAttributesUpdateCallback() {
            @Override
            public void onComplete(int errorCode, Map<String, Integer> failedKeys) {
                int i = 1;
            }
        });
    }

    @Override
    public void onChatroomQuit(String chatroomId) {
        Log.i("demo", "onChatroomQuit, chatroomId is " + chatroomId);
    }

    @Override
    public void onChatroomJoinFail(String chatroomId, int errorCode) {
        Log.i("demo", "onChatroomJoinFail, chatroomId is " + chatroomId + ", errorCode is " + errorCode);
    }

    @Override
    public void onChatroomQuitFail(String chatroomId, int errorCode) {
        Log.i("demo", "onChatroomQuitFail, chatroomId is " + chatroomId + ", errorCode is " + errorCode);
    }

    @Override
    public void onChatroomKick(String chatroomId) {
        Log.i("demo", "onChatroomKick, chatroomId is " + chatroomId);
    }

    @Override
    public void onChatroomDestroy(String chatroomId) {
        Log.i("demo", "onChatroomDestroy, chatroomId is " + chatroomId);
    }
}