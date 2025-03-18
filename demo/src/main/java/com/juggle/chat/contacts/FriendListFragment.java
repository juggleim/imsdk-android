package com.juggle.chat.contacts;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.jet.im.kit.interfaces.OnItemClickListener;
import com.jet.im.kit.modules.components.StateHeaderComponent;
import com.juggle.chat.R;
import com.juggle.chat.bean.FriendBean;
import com.juggle.chat.bean.HttpResult;
import com.juggle.chat.bean.ListResult;
import com.juggle.chat.databinding.FragmentFriendsGroupsBinding;
import com.juggle.chat.contacts.add.AddFriendListActivity;
import com.juggle.chat.http.CustomCallback;
import com.juggle.chat.http.ServiceManager;
import com.jet.im.kit.SendbirdUIKit;
import com.jet.im.kit.activities.ChannelActivity;
import com.juggle.im.model.Conversation;

/**
 * Fragment displaying the member list in the channel.
 */
public class FriendListFragment extends Fragment {
    private FragmentFriendsGroupsBinding binding;
    private final StateHeaderComponent headerComponent = new StateHeaderComponent();
    private final FriendAdapter adapter = new FriendAdapter();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentFriendsGroupsBinding.inflate(inflater, container, false);
        headerComponent.getParams().setTitle(getString(R.string.text_tab_friends));
        headerComponent.getParams().setUseLeftButton(false);
        headerComponent.getParams().setRightButtonText("Add");
        headerComponent.setOnRightButtonClickListener(v -> startActivity(AddFriendListActivity.newIntent(getContext())));
        View header = headerComponent.onCreateView(requireContext(), inflater, binding.headerComponent, savedInstanceState);
        binding.headerComponent.addView(header);
        adapter.setOnItemClickListener(new OnItemClickListener<FriendBean>() {
            @Override
            public void onItemClick(@NonNull View view, int position, @NonNull FriendBean data) {
                if (position == 0) {

                } else if (position == 1) {
                    startActivity(GroupListActivity.newIntent(requireContext()));
                } else if (position == 2) {
                    startActivity(ChatroomListActivity.newIntent(requireContext()));
                } else {
                    startActivity(ChannelActivity.newIntent(requireContext(), Conversation.ConversationType.PRIVATE.getValue(), data.getUser_id()));
                }
            }
        });

        binding.rvList.setAdapter(adapter);
        binding.rvList.setLayoutManager(new LinearLayoutManager(getContext()));
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    protected void refresh() {
        ServiceManager.friendsService().getFriendList(SendbirdUIKit.userId, "0", 200).enqueue(new CustomCallback<HttpResult<ListResult<FriendBean>>, ListResult<FriendBean>>() {
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
}
