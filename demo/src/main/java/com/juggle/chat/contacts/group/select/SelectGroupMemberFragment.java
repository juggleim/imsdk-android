package com.juggle.chat.contacts.group.select;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.jet.im.kit.modules.components.StateHeaderComponent;
import com.juggle.chat.R;
import com.juggle.chat.base.BasePageFragment;
import com.juggle.chat.bean.CreateGroupResult;
import com.juggle.chat.bean.FriendBean;
import com.juggle.chat.bean.HttpResult;
import com.juggle.chat.bean.ListResult;
import com.juggle.chat.bean.SelectFriendBean;
import com.juggle.chat.common.adapter.CommonAdapter;
import com.juggle.chat.common.widgets.SimpleInputDialog;
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
 */
public class SelectGroupMemberFragment
        extends BasePageFragment<AddFriendListViewModel> {
    private final CommonAdapter<SelectFriendBean> adapter = new SelectMemberAdapter();

    @NonNull
    @Override
    protected AddFriendListViewModel onCreateViewModel(@NonNull Bundle bundle) {
        return new AddFriendListViewModel();
    }

    @Override
    protected View createView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        com.juggle.chat.databinding.FragmentCreateGroupsBinding binding = FragmentCreateGroupsBinding.inflate(inflater, container, false);

        StateHeaderComponent headerComponent = new StateHeaderComponent();
        headerComponent.getParams().setTitle("");
        headerComponent.getParams().setUseLeftButton(true);
        headerComponent.getParams().setUseRightButton(true);
        headerComponent.getParams().setLeftButtonIcon(requireContext().getDrawable(R.drawable.icon_back));
        headerComponent.getParams().setLeftButtonIconTint(SendbirdUIKit.getDefaultThemeMode().getPrimaryTintColorStateList(requireContext()));
        headerComponent.getParams().setRightButtonText(requireContext().getString(R.string.text_header_create_button));
        headerComponent.setOnLeftButtonClickListener(v -> {
            if (getActivity() != null) {
                getActivity().finish();
            }
        });
        headerComponent.setOnRightButtonClickListener(v -> {
            showAddFriendDialog();
        });
        View header = headerComponent.onCreateView(requireContext(), inflater, binding.headerComponent, savedInstanceState);
        binding.headerComponent.addView(header);

        binding.rvList.setAdapter(adapter);
        binding.rvList.setLayoutManager(new LinearLayoutManager(getContext()));
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
        dialog.setInputHint(getString(R.string.text_group_name));
        dialog.setTitleText(getString(R.string.text_group_name));
        dialog.setInputDialogListener(
                new SimpleInputDialog.InputDialogListener() {
                    @Override
                    public boolean onConfirmClicked(EditText input) {
                        String inviteMsg = input.getText().toString();

                        if (TextUtils.isEmpty(inviteMsg)) {
                            Toast.makeText(getContext(), "group name is empty", Toast.LENGTH_SHORT).show();
                            return true;
                        }
                        List<String> users = new ArrayList<>();
                        for (SelectFriendBean item : adapter.getData()) {
                            if (item.isSelected()) {
                                users.add(item.getUser_id());
                            }
                        }
                        if (users.isEmpty()) {
                            Toast.makeText(getContext(), "group member is empty", Toast.LENGTH_SHORT).show();
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
                if (getActivity() != null) {
                    getActivity().finish();
                }
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
