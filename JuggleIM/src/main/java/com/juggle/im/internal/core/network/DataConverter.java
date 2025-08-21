package com.juggle.im.internal.core.network;

import com.juggle.im.internal.encrypt.EncryptionUtils;
import com.juggle.im.internal.util.JLogger;
import com.juggle.im.internal.util.JUtility;

public class DataConverter implements IDataConverter {
    public DataConverter() {
        mSession = JUtility.getUUID();
    }

    public byte[] getPubKey() {
        return EncryptionUtils.generateClientPubkeyPayload(mSession);
    }

    public void storeSharedKey(byte[] key) {
        if (key == null || key.length == 0) {
            mActive = false;
            JLogger.i("CON-Enc", "storeSharedKey key is null");
            return;
        }
        mActive = true;
        int result = EncryptionUtils.storeSharedKeyWithPayload(mSession, key);
        if (result != 0) {
            JLogger.e("CON-Enc", "storeSharedKeyWithPayload error, " + result);
        }
    }

    @Override
    public byte[] encode(byte[] data) {
        if (mActive) {
            return EncryptionUtils.encryptWithSession(mSession, data);
        } else {
            return data;
        }
    }

    @Override
    public byte[] decode(byte[] data) {
        if (mActive) {
            return EncryptionUtils.decryptWithSession(mSession, data);
        } else {
            return data;
        }
    }

    private final String mSession;
    private boolean mActive;
}
