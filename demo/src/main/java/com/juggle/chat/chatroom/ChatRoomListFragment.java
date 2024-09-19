package com.juggle.chat.chatroom;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.jet.im.kit.SendbirdUIKit;
import com.jet.im.kit.activities.ChannelActivity;
import com.juggle.chat.bean.ChatRoomBean;
import com.juggle.chat.bean.FriendBean;
import com.juggle.chat.bean.HttpResult;
import com.juggle.chat.bean.ListResult;
import com.juggle.chat.common.adapter.CommonAdapter;
import com.juggle.chat.common.adapter.MultiItemTypeAdapter;
import com.juggle.chat.common.widgets.TitleBar;
import com.juggle.chat.databinding.FragmentChatroomBinding;
import com.juggle.chat.friends.add.AddFriendListActivity;
import com.juggle.chat.http.CustomCallback;
import com.juggle.chat.http.ServiceManager;
import com.juggle.im.model.Conversation;

import java.util.ArrayList;

/**
 * Fragment displaying the member list in the channel.
 */
public class ChatRoomListFragment extends Fragment {
    private FragmentChatroomBinding binding;
    private CommonAdapter<ChatRoomBean> adapter = new ChatRoomAdapter();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentChatroomBinding.inflate(inflater, container, false);
        adapter.setOnItemClickListener(new MultiItemTypeAdapter.OnItemClickListener<ChatRoomBean>() {
            @Override
            public void onItemClick(View view, RecyclerView.ViewHolder holder, ChatRoomBean bean, int position) {
                startActivity(ChannelActivity.newIntent(requireContext(), Conversation.ConversationType.CHATROOM.getValue(), bean.getRoomId()));
            }

            @Override
            public boolean onItemLongClick(View view, RecyclerView.ViewHolder holder, ChatRoomBean bean, int position) {
                return false;
            }
        });
        binding.rvList.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvList.setAdapter(adapter);
        ArrayList<ChatRoomBean> data = new ArrayList<>();
        data.add(new ChatRoomBean("chatroom1001", "植物大战僵尸首秀","https://downloads.juggleim.com/website/static/chatroom/1.jpeg"));
        data.add(new ChatRoomBean("chatroom1002", "敢不敢和我比比谁更甜","https://downloads.juggleim.com/website/static/chatroom/2.jpeg"));
        data.add(new ChatRoomBean("chatroom1003", "今天和我一起拿下这个黑猴","https://downloads.juggleim.com/website/static/chatroom/3.jpeg"));
        data.add(new ChatRoomBean("chatroom1004", "爱滑雪，2024 滑雪季备赛","https://downloads.juggleim.com/website/static/chatroom/4.jpeg"));
        data.add(new ChatRoomBean("chatroom1005", "战斗吧豌豆君，看我一决高下","https://downloads.juggleim.com/website/static/chatroom/5.png"));
        data.add(new ChatRoomBean("chatroom1006", "倔强青铜的逆袭之路","https://downloads.juggleim.com/website/static/chatroom/6.jpeg"));
        data.add(new ChatRoomBean("chatroom1007", "一个教唱歌的教授","https://downloads.juggleim.com/website/static/chatroom/7.jpeg"));
        data.add(new ChatRoomBean("chatroom1008", "咔咔咔卡个哇伊喽","https://downloads.juggleim.com/website/static/chatroom/8.jpeg"));
        adapter.setData(data);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

}
