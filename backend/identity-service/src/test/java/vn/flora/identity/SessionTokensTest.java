package vn.flora.identity;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
class SessionTokensTest {
    @Test void tokensAreRandomAndHaveStableHash() {
        String first = SessionTokens.generate();
        assertEquals(43, first.length());
        assertNotEquals(first, SessionTokens.generate());
        assertEquals(SessionTokens.hash(first), SessionTokens.hash(first));
        assertEquals(64, SessionTokens.hash(first).length());
        assertNotEquals(first, SessionTokens.hash(first));
    }
    @Test void acceptsOnlyValidBearerFormat() {
        String token = SessionTokens.generate();
        assertEquals(token, SessionTokens.bearer("Bearer " + token));
        assertNull(SessionTokens.bearer(null));
        assertNull(SessionTokens.bearer("Bearer invalid"));
        assertNull(SessionTokens.bearer("Basic " + token));
    }
    @Test void rejectsInvalidCredentialsAndNormalizesEmail() {
        assertEquals("a@example.com", AuthService.email(" A@Example.com "));
        assertThrows(RuntimeException.class, () -> AuthService.email("not-an-email"));
        assertThrows(RuntimeException.class, () -> AuthService.validatePassword("short"));
        assertThrows(RuntimeException.class, () -> AuthService.validatePassword("x".repeat(73)));
        assertDoesNotThrow(() -> AuthService.validatePassword("valid-password"));
    }
}