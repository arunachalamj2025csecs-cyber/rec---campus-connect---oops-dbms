package edu.rec.campusconnect.common;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import edu.rec.campusconnect.security.Tokens;
import org.junit.jupiter.api.Test;

class TokensTest {

    @Test
    void sessionTokensAreLongAndUnique() {
        String first = Tokens.newSessionToken();
        String second = Tokens.newSessionToken();
        assertEquals(43, first.length());
        assertNotEquals(first, second);
    }

    @Test
    void sha256MatchesKnownVector() {
        assertEquals("ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad", Tokens.sha256Hex("abc"));
    }

    @Test
    void readableIdsStartWithPrefixAndYear() {
        String id = Tokens.readableId("CMP");
        assertTrue(id.startsWith("CMP-"));
        assertEquals(3 + 1 + 4 + 1 + 8, id.length());
    }

    @Test
    void bearerTokenIsReadFromHeader() {
        assertEquals("abc123", Tokens.bearerToken("Bearer abc123"));
        assertNull(Tokens.bearerToken(null));
        assertNull(Tokens.bearerToken("Basic abc123"));
        assertNull(Tokens.bearerToken("Bearer   "));
    }
}
