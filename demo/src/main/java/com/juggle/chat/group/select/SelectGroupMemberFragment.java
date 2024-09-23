package com.juggle.chat.group.select;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.juggle.chat.base.BasePageFragment;
import com.juggle.chat.bean.CreateGroupResult;
import com.juggle.chat.bean.FriendBean;
import com.juggle.chat.bean.HttpResult;
import com.juggle.chat.bean.ListResult;
import com.juggle.chat.bean.SelectFriendBean;
import com.juggle.chat.common.adapter.CommonAdapter;
import com.juggle.chat.common.widgets.SimpleInputDialog;
import com.juggle.chat.common.widgets.TitleBar;
import com.juggle.chat.databinding.FragmentCreateGroupsBinding;
import com.juggle.chat.http.CustomCallback;
import com.juggle.chat.http.ServiceManager;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.jet.im.kit.SendbirdUIKit;
import com.jet.im.kit.utils.TextUtils;

import java.util.ArrayList;
import java.util.List;

import okhttp3.RequestBody;

/**
 * 功能描述: 创建群组页面
 *
 * @author rongcloud
 * @since 5.10.4
 */
public class SelectGroupMemberFragment
        extends BasePageFragment<AddFriendListViewModel> {
    private FragmentCreateGroupsBinding binding;
    private CommonAdapter<SelectFriendBean> adapter = new SelectMemberAdapter();


    @NonNull
    @Override
    protected AddFriendListViewModel onCreateViewModel(@NonNull Bundle bundle) {
        return new AddFriendListViewModel();
    }

    @Override
    protected View createView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentCreateGroupsBinding.inflate(inflater, container, false);
        binding.rvList.setAdapter(adapter);
        binding.rvList.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.tvTitle.setOnRightIconClickListener(new TitleBar.OnRightIconClickListener() {
            @Override
            public void onRightIconClick(View v) {
                showAddFriendDialog();
            }
        });
        return binding.getRoot();
    }

    @Override
    protected void onViewReady(@NonNull AddFriendListViewModel viewModel) {
        refresh();
    }

    protected void refresh() {
        ServiceManager.friendsService().getFriendList(SendbirdUIKit.userId, "0", 200).enqueue(new CustomCallback<HttpResult<ListResult<FriendBean>>, ListResult<FriendBean>>() {
            @Override
            public void onSuccess(ListResult<FriendBean> listResult) {
                if (listResult.getItems() != null && !listResult.getItems().isEmpty()) {
                    List<SelectFriendBean> items = new ArrayList<>();
                    for (FriendBean item : listResult.getItems()) {
                        items.add(new SelectFriendBean(item));
                    }
                    adapter.setData(items);
                }
            }
        });
    }

    protected void showAddFriendDialog() {
        SimpleInputDialog dialog = new SimpleInputDialog();
        //        dialog.setInputHint(getString(R.string.profile_add_friend_hint));
        dialog.setTitleText("Group Name");
        dialog.setInputDialogListener(
                new SimpleInputDialog.InputDialogListener() {
                    @Override
                    public boolean onConfirmClicked(EditText input) {
                        String inviteMsg = input.getText().toString();

                        if (TextUtils.isEmpty(inviteMsg)) {
                            Toast.makeText(getContext(), "group name is not empty", Toast.LENGTH_SHORT).show();
                            return true;
                        }
                        List<String> users = new ArrayList<>();
                        for (SelectFriendBean item : adapter.getData()) {
                            if (item.isSelected()) {
                                users.add(item.getUser_id());
                            }
                        }
                        if (users.isEmpty()) {
                            Toast.makeText(getContext(), "group member is not empty", Toast.LENGTH_SHORT).show();
                            return true;
                        }
                        createGroup(inviteMsg, users);
                        return true;
                    }
                });
        dialog.show(getParentFragmentManager(), null);
    }

    private void createGroup(String groupName, List<String> users) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("group_name", groupName);
        jsonObject.addProperty("group_portrait", "");
        JsonArray jsonArray = new JsonArray();
        for (String user : users) {
            JsonObject item = new JsonObject();
            item.addProperty("user_id", user);
            jsonArray.add(item);
        }
        JsonObject item = new JsonObject();
        item.addProperty("user_id", SendbirdUIKit.userId);
        jsonArray.add(item);
        jsonObject.add("members", jsonArray);
        RequestBody body = RequestBody.create(ServiceManager.MEDIA_TYPE_JSON, jsonObject.toString());
        ServiceManager.getGroupsService().createGroup(body).enqueue(new CustomCallback<HttpResult<CreateGroupResult>, CreateGroupResult>() {
            @Override
            public void onSuccess(CreateGroupResult o) {
                Toast.makeText(getContext(), "create success", Toast.LENGTH_SHORT).show();
            }
        });
    }

//    private void addMember(String groupId, List<String> users) {
//        JsonObject jsonObject = new JsonObject();
//        jsonObject.addProperty("group_id", groupId);
//        JsonArray jsonArray = new JsonArray();
//        for (String user : users) {
//            JsonObject item = new JsonObject();
//            item.addProperty("user_id", user);
//            jsonArray.add(item);
//        }
//        JsonObject item = new JsonObject();
//        item.addProperty("user_id", SendbirdUIKit.userId);
//        jsonArray.add(item);
//        jsonObject.add("members", jsonArray);
//        ServiceManager.getGroupsService().addMember(body).enqueue(new CustomCallback<HttpResult<CreateGroupResult>, CreateGroupResult>() {
//            @Override
//            public void onSuccess(CreateGroupResult o) {
//                Toast.makeText(getContext(), "create success", Toast.LENGTH_SHORT).show();
//            }
//        });
//    }
}
