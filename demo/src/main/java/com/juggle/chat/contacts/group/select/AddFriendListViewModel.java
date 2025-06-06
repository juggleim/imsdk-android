package com.juggle.chat.contacts.group.select;


import androidx.lifecycle.MutableLiveData;

import com.juggle.chat.base.Action;
import com.juggle.chat.base.BaseViewModel;
import com.juggle.chat.bean.HttpResult;
import com.juggle.chat.bean.ListResult;
import com.juggle.chat.bean.SearchUserBean;
import com.juggle.chat.http.CustomCallback;
import com.juggle.chat.http.ServiceManager;
import com.jet.im.kit.SendbirdUIKit;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import okhttp3.RequestBody;


/**
 * 功能描述: 创建群组ViewModel
 *
 */
public class AddFriendListViewModel extends BaseViewModel {
    private final MutableLiveData<List<SearchUserBean>> userProfileLiveData = new MutableLiveData<>();

    public AddFriendListViewModel() {
        super(null);
    }

    public MutableLiveData<List<SearchUserBean>> getUserProfileLiveData() {
        return userProfileLiveData;
    }

    public void findUser(String phone) {
        HashMap<String, Object> paramsMap = new HashMap<>();
        paramsMap.put("phone", phone);
        RequestBody body = ServiceManager.createJsonRequest(paramsMap);
        ServiceManager.getFriendsService().searchUsers(body).enqueue(new CustomCallback<HttpResult<ListResult<SearchUserBean>>, ListResult<SearchUserBean>>() {
            @Override
            public void onSuccess(ListResult<SearchUserBean> listResult) {
                if (listResult != null) {
                    userProfileLiveData.postValue(listResult.getItems());
                } else {
                    userProfileLiveData.postValue(Collections.emptyList());
                }
            }

            @Override
            public void onError(Throwable t) {
//                super.onError(t);
                userProfileLiveData.postValue(Collections.emptyList());
            }
        });
    }

    public void addFriend(String friendId, Action<Object> callback) {
        HashMap<String, Object> paramsMap = new HashMap<>();
        paramsMap.put("user_id", SendbirdUIKit.userId);
        paramsMap.put("friend_id", friendId);
        RequestBody body = ServiceManager.createJsonRequest(paramsMap);
        ServiceManager.getFriendsService().addFriend(body).enqueue(new CustomCallback<HttpResult<Object>, Object>() {
            @Override
            public void onSuccess(Object o) {
                callback.call(null);
            }
        });
    }

    @Override
    protected void onCleared() {
        super.onCleared();
    }
}
