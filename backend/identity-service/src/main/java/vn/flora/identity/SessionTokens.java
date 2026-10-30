package vn.flora.identity;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.HexFormat;

final class SessionTokens {
    private static final SecureRandom RANDOM = new SecureRandom();
    private SessionTokens() {}
    static String generate() {
        byte[] bytes = new byte[32];
        RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
    static String hash(String token) {
        try { return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(token.getBytes(StandardCharsets.US_ASCII))); }
        catch (NoSuchAlgorithmException ex) { throw new IllegalStateException(ex); }
    }
    static String bearer(String value) {
        if (value == null || !value.startsWith("Bearer ")) return null;
        String token = value.substring(7);
        return token.matches("[A-Za-z0-9_-]{43}") ? token : null;
    }
}