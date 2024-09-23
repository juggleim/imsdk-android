package com.juggle.chat.bean;

public class ChatRoomBean {
    private String roomId;

    private String name;

    private String poster;

    public ChatRoomBean(String roomId, String name, String poster) {
        this.roomId = roomId;
        this.name = name;
        this.poster = poster;
    }

    public String getRoomId() {
        return roomId;
    }

    public String getName() {
        return name;
    }

    public String getPoster() {
        return poster;
    }
}
