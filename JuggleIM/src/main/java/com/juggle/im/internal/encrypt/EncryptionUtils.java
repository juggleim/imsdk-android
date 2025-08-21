package com.juggle.im.internal.encrypt;

public class EncryptionUtils {
    static {
        try {
            System.loadLibrary("imsdk_libs");
        } catch (UnsatisfiedLinkError e) {
            e.printStackTrace();
        }
    }

    /**
     * 生成客户端公钥
     * @param sessionId 会话ID
     * @param outPubkey 输出公钥的字节数组（长度必须为65字节）
     * @return 0表示成功，其他值表示失败
     */
    public static native int generateClientPubkey(String sessionId, byte[] outPubkey);

    /**
     * 验证并存储共享密钥
     * @param sessionId 会话ID
     * @param serverPubkey 服务端公钥（65字节）
     * @param signature 签名数据
     * @return 0表示成功，其他值表示失败
     */
    public static native int storeSharedKeyWithVerify(String sessionId, byte[] serverPubkey, byte[] signature);

    /**
     * 使用会话密钥加密数据
     * @param sessionId 会话ID
     * @param plaintext 明文数据
     * @return 加密后的数据，失败时返回null
     */
    public static native byte[] encryptWithSession(String sessionId, byte[] plaintext);

    /**
     * 使用会话密钥解密数据
     * @param sessionId 会话ID
     * @param encryptedData 加密的数据
     * @return 解密后的数据，失败时返回null
     */
    public static native byte[] decryptWithSession(String sessionId, byte[] encryptedData);

    /**
     * 生成客户端公钥载荷
     * @param sessionId 会话ID
     * @return 公钥载荷数据
     */
    public static native byte[] generateClientPubkeyPayload(String sessionId);

    /**
     * 使用载荷存储共享密钥
     * @param sessionId 会话ID
     * @param payload 载荷数据
     * @return 0表示成功，其他值表示失败
     */
    public static native int storeSharedKeyWithPayload(String sessionId, byte[] payload);
}
