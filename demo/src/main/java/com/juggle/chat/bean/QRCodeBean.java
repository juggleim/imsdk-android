package com.juggle.chat.bean;

import com.google.gson.annotations.SerializedName;

public class QRCodeBean {
    public String getQrCode() {
        return qrCode;
    }

    public void setQrCode(String qrCode) {
        this.qrCode = qrCode;
    }

    @SerializedName("qr_code")
    private String qrCode;
}
