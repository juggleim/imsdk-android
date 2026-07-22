package com.juggle.chat.contacts.chatroom;

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
import com.juggle.chat.R;
import com.juggle.chat.bean.ChatRoomBean;
import com.juggle.chat.common.adapter.CommonAdapter;
import com.juggle.chat.common.adapter.MultiItemTypeAdapter;
import com.juggle.chat.databinding.FragmentChatroomBinding;
import com.juggle.im.model.Conversation;

import java.util.ArrayList;

/**
 * Fragment displaying the member list in the channel.
 */
public class ChatRoomListFragment extends Fragment {
    private FragmentChatroomBinding binding;
    private final CommonAdapter<ChatRoomBean> adapter = new ChatRoomAdapter();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentChatroomBinding.inflate(inflater, container, false);

        binding.headerView.getTitleTextView().setText(getString(R.string.text_tab_chatroom));
        binding.headerView.setLeftButtonImageResource(R.drawable.icon_back);
        binding.headerView.setLeftButtonTint(SendbirdUIKit.getDefaultThemeMode().getPrimaryTintColorStateList(getContext()));
        binding.headerView.setOnLeftButtonClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getActivity() != null) {
                    getActivity().finish();
                }
            }
        });
        binding.headerView.getRightButton().setVisibility(View.GONE);

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
        data.add(new ChatRoomBean("chatroom1001", "Plants vs. Zombies debut","https://downloads.juggleim.com/website/static/chatroom/1.jpeg"));
        data.add(new ChatRoomBean("chatroom1002", "Want to compare who is sweeter?","https://downloads.juggleim.com/website/static/chatroom/2.jpeg"));
        data.add(new ChatRoomBean("chatroom1003", "Join me to take down this black monkey","https://downloads.juggleim.com/website/static/chatroom/3.jpeg"));
        data.add(new ChatRoomBean("chatroom1004", "Love skiing, training for the 2024 ski season","https://downloads.juggleim.com/website/static/chatroom/4.jpeg"));
        data.add(new ChatRoomBean("chatroom1005", "Fight on, Pea Shooter, let me beat you","https://downloads.juggleim.com/website/static/chatroom/5.png"));
        data.add(new ChatRoomBean("chatroom1006", "The comeback of a stubborn bronze player","https://downloads.juggleim.com/website/static/chatroom/6.jpeg"));
        data.add(new ChatRoomBean("chatroom1007", "A professor who teaches singing","https://downloads.juggleim.com/website/static/chatroom/7.jpeg"));
        data.add(new ChatRoomBean("chatroom1008", "Ka-ka-ka-ka-wa-yi-lu","https://downloads.juggleim.com/website/static/chatroom/8.jpeg"));
        adapter.setData(data);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

}
