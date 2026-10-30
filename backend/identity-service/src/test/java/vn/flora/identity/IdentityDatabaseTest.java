package vn.flora.identity;
import static org.junit.jupiter.api.Assertions.*;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.server.ResponseStatusException;

@SpringBootTest
@EnabledIfEnvironmentVariable(named="RUN_DB_TESTS",matches="true")
class IdentityDatabaseTest {
    @Autowired AuthService auth;
    @Autowired JdbcTemplate db;
    @Test void registersAuthenticatesAndRevokesSession() {
        String email=UUID.randomUUID()+"@example.test";
        UUID id=null;
        try {
            auth.register(email,"local-test-password");
            assertThrows(ResponseStatusException.class, () -> auth.register(email,"local-test-password"));
            assertThrows(ResponseStatusException.class, () -> auth.login(email,"incorrect-password"));
            var login=auth.login(email,"local-test-password");
            var profile=auth.profile("Bearer "+login.accessToken());
            id=profile.id();
            assertEquals(email,profile.email());
            assertEquals(java.util.List.of("CUSTOMER"),profile.roles());
            assertEquals(0,db.queryForObject("SELECT count(*) FROM identity.sessions WHERE token_hash=?",Integer.class,login.accessToken()));
            auth.logout("Bearer "+login.accessToken());
            auth.logout("Bearer "+login.accessToken());
            assertThrows(ResponseStatusException.class, () -> auth.profile("Bearer "+login.accessToken()));
        } finally {
            if (id == null) {
                var ids=db.queryForList("SELECT id FROM identity.users WHERE email=?", UUID.class,email);
                if (!ids.isEmpty()) id=ids.get(0);
            }
            if (id != null) {
                db.update("DELETE FROM identity.sessions WHERE user_id=?",id);
                db.update("DELETE FROM identity.user_roles WHERE user_id=?",id);
                db.update("DELETE FROM identity.credentials WHERE user_id=?",id);
                db.update("DELETE FROM identity.users WHERE id=?",id);
            }
        }
    }
}