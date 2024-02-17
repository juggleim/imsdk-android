package com.example.jetimdemo;

import android.os.Bundle;

import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;

import android.util.Log;
import android.view.View;

import androidx.core.view.WindowCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.jetimdemo.databinding.ActivityMainBinding;
import com.jet.im.JetIM;
import com.jet.im.JetIMConst;
import com.jet.im.interfaces.IConnectionManager;
import com.jet.im.interfaces.IConversationManager;
import com.jet.im.interfaces.IMessageManager;
import com.jet.im.model.Conversation;
import com.jet.im.model.ConversationInfo;
import com.jet.im.model.Message;
import com.jet.im.model.MessageContent;
import com.jet.im.model.messages.FileMessage;
import com.jet.im.model.messages.ImageMessage;
import com.jet.im.model.messages.TextMessage;
import com.jet.im.model.messages.VideoMessage;
import com.jet.im.model.messages.VoiceMessage;
import com.jet.im.utils.LoggerUtils;

import android.view.Menu;
import android.view.MenuItem;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {

    private final String TOKEN1 = "CgZhcHBrZXkaIDAr072n8uOcw5YBeKCcQ+QCw4m6YWhgt99U787/dEJS";
    private final String TOKEN2 = "CgZhcHBrZXkaINodQgLnbhTbt0SzC8b/JFwjgUAdIfUZTEFK8DvDLgM1";
    private final String TOKEN3 = "CgZhcHBrZXkaINMDzs7BBTTZTwjKtM10zyxL4DBWFuZL6Z/OAU0Iajpv";
    private final String TOKEN4 = "CgZhcHBrZXkaIDHZwzfny4j4GiJye8y8ehU5fpJ+wVOGI3dCsBMfyLQv";
    private final String TOKEN5 = "CgZhcHBrZXkaIOx2upLCsmsefp8U/KNb52UGnAEu/xf+im3QaUd0HTC2";

    private AppBarConfiguration appBarConfiguration;
    private ActivityMainBinding binding;

    private int mConnectCount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        JetIM.getInstance().getConnectionManager().addConnectionStatusListener("mainActivity", new IConnectionManager.IConnectionStatusListener() {
            @Override
            public void onStatusChange(JetIMConst.ConnectionStatus status, int code) {
                Log.i("lifei", "main activity onStatusChange status is " + status + " code is " + code);
                if (status == JetIMConst.ConnectionStatus.CONNECTED) {

                    //get remote messages
//                    Conversation c = new Conversation(Conversation.ConversationType.PRIVATE, "userid5");
//                    JetIM.getInstance().getMessageManager().getRemoteMessages(c, 100, System.currentTimeMillis(), JetIMConst.PullDirection.NEWER, new IMessageManager.IGetMessagesCallback() {
//                        @Override
//                        public void onSuccess(List<Message> messages) {
//                            Log.i("lifei", "getRemoteMessage count is  " + messages.size());
//                        }
//
//                        @Override
//                        public void onError(int errorCode) {
//                            Log.i("lifei", "getRemoteMessage error code is  " + errorCode);
//                        }
//                    });

                    //disconnect
//                    if (mConnectCount == 0) {
//                        mConnectCount ++;
//                        JetIM.getInstance().getConnectionManager().disconnect(false);
//                        Timer timer = new Timer();
//                        timer.schedule(new TimerTask() {
//                            @Override
//                            public void run() {
//                                JetIM.getInstance().getConnectionManager().connect(TOKEN3);
//                            }
//                        }, 10000);
//                    } else if (mConnectCount == 1) {
//                        mConnectCount ++;
//                        JetIM.getInstance().getConnectionManager().disconnect(false);
//                        Timer timer = new Timer();
//                        timer.schedule(new TimerTask() {
//                            @Override
//                            public void run() {
//                                JetIM.getInstance().getConnectionManager().connect(TOKEN1);
//                            }
//                        }, 10000);
//                    } else if (mConnectCount == 2) {
//                        mConnectCount ++;
//                        List convs = JetIM.getInstance().getConversationManager().getConversationInfoList();
//                        Conversation c = new Conversation(Conversation.ConversationType.PRIVATE, "userid3");
//                        Message m = JetIM.getInstance().getMessageManager().sendMessage(new TextMessage("text"), c, new IMessageManager.ISendMessageCallback() {
//                            @Override
//                            public void onSuccess(Message message) {
//                                Log.i("lifei", "send success clientMsgNo is " + message.getClientMsgNo());
//                            }
//
//                            @Override
//                            public void onError(Message message, int errorCode) {
//
//                            }
//                        });
//                        Log.i("lifei", "send m clientMsgNo is " + m.getClientMsgNo());
//                    }

                    //send message
//                    try {
//                        sendMessages();
//                    } catch (InterruptedException e) {
//                        throw new RuntimeException(e);
//                    }

                    //recall message
//                    JetIM.getInstance().getMessageManager().recallMessage("npqml4eq2ane43gq", new IMessageManager.IRecallMessageCallback() {
//                        @Override
//                        public void onSuccess(Message message) {
//                            Log.i("lifei", "recall success");
//                        }
//
//                        @Override
//                        public void onError(int errorCode) {
//                            Log.i("lifei", "recall error code is " + errorCode);
//                        }
//                    });

//                    List<ConversationInfo> l = JetIM.getInstance().getConversationManager().getConversationInfoList();
//                    Log.e("lifei", "conversationList count is " + l.size());
//
//                    int[] conversationTypes = {Conversation.ConversationType.PRIVATE.getValue(), Conversation.ConversationType.GROUP.getValue()};
//                    l = JetIM.getInstance().getConversationManager().getConversationInfoList( 100, 1706445483689L, JetIMConst.PullDirection.NEWER);
//                    Log.e("lifei", "conversationList count is " + l.size());
//
//                    //get conversation
//                    Conversation conversation = new Conversation(Conversation.ConversationType.PRIVATE, "userid3");
//                    ConversationInfo info = JetIM.getInstance().getConversationManager().getConversationInfo(conversation);
//
//                    //set draft
//                    JetIM.getInstance().getConversationManager().setDraft(conversation, "draft");
//                    info = JetIM.getInstance().getConversationManager().getConversationInfo(conversation);
//
//                    //clear draft
//                    JetIM.getInstance().getConversationManager().clearDraft(conversation);
//                    info = JetIM.getInstance().getConversationManager().getConversationInfo(conversation);
//                    Log.e("lifei", "conversationList count is " + l.size());



                    //clear messages
//                    List<Message> messages = JetIM.getInstance().getMessageManager().getMessages(conversation, 100, 0, JetIMConst.PullDirection.OLDER);
//                    Log.e("lifei", "message count is " + messages.size());
//
//                    JetIM.getInstance().getMessageManager().clearMessages(conversation);
//                    messages = JetIM.getInstance().getMessageManager().getMessages(conversation, 100, 0, JetIMConst.PullDirection.OLDER);
//                    Log.e("lifei", "message count is " + messages.size());


                    //delete conversation
//                    JetIM.getInstance().getConversationManager().deleteConversationInfo(conversation);
//
//                    l = JetIM.getInstance().getConversationManager().getConversationInfoList();
//                    Log.e("lifei", "conversationList count is " + l.size());
//
//                    JetIM.getInstance().getConversationManager().getConversationInfo(conversation);

                    //delete Message
//                    JetIM.getInstance().getMessageManager().deleteMessageByClientMsgNo(57L);
//                    JetIM.getInstance().getMessageManager().deleteMessageByMessageId("npgdwvn5gf8grenb");

                    //get messages
//                    List<Message> messageList = JetIM.getInstance().getMessageManager().getMessages(conversation, 100, 1705922710597L, JetIMConst.PullDirection.OLDER);
//                    Log.e("lifei", "messageList count is " + messageList.size());
//
//                    List<String> contentTypes = new ArrayList<>();
//                    contentTypes.add("jg:file");
//                    contentTypes.add("jg:video");
//                    contentTypes.add("jg:img");
//                    List<Message> messageList4 = JetIM.getInstance().getMessageManager().getMessages(conversation, 100, 1705922710597L, JetIMConst.PullDirection.OLDER, contentTypes);
//                    Log.e("lifei", "messageList4 count is " + messageList.size());


//                    List<String> messageIds = new ArrayList<>();
//                    messageIds.add("npgdrs4e2eygrenb");
//                    messageIds.add("npgdwvg92f6grenb");
//                    messageIds.add("npgdwve92f4grenb");
//
//
//                    List<Message> messageList1 = JetIM.getInstance().getMessageManager().getMessagesByMessageIds(messageIds);
//                    Log.e("lifei", "messageList count is " + messageList1.size());
//
//                    long[] clientMsgNos = {54, 55, 40};
//                    List<Message> messageList2 = JetIM.getInstance().getMessageManager().getMessagesByClientMsgNos(clientMsgNos);
//                    Log.e("lifei", "messageList count is " + messageList2.size());

                }
            }

            @Override
            public void onDbOpen() {

            }
        });
        JetIM.getInstance().getConnectionManager().connect(TOKEN1);
        JetIM.getInstance().getMessageManager().addSyncListener("main", new IMessageManager.IMessageSyncListener() {
            @Override
            public void onMessageSyncComplete() {
                Log.d("lifei", "onMessageSyncComplete");
            }
        });
        JetIM.getInstance().getConversationManager().addSyncListener("main", new IConversationManager.IConversationSyncListener() {
            @Override
            public void onConversationSyncComplete() {
                Log.d("lifei", "onConversationSyncComplete");
            }
        });
        JetIM.getInstance().getMessageManager().addListener("main", new IMessageManager.IMessageListener() {
            @Override
            public void onMessageReceive(Message message) {
                Log.d("lifei", "onMessageReceive type is " + message.getContentType() + " message is " + message);
                MessageContent c = message.getContent();
                if (c instanceof TextMessage) {
                    TextMessage t = (TextMessage)c;
                    Log.i("lifei", "text message, extra is " + t.getExtra());
                } else if (c instanceof ImageMessage) {
                    ImageMessage i = (ImageMessage) c;
                    Log.i("lifei", "image message, extra is " + i.getExtra());
                } else if (c instanceof FileMessage) {
                    FileMessage f = (FileMessage) c;
                    Log.i("lifei", "file message, extra is " + f.getExtra());
                } else if (c instanceof VoiceMessage) {
                    VoiceMessage v = (VoiceMessage) c;
                    Log.i("lifei", "voice message, extra is " + v.getExtra());
                }
            }

            @Override
            public void onMessageRecall(Message message) {
                Log.d("lifei", "onMessageRecall, messageId is " + message.getMessageId());
            }
        });

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);

        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        appBarConfiguration = new AppBarConfiguration.Builder(navController.getGraph()).build();
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);

        binding.fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAnchorView(R.id.fab)
                        .setAction("Action", null).show();
            }
        });
    }

    private void sendMessages() throws InterruptedException {
        Conversation c = new Conversation(Conversation.ConversationType.PRIVATE, "userid4");
        TextMessage t = new TextMessage("sdfasdf");
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
                Message mm = JetIM.getInstance().getMessageManager().resendMessage(message, new IMessageManager.ISendMessageCallback() {
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
        Message m = JetIM.getInstance().getMessageManager().sendMessage(t, c, callback);
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
}