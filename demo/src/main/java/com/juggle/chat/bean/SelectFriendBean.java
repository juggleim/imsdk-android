package com.juggle.chat.bean;

public class SelectFriendBean extends FriendBean {
    private boolean selected;

    public SelectFriendBean(FriendBean friendBean) {
        setAvatar(friendBean.getAvatar());
        setNickname(friendBean.getNickname());
        setPhone(friendBean.getPhone());
        setUser_id(friendBean.getUser_id());
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }
}
