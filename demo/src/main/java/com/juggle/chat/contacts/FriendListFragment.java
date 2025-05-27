package com.juggle.chat.contacts;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.juggle.chat.R;
import com.juggle.chat.bean.FriendBean;
import com.juggle.chat.bean.HttpResult;
import com.juggle.chat.bean.ListResult;
import com.juggle.chat.contacts.chatroom.ChatroomListActivity;
import com.juggle.chat.contacts.group.GroupListActivity;
import com.juggle.chat.databinding.FragmentFriendsGroupsBinding;
import com.juggle.chat.contacts.add.AddFriendListActivity;
import com.juggle.chat.http.CustomCallback;
import com.juggle.chat.http.ServiceManager;
import com.jet.im.kit.SendbirdUIKit;
import com.jet.im.kit.activities.ChannelActivity;
import com.juggle.im.JIM;
import com.juggle.im.interfaces.IConversationManager;
import com.juggle.im.model.Conversation;
import com.juggle.im.model.ConversationInfo;

import java.util.List;

/**
 * Fragment displaying the member list in the channel.
 */
public class FriendListFragment extends Fragment implements IConversationManager.IConversationListener {
    private final FriendAdapter adapter = new FriendAdapter(getContext());
    private static final String sKey = "FriendListFragment";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        com.juggle.chat.databinding.FragmentFriendsGroupsBinding binding = FragmentFriendsGroupsBinding.inflate(inflater, container, false);

        binding.headerView.getTitleTextView().setText(getString(R.string.text_tab_friends));
        binding.headerView.getLeftButton().setVisibility(View.GONE);
        binding.headerView.setRightButtonImageResource(com.jet.im.kit.R.drawable.icon_create);
        binding.headerView.setRightButtonTint(SendbirdUIKit.getDefaultThemeMode().getPrimaryTintColorStateList(getContext()));
        binding.headerView.setOnRightButtonClickListener(v -> {
            startActivity(AddFriendListActivity.newIntent(getContext()));
        });

        adapter.setOnItemClickListener((view, position, data) -> {
            if (position == 0) {
                startActivity(FriendApplicationListActivity.newIntent(requireContext()));
            } else if (position == 1) {
                startActivity(GroupListActivity.newIntent(requireContext()));
            } else if (position == 2) {
                startActivity(ChatroomListActivity.newIntent(requireContext()));
            } else {
                startActivity(ChannelActivity.newIntent(requireContext(), Conversation.ConversationType.PRIVATE.getValue(), data.getUser_id()));
            }
        });

        binding.rvList.setAdapter(adapter);
        binding.rvList.setLayoutManager(new LinearLayoutManager(getContext()));

        JIM.getInstance().getConversationManager().addListener(sKey, this);

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    protected void refresh() {
        ServiceManager.getFriendsService().getFriendList(SendbirdUIKit.userId, "0", 200).enqueue(new CustomCallback<HttpResult<ListResult<FriendBean>>, ListResult<FriendBean>>() {
            @Override
            public void onSuccess(ListResult<FriendBean> listResult) {
                if (listResult.getItems() != null && !listResult.getItems().isEmpty()) {
                    adapter.setList(listResult.getItems());
                }
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        refresh();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        JIM.getInstance().getConversationManager().removeListener(sKey);
    }

    @Override
    public void onConversationInfoAdd(List<ConversationInfo> conversationInfoList) {
        checkUnreadCount(conversationInfoList);
    }

    private void checkUnreadCount(List<ConversationInfo> conversationInfoList) {
        for (ConversationInfo c : conversationInfoList) {
            if (c.getConversation().getConversationType() == Conversation.ConversationType.SYSTEM
            && c.getConversation().getConversationId().equals(SendbirdUIKit.FRIEND_CONVERSATION_ID)) {
                adapter.notifyItemChanged(0);
            }
        }
    }

    @Override
    public void onConversationInfoUpdate(List<ConversationInfo> conversationInfoList) {
        checkUnreadCount(conversationInfoList);
    }

    @Override
    public void onConversationInfoDelete(List<ConversationInfo> conversationInfoList) {

    }

    @Override
    public void onTotalUnreadMessageCountUpdate(int count) {

    }
}
