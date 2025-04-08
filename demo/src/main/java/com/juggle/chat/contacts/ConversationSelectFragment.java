package com.juggle.chat.contacts;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.bumptech.glide.request.RequestOptions;
import com.jet.im.kit.SendbirdUIKit;
import com.jet.im.kit.utils.DrawableUtils;
import com.jet.im.kit.utils.TextUtils;
import com.juggle.chat.R;
import com.juggle.chat.common.adapter.CommonAdapter;
import com.juggle.chat.common.adapter.MultiItemTypeAdapter;
import com.juggle.chat.common.adapter.ViewHolder;
import com.juggle.chat.databinding.FragmentConversationSelectBinding;
import com.juggle.im.JIM;
import com.juggle.im.JIMConst;
import com.juggle.im.model.Conversation;
import com.juggle.im.model.ConversationInfo;
import com.juggle.im.model.GroupInfo;
import com.juggle.im.model.UserInfo;

import java.util.List;

public class ConversationSelectFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        com.juggle.chat.databinding.FragmentConversationSelectBinding binding = FragmentConversationSelectBinding.inflate(inflater, container, false);

        binding.headerView.getTitleTextView().setText(getString(R.string.text_select_conversation));
        binding.headerView.setLeftButtonImageResource(R.drawable.icon_back);
        binding.headerView.setLeftButtonTint(SendbirdUIKit.getDefaultThemeMode().getPrimaryTintColorStateList(requireContext()));
        binding.headerView.setOnLeftButtonClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getActivity() != null) {
                    getActivity().finish();
                }
            }
        });
        binding.headerView.setUseRightButton(false);

        CommonAdapter<ConversationInfo> adapter = new CommonAdapter<ConversationInfo>(R.layout.sb_view_member_list_item) {
            @Override
            public void bindData(ViewHolder holder, ConversationInfo conversationInfo, int position) {
                String portrait = "";
                String name = "";
                if (conversationInfo.getConversation().getConversationType() == Conversation.ConversationType.GROUP) {
                    GroupInfo groupInfo = JIM.getInstance().getUserInfoManager().getGroupInfo(conversationInfo.getConversation().getConversationId());
                    portrait = groupInfo.getPortrait();
                    name = groupInfo.getGroupName();
                } else if (conversationInfo.getConversation().getConversationType() == Conversation.ConversationType.PRIVATE) {
                    UserInfo userInfo = JIM.getInstance().getUserInfoManager().getUserInfo(conversationInfo.getConversation().getConversationId());
                    portrait = userInfo.getPortrait();
                    name = userInfo.getUserName();
                }
                if (TextUtils.isNotEmpty(portrait)) {
                    Glide.with(holder.itemView.getContext())
                            .load(portrait)
                            .apply(RequestOptions.bitmapTransform(new CircleCrop()))
                            .into(holder.<ImageView>getView(R.id.ivProfile));
                } else {
                    holder.<ImageView>getView(R.id.ivProfile).setImageDrawable(DrawableUtils.getDefaultDrawable(holder.itemView.getContext()));
                }
                holder.setText(R.id.tvNickname, name);
            }
        };
        adapter.setOnItemClickListener(new MultiItemTypeAdapter.OnItemClickListener<ConversationInfo>() {
            @Override
            public void onItemClick(View view, RecyclerView.ViewHolder holder, ConversationInfo conversationInfo, int position) {
                if (getActivity() == null) {
                    return;
                }
                Intent intent = new Intent();
                intent.putExtra("type", conversationInfo.getConversation().getConversationType().getValue());
                intent.putExtra("id", conversationInfo.getConversation().getConversationId());
                getActivity().setResult(Activity.RESULT_OK, intent);
                getActivity().finish();
            }

            @Override
            public boolean onItemLongClick(View view, RecyclerView.ViewHolder holder, ConversationInfo conversationInfo, int position) {
                return false;
            }
        });
        int[] types = {Conversation.ConversationType.PRIVATE.getValue(), Conversation.ConversationType.GROUP.getValue()};
        List<ConversationInfo> conversationInfoList = JIM.getInstance().getConversationManager().getConversationInfoList(types, 100, 0, JIMConst.PullDirection.OLDER);
        adapter.setData(conversationInfoList);

        binding.rvList.setAdapter(adapter);
        binding.rvList.setLayoutManager(new LinearLayoutManager(getContext()));
        return binding.getRoot();
    }
}
