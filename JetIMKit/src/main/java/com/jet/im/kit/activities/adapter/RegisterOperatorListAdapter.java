package com.jet.im.kit.activities.adapter;

import androidx.annotation.NonNull;

import com.sendbird.android.channel.Role;
import com.sendbird.android.user.Member;
import com.jet.im.kit.interfaces.UserInfo;
import com.jet.im.kit.utils.UserUtils;

/**
 * RegisterOperatorListAdapter provides a binding from a {@link Member} type data set to views that are displayed within a RecyclerView.
 *
 * since 3.0.0
 */
public class RegisterOperatorListAdapter extends SelectUserListAdapter<Member> {
    @Override
    protected boolean isDisabled(@NonNull Member item) {
        return item.getRole() == Role.OPERATOR;
    }

    @Override
    protected boolean isSelected(@NonNull Member item) {
        return selectedUserIdList.contains(item.getUserId());
    }

    @NonNull
    @Override
    protected UserInfo toUserInfo(@NonNull Member member) {
        return UserUtils.toUserInfo(member);
    }
}
