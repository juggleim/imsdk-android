package com.juggle.im.internal.util;

import com.juggle.im.internal.model.E2EEInfo;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.security.spec.AlgorithmParameterSpec;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.crypto.AEADBadTagException;
import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class EncryptUtility {
    private static final int X25519_KEY_SIZE = 32;
    private static final int AES_256_GCM_KEY_SIZE = 32;
    private static final int AES_GCM_NONCE_SIZE = 12;
    private static final int AES_GCM_TAG_SIZE = 16;
    private static final int AES_GCM_TAG_SIZE_BITS = 128;
    private static final String AES_GCM_TRANSFORMATION = "AES/GCM/NoPadding";
    private static final String AES_ALGORITHM = "AES";
    private static final String HMAC_SHA256_ALGORITHM = "HmacSHA256";
    private static final String SHA256_ALGORITHM = "SHA-256";
    private static final char[] BASE64_ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/".toCharArray();
    private static final byte[] HKDF_SALT = "protocol-v1".getBytes(StandardCharsets.UTF_8);
    private static final byte[] HKDF_INFO = "envelope-v1".getBytes(StandardCharsets.UTF_8);
    private static final BigInteger P = BigInteger.ONE.shiftLeft(255).subtract(BigInteger.valueOf(19));
    private static final BigInteger A24 = BigInteger.valueOf(121665);
    private static final BigInteger BASE_POINT_U = BigInteger.valueOf(9);
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private EncryptUtility() {
    }

    public static byte[] generateX25519PrivateKey() {
        byte[] privateKey = new byte[X25519_KEY_SIZE];
        SECURE_RANDOM.nextBytes(privateKey);
        clampScalar(privateKey);
        return privateKey;
    }

    public static byte[] generateX25519PublicKey(byte[] privateKey) {
        return x25519(privateKey, encodeLittleEndian(BASE_POINT_U));
    }

    public static byte[] calculateX25519SharedSecret(byte[] privateKey, byte[] peerPublicKey) {
        return x25519(privateKey, peerPublicKey);
    }

    public static byte[] generateAes256GcmKey() {
        byte[] key = new byte[AES_256_GCM_KEY_SIZE];
        SECURE_RANDOM.nextBytes(key);
        return key;
    }

    public static byte[] generateAes256GcmNonce() {
        byte[] nonce = new byte[AES_GCM_NONCE_SIZE];
        SECURE_RANDOM.nextBytes(nonce);
        return nonce;
    }

    public static byte[] encryptAes256Gcm(byte[] key, byte[] nonce, byte[] aad, byte[] plaintext, byte[] tag) {
        checkAes256GcmKeyNonceAndTag(key, nonce, tag);
        if (plaintext == null) {
            throw new IllegalArgumentException("plaintext must not be null");
        }
        byte[] encrypted = doAes256Gcm(Cipher.ENCRYPT_MODE, key, nonce, aad, plaintext);
        int ciphertextLength = encrypted.length - AES_GCM_TAG_SIZE;
        if (ciphertextLength < 0) {
            throw new IllegalArgumentException("AES-256-GCM operation failed");
        }
        byte[] ciphertext = new byte[ciphertextLength];
        System.arraycopy(encrypted, 0, ciphertext, 0, ciphertextLength);
        System.arraycopy(encrypted, ciphertextLength, tag, 0, AES_GCM_TAG_SIZE);
        return ciphertext;
    }

    public static byte[] decryptAes256Gcm(byte[] key, byte[] nonce, byte[] aad, byte[] ciphertext, byte[] tag) {
        checkAes256GcmKeyNonceAndTag(key, nonce, tag);
        if (ciphertext == null) {
            throw new IllegalArgumentException("ciphertext must not be null");
        }
        byte[] encrypted = new byte[ciphertext.length + AES_GCM_TAG_SIZE];
        System.arraycopy(ciphertext, 0, encrypted, 0, ciphertext.length);
        System.arraycopy(tag, 0, encrypted, ciphertext.length, AES_GCM_TAG_SIZE);
        try {
            return doAes256Gcm(Cipher.DECRYPT_MODE, key, nonce, aad, encrypted);
        } catch (IllegalArgumentException e) {
            if (causedByAeadBadTag(e)) {
                throw new IllegalArgumentException("ciphertext authentication failed", e.getCause());
            }
            throw e;
        }
    }

    public static String calcPubKeysSHA256Base64WithInfoList(List<E2EEInfo> infoList) {
        if (infoList == null || infoList.isEmpty()) {
            return "";
        }
        List<E2EEInfo> sortedList = new ArrayList<>(infoList);
        Collections.sort(sortedList, new Comparator<E2EEInfo>() {
            @Override
            public int compare(E2EEInfo lhs, E2EEInfo rhs) {
                return sortKey(lhs).compareTo(sortKey(rhs));
            }
        });

        int totalLength = 0;
        for (E2EEInfo info : sortedList) {
            byte[] pubKey = info == null ? null : info.getPubKey();
            if (pubKey != null && pubKey.length > 0) {
                totalLength += pubKey.length;
            }
        }

        byte[] concatPubKeys = new byte[totalLength];
        int offset = 0;
        for (E2EEInfo info : sortedList) {
            byte[] pubKey = info == null ? null : info.getPubKey();
            if (pubKey != null && pubKey.length > 0) {
                System.arraycopy(pubKey, 0, concatPubKeys, offset, pubKey.length);
                offset += pubKey.length;
            }
        }

        try {
            MessageDigest digest = MessageDigest.getInstance(SHA256_ALGORITHM);
            return base64Encode(digest.digest(concatPubKeys));
        } catch (Exception e) {
            throw new IllegalArgumentException("SHA-256 operation failed", e);
        }
    }

    public static byte[] deriveAES256KeyFromSharedSecret(byte[] sharedSecret) {
        if (sharedSecret == null) {
            throw new IllegalArgumentException("sharedSecret must not be null");
        }
        try {
            byte[] prk = hmacSha256(HKDF_SALT, sharedSecret);
            byte[] expandInput = new byte[HKDF_INFO.length + 1];
            System.arraycopy(HKDF_INFO, 0, expandInput, 0, HKDF_INFO.length);
            expandInput[expandInput.length - 1] = 1;
            return hmacSha256(prk, expandInput);
        } catch (Exception e) {
            throw new IllegalArgumentException("AES-256 key derivation failed", e);
        }
    }

    private static byte[] x25519(byte[] scalar, byte[] uCoordinate) {
        checkLength(scalar, "scalar");
        checkLength(uCoordinate, "uCoordinate");

        byte[] clampedScalar = scalar.clone();
        clampScalar(clampedScalar);

        byte[] encodedU = uCoordinate.clone();
        encodedU[31] &= 0x7f;
        BigInteger x1 = decodeLittleEndian(encodedU);
        BigInteger x2 = BigInteger.ONE;
        BigInteger z2 = BigInteger.ZERO;
        BigInteger x3 = x1;
        BigInteger z3 = BigInteger.ONE;
        int swap = 0;

        for (int t = 254; t >= 0; t--) {
            int kT = bitAt(clampedScalar, t);
            swap ^= kT;
            BigInteger[] xSwap = conditionalSwap(swap, x2, x3);
            x2 = xSwap[0];
            x3 = xSwap[1];
            BigInteger[] zSwap = conditionalSwap(swap, z2, z3);
            z2 = zSwap[0];
            z3 = zSwap[1];
            swap = kT;

            BigInteger a = x2.add(z2).mod(P);
            BigInteger aa = a.multiply(a).mod(P);
            BigInteger b = x2.subtract(z2).mod(P);
            BigInteger bb = b.multiply(b).mod(P);
            BigInteger e = aa.subtract(bb).mod(P);
            BigInteger c = x3.add(z3).mod(P);
            BigInteger d = x3.subtract(z3).mod(P);
            BigInteger da = d.multiply(a).mod(P);
            BigInteger cb = c.multiply(b).mod(P);
            BigInteger daPlusCb = da.add(cb).mod(P);
            BigInteger daMinusCb = da.subtract(cb).mod(P);

            x3 = daPlusCb.multiply(daPlusCb).mod(P);
            z3 = x1.multiply(daMinusCb.multiply(daMinusCb).mod(P)).mod(P);
            x2 = aa.multiply(bb).mod(P);
            z2 = e.multiply(aa.add(A24.multiply(e)).mod(P)).mod(P);
        }

        BigInteger[] xSwap = conditionalSwap(swap, x2, x3);
        x2 = xSwap[0];
        BigInteger[] zSwap = conditionalSwap(swap, z2, z3);
        z2 = zSwap[0];

        return encodeLittleEndian(x2.multiply(z2.modInverse(P)).mod(P));
    }

    private static byte[] doAes256Gcm(int mode, byte[] key, byte[] nonce, byte[] aad, byte[] input) {
        try {
            Cipher cipher = Cipher.getInstance(AES_GCM_TRANSFORMATION);
            SecretKeySpec keySpec = new SecretKeySpec(key, AES_ALGORITHM);
            cipher.init(mode, keySpec, createAesGcmParameterSpec(nonce));
            if (aad != null && aad.length > 0) {
                cipher.updateAAD(aad);
            }
            return cipher.doFinal(input);
        } catch (Exception e) {
            throw new IllegalArgumentException("AES-256-GCM operation failed", e);
        }
    }

    private static String sortKey(E2EEInfo info) {
        if (info == null) {
            return "";
        }
        return nullToEmpty(info.getUserId()) + nullToEmpty(info.getDeviceId());
    }

    private static String nullToEmpty(String value) {
        return value == null ? "" : value;
    }

    private static byte[] hmacSha256(byte[] key, byte[] data) throws Exception {
        Mac mac = Mac.getInstance(HMAC_SHA256_ALGORITHM);
        mac.init(new SecretKeySpec(key, HMAC_SHA256_ALGORITHM));
        return mac.doFinal(data);
    }

    private static String base64Encode(byte[] data) {
        if (data == null || data.length == 0) {
            return "";
        }
        StringBuilder encoded = new StringBuilder(((data.length + 2) / 3) * 4);
        for (int i = 0; i < data.length; i += 3) {
            int b0 = data[i] & 0xff;
            int b1 = i + 1 < data.length ? data[i + 1] & 0xff : 0;
            int b2 = i + 2 < data.length ? data[i + 2] & 0xff : 0;

            encoded.append(BASE64_ALPHABET[b0 >>> 2]);
            encoded.append(BASE64_ALPHABET[((b0 & 0x03) << 4) | (b1 >>> 4)]);
            encoded.append(i + 1 < data.length ? BASE64_ALPHABET[((b1 & 0x0f) << 2) | (b2 >>> 6)] : '=');
            encoded.append(i + 2 < data.length ? BASE64_ALPHABET[b2 & 0x3f] : '=');
        }
        return encoded.toString();
    }

    private static AlgorithmParameterSpec createAesGcmParameterSpec(byte[] nonce) {
        try {
            Class<?> gcmParameterSpecClass = Class.forName("javax.crypto.spec.GCMParameterSpec");
            return (AlgorithmParameterSpec) gcmParameterSpecClass
                    .getConstructor(int.class, byte[].class)
                    .newInstance(AES_GCM_TAG_SIZE_BITS, nonce);
        } catch (Exception e) {
            return new IvParameterSpec(nonce);
        }
    }

    private static boolean causedByAeadBadTag(Throwable throwable) {
        while (throwable != null) {
            if (throwable instanceof AEADBadTagException) {
                return true;
            }
            throwable = throwable.getCause();
        }
        return false;
    }

    private static void checkAes256GcmKeyNonceAndTag(byte[] key, byte[] nonce, byte[] tag) {
        if (key == null || key.length != AES_256_GCM_KEY_SIZE) {
            throw new IllegalArgumentException("key must be 32 bytes");
        }
        if (nonce == null || nonce.length != AES_GCM_NONCE_SIZE) {
            throw new IllegalArgumentException("nonce must be 12 bytes");
        }
        if (tag == null || tag.length != AES_GCM_TAG_SIZE) {
            throw new IllegalArgumentException("tag must be 16 bytes");
        }
    }

    private static void checkLength(byte[] value, String name) {
        if (value == null || value.length != X25519_KEY_SIZE) {
            throw new IllegalArgumentException(name + " must be 32 bytes");
        }
    }

    private static void clampScalar(byte[] scalar) {
        scalar[0] &= 248;
        scalar[31] &= 127;
        scalar[31] |= 64;
    }

    private static int bitAt(byte[] littleEndianScalar, int index) {
        return (littleEndianScalar[index >>> 3] >>> (index & 7)) & 1;
    }

    private static BigInteger[] conditionalSwap(int swap, BigInteger a, BigInteger b) {
        if (swap == 0) {
            return new BigInteger[]{a, b};
        }
        return new BigInteger[]{b, a};
    }

    private static BigInteger decodeLittleEndian(byte[] input) {
        byte[] bigEndian = new byte[input.length + 1];
        for (int i = 0; i < input.length; i++) {
            bigEndian[input.length - i] = input[i];
        }
        return new BigInteger(bigEndian).mod(P);
    }

    private static byte[] encodeLittleEndian(BigInteger value) {
        byte[] bigEndian = value.mod(P).toByteArray();
        byte[] littleEndian = new byte[X25519_KEY_SIZE];
        int copyLength = Math.min(X25519_KEY_SIZE, bigEndian.length);
        for (int i = 0; i < copyLength; i++) {
            littleEndian[i] = bigEndian[bigEndian.length - 1 - i];
        }
        return littleEndian;
    }
}
