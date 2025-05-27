package com.juggle.chat.contacts.add;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.bumptech.glide.request.RequestOptions;
import com.jet.im.kit.activities.ChannelActivity;
import com.jet.im.kit.utils.DrawableUtils;
import com.juggle.chat.R;
import com.juggle.chat.base.Action;
import com.juggle.chat.base.BasePageFragment;
import com.juggle.chat.bean.SearchUserBean;
import com.juggle.chat.common.adapter.CommonAdapter;
import com.juggle.chat.common.adapter.EmptyWrapper;
import com.juggle.chat.common.adapter.MultiItemTypeAdapter;
import com.juggle.chat.common.adapter.ViewHolder;
import com.juggle.chat.common.widgets.CommonDialog;
import com.juggle.chat.component.HeadComponent;
import com.juggle.chat.component.ListComponent;
import com.juggle.chat.component.SearchComponent;
import com.juggle.im.model.Conversation;

import java.util.List;

/**
 * 功能描述: 创建群组页面
 *
 */
public class AddFriendListFragment
        extends BasePageFragment<AddFriendListViewModel> {

    protected ListComponent listComponent;
    protected SearchComponent searchComponent;
    protected HeadComponent headComponent;
    String mQuery;
    protected CommonAdapter<SearchUserBean> adapter;
    EmptyWrapper emptyWrapper =
            new EmptyWrapper(adapter, R.layout.rc_item_find_user_empty) {
                @Override
                public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
                    if (isEmpty()) {
                        holder.itemView
                                .findViewById(R.id.tv_hint)
                                .setVisibility(
                                        TextUtils.isEmpty(mQuery) ? View.GONE : View.VISIBLE);
                        return;
                    }
                    mInnerAdapter.onBindViewHolder(holder, position);
                }
            };

    @NonNull
    @Override
    protected AddFriendListViewModel onCreateViewModel(Bundle bundle) {
        return new ViewModelProvider(this)
                .get(AddFriendListViewModel.class);
    }

    @Override
    protected View createView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.rc_page_contact_list, container, false);
        headComponent = view.findViewById(R.id.rc_head_component);
        searchComponent = view.findViewById(R.id.rc_search_component);
        listComponent = view.findViewById(R.id.rc_list_component);
        listComponent.setEnableLoadMore(false);
        listComponent.setEnableRefresh(false);

        adapter = new CommonAdapter<SearchUserBean>(R.layout.view_add_friend_list_item) {
            @Override
            public void bindData(ViewHolder viewHolder, SearchUserBean item, int position) {
                if (com.jet.im.kit.utils.TextUtils.isNotEmpty(item.getAvatar())) {
                    Glide.with(viewHolder.itemView.getContext())
                            .load(item.getAvatar())
                            .apply(RequestOptions.bitmapTransform(new CircleCrop()))
                            .into(viewHolder.<ImageView>getView(R.id.ivProfile));
                } else {
                    viewHolder.<ImageView>getView(R.id.ivProfile).setImageDrawable(DrawableUtils.getDefaultDrawable(viewHolder.itemView.getContext()));
                }
                viewHolder.setText(R.id.tvNickname, item.getNickname());
                if (item.isFriend()) {
                    viewHolder.setText(R.id.tv_add_btn, getString(R.string.text_added));
                    viewHolder.setTextColor(R.id.tv_add_btn, getResources().getColor(com.jet.im.kit.R.color.background_400));
                } else {
                    viewHolder.setText(R.id.tv_add_btn, getString(com.jet.im.kit.R.string.sb_text_button_add));
                    viewHolder.setTextColor(R.id.tv_add_btn, getResources().getColor(com.jet.im.kit.R.color.primary_300));
                }
            }
        };
        emptyWrapper = new EmptyWrapper(adapter, R.layout.rc_item_find_user_empty) {
            @Override
            public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
                if (isEmpty()) {
                    holder.itemView
                            .findViewById(R.id.tv_hint)
                            .setVisibility(
                                    TextUtils.isEmpty(mQuery) ? View.GONE : View.VISIBLE);
                    return;
                }
                mInnerAdapter.onBindViewHolder(holder, position);
            }
        };

        listComponent.setAdapter(emptyWrapper);
        return view;
    }


    @Override
    protected void onViewReady(@NonNull AddFriendListViewModel viewModel) {
        headComponent.setLeftClickListener(v -> getActivity().finish());
        adapter.<SearchUserBean>setOnItemClickListener(
                new MultiItemTypeAdapter.OnItemClickListener<SearchUserBean>() {
                    @Override
                    public void onItemClick(
                            View view,
                            RecyclerView.ViewHolder holder,
                            SearchUserBean userProfile,
                            int position) {
                        if (userProfile.isFriend()) {
                            startActivity(ChannelActivity.newIntent(requireContext(), Conversation.ConversationType.PRIVATE.getValue(), userProfile.getUserId()));
                        } else {
                            addFriend(userProfile.getUserId());
                        }
                    }

                    @Override
                    public boolean onItemLongClick(
                            View view,
                            RecyclerView.ViewHolder holder,
                            SearchUserBean userProfile,
                            int position) {
                        return false;
                    }
                });
        searchComponent.setSearchQueryListener(
                new SearchComponent.OnSearchQueryListener() {
                    @Override
                    public void onSearch(String query) {
                        mQuery = query;
                        getViewModel().findUser(query);
                    }

                    @Override
                    public void onClickSearch(String query) {

                    }
                });
        viewModel
                .getUserProfileLiveData()
                .observe(
                        getViewLifecycleOwner(),
                        new Observer<List<SearchUserBean>>() {
                            @Override
                            public void onChanged(List<SearchUserBean> userProfiles) {
                                adapter.setData(userProfiles);
                                emptyWrapper.notifyDataSetChanged();
                            }
                        });
    }

    private void addFriend(String friendId) {
        CommonDialog dialog =
                new CommonDialog.Builder()
                        .setContentMessage(getString(R.string.text_add_friend_confirm))
                        .setDialogButtonClickListener(
                                new CommonDialog.OnDialogButtonClickListener() {
                                    @Override
                                    public void onPositiveClick(View v, Bundle bundle) {
                                        getViewModel().addFriend(friendId, new Action<Object>() {
                                            @Override
                                            public void call(Object o) {
                                                Toast.makeText(getContext(), "add Friend Success", Toast.LENGTH_SHORT).show();
                                                if (getActivity() != null) {
                                                    getActivity().finish();
                                                }
                                            }
                                        });
                                    }

                                    @Override
                                    public void onNegativeClick(View v, Bundle bundle) {
                                    }
                                })
                        .build();
        dialog.show(getParentFragmentManager(), null);
    }
}
