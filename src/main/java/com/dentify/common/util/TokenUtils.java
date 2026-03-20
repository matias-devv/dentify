package com.dentify.common.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

public class TokenUtils {

    public static String sha256Hex(String input) {

        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");

            byte[] hashed = digest.digest( input.getBytes(StandardCharsets.UTF_8) );

            return HexFormat.of().formatHex(hashed);

        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}