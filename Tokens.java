package edu.rec.campusconnect.security;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Year;
import java.util.Base64;
import java.util.HexFormat;

/**
 * Random values used by the app: session tokens, file IDs and readable record IDs.
 */
public final class Tokens {

    private static final SecureRandom RANDOM = new SecureRandom();

    private Tokens() {
    }

    /** A 256-bit random session token. The client keeps it; the server stores only its hash. */
    public static String newSessionToken() {
        return randomUrlSafe(32);
    }

    /** An unguessable ID for uploaded files (poster and profile photo links). */
    public static String newFileId() {
        return randomUrlSafe(18);
    }

    /** Readable record IDs such as CMP-2026-1A2B3C4D (prefix, year, 8 random hex digits). */
    public static String readableId(String prefix) {
        return prefix + "-" + Year.now().getValue() + "-" + String.format("%08X", RANDOM.nextInt());
    }

    /** SHA-256 hash of a value, written as 64 hex characters. Used to store session tokens safely. */
    public static String sha256Hex(String value) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 is not available", ex);
        }
    }

    /**
     * Reads the token from a header such as "Authorization: Bearer abc123".
     * Returns null when the header is missing or has the wrong shape.
     */
    public static String bearerToken(String authorizationHeader) {
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            return null;
        }
        String token = authorizationHeader.substring(7).trim();
        return token.isEmpty() ? null : token;
    }

    static String randomUrlSafe(int bytes) {
        byte[] data = new byte[bytes];
        RANDOM.nextBytes(data);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(data);
    }
}
