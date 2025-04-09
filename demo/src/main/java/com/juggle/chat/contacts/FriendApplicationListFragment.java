package com.juggle.chat.contacts;

import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
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
import com.jet.im.kit.utils.PortraitGenerator;
import com.juggle.chat.R;
import com.juggle.chat.bean.FriendApplicationBean;
import com.juggle.chat.bean.HttpResult;
import com.juggle.chat.bean.ListResult;
import com.juggle.chat.common.adapter.CommonAdapter;
import com.juggle.chat.common.adapter.MultiItemTypeAdapter;
import com.juggle.chat.common.adapter.ViewHolder;
import com.juggle.chat.databinding.FragmentFriendApplicationListBinding;
import com.juggle.chat.http.CustomCallback;
import com.juggle.chat.http.ServiceManager;
import com.juggle.im.JIM;
import com.juggle.im.model.Conversation;

import java.util.HashMap;

import okhttp3.RequestBody;

public class FriendApplicationListFragment extends Fragment {
    private CommonAdapter<FriendApplicationBean> mAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        FragmentFriendApplicationListBinding binding = FragmentFriendApplicationListBinding.inflate(inflater, container, false);

        binding.headerView.getTitleTextView().setText(getString(R.string.text_new_friend));
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

        mAdapter = new CommonAdapter<FriendApplicationBean>(R.layout.view_add_friend_list_item) {
            @Override
            public void bindData(ViewHolder holder, FriendApplicationBean friendApplicationBean, int position) {
                if (!TextUtils.isEmpty(friendApplicationBean.getUserInfo().getAvatar())) {
                    Glide.with(holder.itemView.getContext())
                            .load(friendApplicationBean.getUserInfo().getAvatar())
                            .apply(RequestOptions.bitmapTransform(new CircleCrop()))
                            .into(holder.<ImageView>getView(R.id.ivProfile));
                } else {
                    String path = PortraitGenerator.generateDefaultAvatar(holder.itemView.getContext(), friendApplicationBean.getUserInfo().getUser_id(), friendApplicationBean.getUserInfo().getNickname());
                    Uri uri = Uri.parse(path);
                    Glide.with(holder.itemView.getContext())
                            .load(uri)
                            .apply(RequestOptions.bitmapTransform(new CircleCrop()))
                            .into(holder.<ImageView>getView(R.id.ivProfile));
                }
                holder.setText(R.id.tvNickname, friendApplicationBean.getUserInfo().getNickname());
                if (friendApplicationBean.isSponsor()) {
                    if (friendApplicationBean.getStatus() == 0) {
                        holder.setText(R.id.tv_add_btn, "申请中");
                        holder.setTextColor(R.id.tv_add_btn, getResources().getColor(com.jet.im.kit.R.color.background_400));
                    } else if (friendApplicationBean.getStatus() == 1) {
                        holder.setText(R.id.tv_add_btn, "对方已接受");
                        holder.setTextColor(R.id.tv_add_btn, getResources().getColor(com.jet.im.kit.R.color.background_400));
                    } else if (friendApplicationBean.getStatus() == 3) {
                        holder.setText(R.id.tv_add_btn, "无应答");
                        holder.setTextColor(R.id.tv_add_btn, getResources().getColor(com.jet.im.kit.R.color.background_400));
                    }
                } else {
                    if (friendApplicationBean.getStatus() == 0) {
                        holder.setText(R.id.tv_add_btn, "同意");
                        holder.setTextColor(R.id.tv_add_btn, getResources().getColor(com.jet.im.kit.R.color.primary_300));
                    } else if (friendApplicationBean.getStatus() == 1) {
                        holder.setText(R.id.tv_add_btn, "已接受");
                        holder.setTextColor(R.id.tv_add_btn, getResources().getColor(com.jet.im.kit.R.color.background_400));
                    } else if (friendApplicationBean.getStatus() == 3) {
                        holder.setText(R.id.tv_add_btn, "已超时");
                        holder.setTextColor(R.id.tv_add_btn, getResources().getColor(com.jet.im.kit.R.color.background_400));
                    }
                }
            }
        };

        mAdapter.setOnItemClickListener(new MultiItemTypeAdapter.OnItemClickListener<FriendApplicationBean>() {
            @Override
            public void onItemClick(View view, RecyclerView.ViewHolder holder, FriendApplicationBean friendApplicationBean, int position) {
                if (!friendApplicationBean.isSponsor() && friendApplicationBean.getStatus() == 0) {
                    HashMap<String, Object> paramsMap = new HashMap<>();
                    paramsMap.put("sponsor_id", friendApplicationBean.getUserInfo().getUser_id());
                    paramsMap.put("is_agree", true);
                    RequestBody body = ServiceManager.createJsonRequest(paramsMap);
                    ServiceManager.getFriendsService().confirmFriend(body).enqueue(new CustomCallback<HttpResult<Object>, Object>() {
                        @Override
                        public void onSuccess(Object o) {
                            if (holder instanceof ViewHolder) {
                                ViewHolder h = (ViewHolder) holder;
                                h.setText(R.id.tv_add_btn, "已接受");
                                h.setTextColor(R.id.tv_add_btn, getResources().getColor(com.jet.im.kit.R.color.background_400));
                            }
                        }
                    });
                }
            }

            @Override
            public boolean onItemLongClick(View view, RecyclerView.ViewHolder holder, FriendApplicationBean friendApplicationBean, int position) {
                return false;
            }
        });

        binding.rvList.setAdapter(mAdapter);
        binding.rvList.setLayoutManager(new LinearLayoutManager(getContext()));
        return binding.getRoot();
    }

    @Override
    public void onResume() {
        super.onResume();
        refresh();
    }

    private void refresh() {
        Conversation c = new Conversation(Conversation.ConversationType.SYSTEM, SendbirdUIKit.FRIEND_CONVERSATION_ID);
        JIM.getInstance().getConversationManager().clearUnreadCount(c, null);
        ServiceManager.getFriendsService().getFriendApplicationList(0, 100).enqueue(new CustomCallback<HttpResult<ListResult<FriendApplicationBean>>, ListResult<FriendApplicationBean>>() {
            @Override
            public void onSuccess(ListResult<FriendApplicationBean> listResult) {
                if (listResult.getItems() != null && !listResult.getItems().isEmpty()) {
                    mAdapter.setData(listResult.getItems());
                }
            }
        });
    }
}
