package com.anli.generalization.user.data.encryption;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import org.apache.commons.codec.binary.Hex;

import static java.security.MessageDigest.getInstance;

public class PasswordHasher {

    private static final String ALGORITHM = "SHA_512";
    private static final byte[] salt = new byte[]{-52, 13, 103, -2};

    public static String hash(String password) {
        if (password == null) {
            return null;
        }
        try {
            MessageDigest digest = getInstance(ALGORITHM);
            digest.update(salt);
            digest.update(password.getBytes());
            byte[] encrypted = digest.digest();
            return Hex.encodeHexString(encrypted);
        } catch (NoSuchAlgorithmException ex) {
            throw new RuntimeException(ex);
        }
    }
}
