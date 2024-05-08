package com.example.demo.customization.userlist

import android.app.Activity
import com.jet.im.kit.SendbirdUIKit
import com.jet.im.kit.activities.CreateChannelActivity
import com.jet.im.kit.consts.CreatableChannelType
import com.jet.im.kit.interfaces.CustomUserListQueryHandler
import com.jet.im.kit.interfaces.OnListResultHandler
import com.jet.im.kit.interfaces.UserInfo
import com.jet.im.kit.vm.CreateChannelViewModel
import com.jet.im.kit.vm.InviteUserViewModel
import java.lang.Integer.min

/**
 * In this sample, profile data in user information is integrated into one image on the user list screen.
 *
 * Through [SendbirdUIKit.setCustomUserListQueryHandler], you can apply a custom user list data source to the user list.
 * The user list data source is used in [CreateChannelViewModel] and [InviteUserViewModel].
 *
 * step 1. Create a custom user list data source by implementing [CustomUserListQueryHandler].
 * step 2. Set [CustomUserListQueryHandler] to [SendbirdUIKit.setCustomUserListQueryHandler].
 */
fun showUserItemDataSourceSample(activity: Activity) {
    val customUserList = createCustomUserList()
    var loadIndex = 0
    val loadSize = 10

    SendbirdUIKit.setCustomUserListQueryHandler(object : CustomUserListQueryHandler {
        override fun loadInitial(handler: OnListResultHandler<UserInfo>) {
            loadIndex = 0
            val endIndex = min(loadSize, customUserList.size)

            handler.onResult(customUserList.subList(0, endIndex), null)
            loadIndex = endIndex
        }

        override fun loadMore(handler: OnListResultHandler<UserInfo>) {
            val endIndex = min(loadIndex + loadSize, customUserList.size)

            handler.onResult(customUserList.subList(loadIndex, endIndex), null)
            loadIndex = endIndex
        }

        override fun hasMore(): Boolean {
            return loadIndex < customUserList.count()
        }
    })

    val intent = CreateChannelActivity.newIntent(activity.applicationContext, CreatableChannelType.Normal)
    activity.startActivity(intent)
}

private fun createCustomUserList(): List<UserInfo> = (1..30).map {
    CustomUserInfo("userId_$it", "nickname_$it")
}

/**
 * This class demonstrates how to bind your own user information to the user list.
 */
private class CustomUserInfo(
    private val userId: String,
    private val nickname: String? = null
) : UserInfo {
    override fun getUserId(): String {
        return userId
    }

    override fun getNickname(): String? {
        return nickname
    }

    override fun getProfileUrl(): String {
        return "https://avatars.githubusercontent.com/u/5143408?s=200&v=4"
    }
}
