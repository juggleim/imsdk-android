package com.juggle.im.internal.core.network;

public interface IUltEncrypt {
    byte[] getPubKey();
    void storeSharedKey(byte[] key);
    byte[] encrypt(byte[] data);
    byte[] decrypt(byte[] data);
}
