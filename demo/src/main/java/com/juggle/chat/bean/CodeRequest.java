package com.juggle.chat.bean;

public class CodeRequest {
    private String phone;

    public CodeRequest(String phone) {
        this.phone = phone;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }
}
