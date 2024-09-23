package com.juggle.chat.friends.add;


import androidx.lifecycle.MutableLiveData;

import com.juggle.chat.base.Action;
import com.juggle.chat.base.BaseViewModel;
import com.juggle.chat.bean.FriendBean;
import com.juggle.chat.bean.HttpResult;
import com.juggle.chat.bean.ListResult;
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
 * @author rongcloud
 * @since 5.10.4
 */
public class AddFriendListViewModel extends BaseViewModel {
    private final MutableLiveData<List<FriendBean>> userProfileLiveData = new MutableLiveData<>();

    public AddFriendListViewModel() {
        super(null);
    }

    public MutableLiveData<List<FriendBean>> getUserProfileLiveData() {
        return userProfileLiveData;
    }

    public void findUser(String phone) {
        HashMap<String, Object> paramsMap = new HashMap<>();
        paramsMap.put("phone", phone);
        RequestBody body = ServiceManager.createJsonRequest(paramsMap);
        ServiceManager.friendsService().searchUsers(body).enqueue(new CustomCallback<HttpResult<ListResult<FriendBean>>, ListResult<FriendBean>>() {
            @Override
            public void onSuccess(ListResult<FriendBean> listResult) {
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
        ServiceManager.friendsService().addFriend(body).enqueue(new CustomCallback<HttpResult<Object>, Object>() {
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
