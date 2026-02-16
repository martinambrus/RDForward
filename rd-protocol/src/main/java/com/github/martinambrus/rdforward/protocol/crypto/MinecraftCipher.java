package com.github.martinambrus.rdforward.protocol.crypto;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.GeneralSecurityException;

/**
 * Wrapper around AES/CFB8/NoPadding stream cipher used by Minecraft 1.3+.
 *
 * The shared secret (16 bytes) is used as both the AES key and the IV.
 * This produces a stream cipher that encrypts/decrypts one byte at a time
 * (CFB with 8-bit feedback), suitable for wrapping a TCP stream.
 */
public class MinecraftCipher {

    private final Cipher cipher;

    public MinecraftCipher(int mode, byte[] sharedSecret) throws GeneralSecurityException {
        cipher = Cipher.getInstance("AES/CFB8/NoPadding");
        SecretKey key = new SecretKeySpec(sharedSecret, "AES");
        IvParameterSpec iv = new IvParameterSpec(sharedSecret);
        cipher.init(mode, key, iv);
    }

    public byte[] update(byte[] input) {
        return cipher.update(input);
    }

    public byte[] update(byte[] input, int offset, int length) {
        return cipher.update(input, offset, length);
    }
}
