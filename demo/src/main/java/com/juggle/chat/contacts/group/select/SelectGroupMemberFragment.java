package com.juggle.chat.contacts.group.select;

import android.app.Activity;
import android.content.Intent;
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
import com.juggle.chat.bean.GroupMemberBean;
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
import com.juggle.im.JIM;

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
    private String mGroupId;
    private int mType;// 0: create; 1: add member; 2: remove member; 3: multi call

    public SelectGroupMemberFragment(String groupId, int type) {
        mGroupId = groupId;
        mType = type;
    }

    @NonNull
    @Override
    protected AddFriendListViewModel onCreateViewModel(@NonNull Bundle bundle) {
        return new AddFriendListViewModel();
    }

    @Override
    protected View createView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        com.juggle.chat.databinding.FragmentCreateGroupsBinding binding = FragmentCreateGroupsBinding.inflate(inflater, container, false);

        StateHeaderComponent headerComponent = new StateHeaderComponent();
        headerComponent.getParams().setTitle(requireContext().getString(R.string.text_select_group_member));
        headerComponent.getParams().setUseLeftButton(true);
        headerComponent.getParams().setUseRightButton(true);
        headerComponent.getParams().setLeftButtonIcon(requireContext().getDrawable(R.drawable.icon_back));
        headerComponent.getParams().setLeftButtonIconTint(SendbirdUIKit.getDefaultThemeMode().getPrimaryTintColorStateList(requireContext()));
        if (mType == 0) {
            headerComponent.getParams().setRightButtonText(requireContext().getString(R.string.text_header_create_button));
            headerComponent.setOnRightButtonClickListener(v -> {
                showCreateGroupDialog();
            });
        } else if (mType == 1) {
            headerComponent.getParams().setRightButtonText(requireContext().getString(com.jet.im.kit.R.string.j_confirm));
            headerComponent.setOnRightButtonClickListener(v -> {
                addGroupMember();
            });
        } else if (mType == 2) {
            headerComponent.getParams().setRightButtonText(requireContext().getString(com.jet.im.kit.R.string.j_confirm));
            headerComponent.setOnRightButtonClickListener(v -> {
                removeGroupMember();
            });
        } else if (mType == 3) {
            headerComponent.getParams().setRightButtonText(requireContext().getString(com.jet.im.kit.R.string.j_confirm));
            headerComponent.setOnRightButtonClickListener(v -> {
                multiCall();
            });
        }
        headerComponent.setOnLeftButtonClickListener(v -> {
            if (getActivity() != null) {
                getActivity().finish();
            }
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
        if (mType == 0) {
            ServiceManager.getFriendsService().getFriendList(SendbirdUIKit.userId, "0", 200).enqueue(new CustomCallback<HttpResult<ListResult<FriendBean>>, ListResult<FriendBean>>() {
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
        } else if (mType == 1) {
            ServiceManager.getFriendsService().getFriendList(SendbirdUIKit.userId, "0", 200).enqueue(new CustomCallback<HttpResult<ListResult<FriendBean>>, ListResult<FriendBean>>() {
                @Override
                public void onSuccess(ListResult<FriendBean> listResult) {
                    if (listResult.getItems() != null && !listResult.getItems().isEmpty()) {
                        ServiceManager.getGroupsService().getGroupMembers(mGroupId).enqueue(new CustomCallback<HttpResult<ListResult<GroupMemberBean>>, ListResult<GroupMemberBean>>() {
                            @Override
                            public void onSuccess(ListResult<GroupMemberBean> groupMemberBeanListResult) {
                                List<SelectFriendBean> items = new ArrayList<>();
                                boolean existInGroup;
                                for (FriendBean friendBean : listResult.getItems()) {
                                    existInGroup = false;
                                    for (GroupMemberBean groupMemberBean : groupMemberBeanListResult.getItems()) {
                                        if (friendBean.getUser_id().equals(groupMemberBean.getUserId())) {
                                            existInGroup = true;
                                            break;
                                        }
                                    }
                                    if (!existInGroup) {
                                        items.add(new SelectFriendBean(friendBean));
                                    }
                                }
                                adapter.setData(items);
                            }
                        });
                    }
                }
            });
        } else if (mType == 2 || mType == 3) {
            ServiceManager.getGroupsService().getGroupMembers(mGroupId).enqueue(new CustomCallback<HttpResult<ListResult<GroupMemberBean>>, ListResult<GroupMemberBean>>() {
                @Override
                public void onSuccess(ListResult<GroupMemberBean> groupMemberBeanListResult) {
                    if (groupMemberBeanListResult.getItems() != null && !groupMemberBeanListResult.getItems().isEmpty()) {
                        List<SelectFriendBean> items = new ArrayList<>();
                        for (GroupMemberBean groupMemberBean : groupMemberBeanListResult.getItems()) {
                            if (!groupMemberBean.getUserId().equals(JIM.getInstance().getCurrentUserId())) {
                                items.add(new SelectFriendBean(groupMemberBean));
                            }
                        }
                        adapter.setData(items);
                    }
                }
            });
        }
    }

    protected void showCreateGroupDialog() {
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

    private void addGroupMember() {
        List<String> userIdList = new ArrayList<>();
        for (SelectFriendBean selectFriendBean : adapter.getData()) {
            if (selectFriendBean.isSelected()) {
                userIdList.add(selectFriendBean.getUser_id());
            }
        }
        if (userIdList.isEmpty()) {
            Toast.makeText(getContext(), "group member is empty", Toast.LENGTH_SHORT).show();
            return;
        }
        JsonArray array = new JsonArray();
        for (String userId : userIdList) {
            array.add(userId);
        }
        JsonObject item = new JsonObject();
        item.addProperty("group_id", mGroupId);
        item.add("member_ids", array);
        RequestBody body = RequestBody.create(ServiceManager.MEDIA_TYPE_JSON, item.toString());
        ServiceManager.getGroupsService().addMember(body).enqueue(new CustomCallback<HttpResult<Object>, Object>() {
            @Override
            public void onSuccess(Object o) {
                Toast.makeText(getContext(), "invite success", Toast.LENGTH_SHORT).show();
                if (getActivity() != null) {
                    getActivity().finish();
                }
            }
        });
    }

    private void removeGroupMember() {
        List<String> userIdList = new ArrayList<>();
        for (SelectFriendBean selectFriendBean : adapter.getData()) {
            if (selectFriendBean.isSelected()) {
                userIdList.add(selectFriendBean.getUser_id());
            }
        }
        if (userIdList.isEmpty()) {
            Toast.makeText(getContext(), "select member empty", Toast.LENGTH_SHORT).show();
            return;
        }
        JsonArray array = new JsonArray();
        for (String userId : userIdList) {
            array.add(userId);
        }
        JsonObject item = new JsonObject();
        item.addProperty("group_id", mGroupId);
        item.add("member_ids", array);
        RequestBody body = RequestBody.create(ServiceManager.MEDIA_TYPE_JSON, item.toString());

        ServiceManager.getGroupsService().removeMember(body).enqueue(new CustomCallback<HttpResult<Object>, Object>() {
            @Override
            public void onSuccess(Object o) {
                Toast.makeText(getContext(), "remove success", Toast.LENGTH_SHORT).show();
                if (getActivity() != null) {
                    getActivity().finish();
                }
            }
        });
    }

    private void multiCall() {
        if (getActivity() == null) {
            return;
        }
        ArrayList<String> userIdList = new ArrayList<>();
        for (SelectFriendBean selectFriendBean : adapter.getData()) {
            if (selectFriendBean.isSelected()) {
                userIdList.add(selectFriendBean.getUser_id());
            }
        }
        if (userIdList.isEmpty()) {
            Toast.makeText(getContext(), "select member empty", Toast.LENGTH_SHORT).show();
            return;
        }
        Intent intent = new Intent();
        intent.putStringArrayListExtra("userIdList", userIdList);
        getActivity().setResult(Activity.RESULT_OK, intent);
        getActivity().finish();
    }
}
