package com.juggle.im.model;

import android.text.TextUtils;

import com.juggle.im.JIM;

public class ConversationInfo {
    public Conversation getConversation() {
        return mConversation;
    }

    public void setConversation(Conversation conversation) {
        mConversation = conversation;
    }

    public int getUnreadCount() {
        return mUnreadCount;
    }

    public void setUnreadCount(int unreadCount) {
        mUnreadCount = unreadCount;
    }

    public boolean hasUnread() {
        return mHasUnread;
    }

    public void setUnread(boolean hasUnread) {
        mHasUnread = hasUnread;
    }

    public long getSortTime() {
        return mSortTime;
    }

    public void setSortTime(long sortTime) {
        mSortTime = sortTime;
    }

    public Message getLastMessage() {
        return mLastMessage;
    }

    public void setLastMessage(Message lastMessage) {
        mLastMessage = lastMessage;
    }

    public boolean isTop() {
        return mIsTop;
    }

    public void setTop(boolean top) {
        mIsTop = top;
    }

    public long getTopTime() {
        return mTopTime;
    }

    public void setTopTime(long topTime) {
        mTopTime = topTime;
    }

    public boolean isMute() {
        return mMute;
    }

    public void setMute(boolean mute) {
        mMute = mute;
    }

    public String getDraft() {
        return mDraft;
    }

    public void setDraft(String draft) {
        mDraft = draft;
    }

    public ConversationMentionInfo getMentionInfo() {
        return mMentionInfo;
    }

    public String getDisplayName() {
        String displayName = "";
        if (mConversation == null) {
            return displayName;
        }
        if (mConversation.getConversationType() == Conversation.ConversationType.GROUP
        || mConversation.getConversationType() == Conversation.ConversationType.PUBLIC_SERVICE) {
            GroupInfo groupInfo = getGroupInfo();
            if (groupInfo != null) {
                displayName = groupInfo.getGroupName();
            }
        } else if (mConversation.getConversationType() == Conversation.ConversationType.PRIVATE) {
            FriendInfo friendInfo = JIM.getInstance().getUserInfoManager().getFriendInfo(mConversation.getConversationId());
            if (friendInfo != null) {
                displayName = friendInfo.getAlias();
            }
            if (TextUtils.isEmpty(displayName)) {
                UserInfo userInfo = getUserInfo();
                if (userInfo != null) {
                    displayName = userInfo.getUserName();
                }
            }
        }
        return displayName;
    }

    public String getAlias() {
        String alias = "";
        if (mConversation == null) {
            return alias;
        }
        if (mConversation.getConversationType() == Conversation.ConversationType.GROUP
        || mConversation.getConversationType() == Conversation.ConversationType.PUBLIC_SERVICE) {
            GroupInfo groupInfo = getGroupInfo();
            if (groupInfo != null) {
                alias = groupInfo.getGroupName();
            }
        } else if (mConversation.getConversationType() == Conversation.ConversationType.PRIVATE) {
            FriendInfo friendInfo = JIM.getInstance().getUserInfoManager().getFriendInfo(mConversation.getConversationId());
            if (friendInfo != null) {
                alias = friendInfo.getAlias();
            }
        }
        return alias;
    }

    public String getName() {
        String name = "";
        if (mConversation == null) {
            return name;
        }
        if (mConversation.getConversationType() == Conversation.ConversationType.GROUP
                || mConversation.getConversationType() == Conversation.ConversationType.PUBLIC_SERVICE) {
            GroupInfo groupInfo = getGroupInfo();
            if (groupInfo != null) {
                name = groupInfo.getGroupName();
            }
        } else if (mConversation.getConversationType() == Conversation.ConversationType.PRIVATE) {
            UserInfo userInfo = getUserInfo();
            if (userInfo != null) {
                name = userInfo.getUserName();
            }
        }
        return name;
    }

    public String getPortrait() {
        String portrait = "";
        if (mConversation == null) {
            return portrait;
        }
        if (mConversation.getConversationType() == Conversation.ConversationType.GROUP
                || mConversation.getConversationType() == Conversation.ConversationType.PUBLIC_SERVICE) {
            GroupInfo groupInfo = getGroupInfo();
            if (groupInfo != null) {
                portrait = groupInfo.getPortrait();
            }
        } else if (mConversation.getConversationType() == Conversation.ConversationType.PRIVATE) {
            UserInfo userInfo = getUserInfo();
            if (userInfo != null) {
                portrait = userInfo.getPortrait();
            }
        }
        return portrait;
    }

    public void setMentionInfo(ConversationMentionInfo mentionInfo) {
        this.mMentionInfo = mentionInfo;
    }

    private GroupInfo getGroupInfo() {
        if (mGroupInfo == null) {
            if (mConversation != null) {
                mGroupInfo = JIM.getInstance().getUserInfoManager().getGroupInfo(mConversation.getConversationId());
            }
        }
        return mGroupInfo;
    }

    private UserInfo getUserInfo() {
        if (mUserInfo == null) {
            if (mConversation != null) {
                mUserInfo = JIM.getInstance().getUserInfoManager().getUserInfo(mConversation.getConversationId());
            }
        }
        return mUserInfo;
    }

    private Conversation mConversation;
    private int mUnreadCount;
    private boolean mHasUnread;
    private long mSortTime;
    private Message mLastMessage;
    private boolean mIsTop;
    private long mTopTime;
    private boolean mMute;
    private String mDraft;
    private ConversationMentionInfo mMentionInfo;
    private GroupInfo mGroupInfo;
    private UserInfo mUserInfo;
}
