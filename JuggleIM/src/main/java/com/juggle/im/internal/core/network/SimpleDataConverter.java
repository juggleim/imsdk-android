package com.juggle.im.internal.core.network;

import java.nio.ByteBuffer;
import java.util.Random;

public class SimpleDataConverter implements IDataConverter{

    public SimpleDataConverter() {
        Random random = new Random();
        long randNumber = random.nextLong();
        mKey = ByteBuffer.allocate(8).putLong(randNumber).array();
    }

    @Override
    public byte[] encode(byte[] data) {
        return xorDataWithKey(data, mKey);
    }

    @Override
    public byte[] decode(byte[] data) {
        return xorDataWithKey(data, mKey);
    }

    private byte[] xorDataWithKey(byte[] sourceData, byte[] key) {
        int keyLen = key.length;
        byte[] result = new byte[sourceData.length];
        for (int i = 0; i < sourceData.length; i++) {
            result[i] = (byte)(sourceData[i] ^ key[i%keyLen]);
        }
        return result;
    }

    private final byte[] mKey;
}
