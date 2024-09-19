package com.juggle.chat.friends.add;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.juggle.chat.R;
import com.juggle.chat.base.Action;
import com.juggle.chat.base.BasePageFragment;
import com.juggle.chat.bean.FriendBean;
import com.juggle.chat.common.adapter.CommonAdapter;
import com.juggle.chat.common.adapter.EmptyWrapper;
import com.juggle.chat.common.adapter.MultiItemTypeAdapter;
import com.juggle.chat.common.widgets.CommonDialog;
import com.juggle.chat.component.HeadComponent;
import com.juggle.chat.component.ListComponent;
import com.juggle.chat.component.SearchComponent;
import com.juggle.chat.friends.FriendAdapter;

import java.util.List;

/**
 * 功能描述: 创建群组页面
 *
 * @author rongcloud
 * @since 5.10.4
 */
public class AddFriendListFragment
        extends BasePageFragment<AddFriendListViewModel> {

    protected ListComponent listComponent;
    protected SearchComponent searchComponent;
    protected HeadComponent headComponent;
    String mQuery;
    protected CommonAdapter<FriendBean> adapter = new FriendAdapter();
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
        listComponent.setAdapter(emptyWrapper);
        return view;
    }


    @Override
    protected void onViewReady(@NonNull AddFriendListViewModel viewModel) {
        headComponent.setLeftClickListener(v -> getActivity().finish());
        adapter.<FriendBean>setOnItemClickListener(
                new MultiItemTypeAdapter.OnItemClickListener<FriendBean>() {
                    @Override
                    public void onItemClick(
                            View view,
                            RecyclerView.ViewHolder holder,
                            FriendBean userProfile,
                            int position) {
                        addFriend(userProfile.getUser_id());

//                        startActivity(
//                                UserDetailActivity.newIntent(
//                                        getActivity(), userProfile.getUserId(), false, false));
                    }

                    @Override
                    public boolean onItemLongClick(
                            View view,
                            RecyclerView.ViewHolder holder,
                            FriendBean userProfile,
                            int position) {
                        return false;
                    }
                });
        searchComponent.setSearchQueryListener(
                new SearchComponent.OnSearchQueryListener() {
                    @Override
                    public void onSearch(String query) {
                    }

                    @Override
                    public void onClickSearch(String query) {
                        mQuery = query;
                        getViewModel().findUser(query);
                    }
                });
        viewModel
                .getUserProfileLiveData()
                .observe(
                        getViewLifecycleOwner(),
                        new Observer<List<FriendBean>>() {
                            @Override
                            public void onChanged(List<FriendBean> userProfiles) {
                                adapter.setData(userProfiles);
                                emptyWrapper.notifyDataSetChanged();
                            }
                        });
    }

    /**
     * 从通讯录中删除
     */
    private void addFriend(String friendId) {
        // 弹出删除好友确认对话框
        CommonDialog dialog =
                new CommonDialog.Builder()
                        .setContentMessage("Add Friend?")
                        .setDialogButtonClickListener(
                                new CommonDialog.OnDialogButtonClickListener() {
                                    @Override
                                    public void onPositiveClick(View v, Bundle bundle) {
                                        // 标记正在删除好友
                                        getViewModel().addFriend(friendId, new Action<Object>() {
                                            @Override
                                            public void call(Object o) {
                                                Toast.makeText(getContext(), "add Friend Success", Toast.LENGTH_SHORT).show();
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
