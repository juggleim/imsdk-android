package com.jet.im.kit.activities.adapter;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;

import com.sendbird.android.channel.Role;
import com.sendbird.android.user.Member;
import com.jet.im.kit.R;
import com.jet.im.kit.activities.adapter.UserTypeDiffCallback;
import com.jet.im.kit.activities.adapter.UserTypeListAdapter;

import java.util.List;

/**
 * MemberListAdapter provides a binding from a {@link Member} type data set to views that are displayed within a RecyclerView.
 *
 * since 3.0.0
 */
public class MemberListAdapter extends UserTypeListAdapter<Member> {
    @NonNull
    private Role myRole = Role.NONE;

    /**
     * Sets the {@link List <Member>} to be displayed.
     *
     * @param userList list to be displayed
     * @param myRole The role of the current user
     */
    public void setItems(@NonNull List<Member> userList, @NonNull Role myRole) {
        final UserTypeDiffCallback<Member> diffCallback = UserTypeDiffCallback.createFromGroupChannel(getItems(), userList, this.myRole, myRole);
        final DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(diffCallback);

        setUsers(userList);
        this.myRole = myRole;
        diffResult.dispatchUpdatesTo(this);
    }

    @Override
    protected boolean isCurrentUserOperator() {
        return myRole == Role.OPERATOR;
    }

    @NonNull
    @Override
    protected String getItemViewDescription(@NonNull Context context, @NonNull Member member) {
        return member.getRole() == Role.OPERATOR ? context.getString(R.string.sb_text_operator) : "";
    }
}
