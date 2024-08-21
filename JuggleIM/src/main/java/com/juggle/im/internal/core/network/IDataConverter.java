package com.juggle.im.internal.core.network;

public interface IDataConverter {

    byte[] encode(byte[] data);

    byte[] decode(byte[] data);
}
