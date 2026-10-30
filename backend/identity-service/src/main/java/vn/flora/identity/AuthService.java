package vn.flora.identity;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AuthService {
    private final JdbcTemplate db;
    private final PasswordEncoder passwords;
    private final String dummyHash;
    AuthService(JdbcTemplate db, PasswordEncoder passwords) {
        this.db = db;
        this.passwords = passwords;
        this.dummyHash = passwords.encode("dummy-password-not-for-authentication");
    }

    static String email(String raw) {
        if (raw == null) throw invalid();
        String value = raw.trim().toLowerCase(Locale.ROOT);
        if (value.length() > 254 || !value.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) throw invalid();
        return value;
    }
    static void validatePassword(String value) {
        if (value == null || value.length() < 10 || value.getBytes(StandardCharsets.UTF_8).length > 72) throw invalid();
    }
    private static ResponseStatusException invalid() {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email hoặc mật khẩu không hợp lệ");
    }

    @Transactional
    public void register(String rawEmail, String password) {
        register(rawEmail, password, null);
    }

    @Transactional
    public void register(String rawEmail, String password, String phone) {
        String normalized = email(rawEmail);
        validatePassword(password);
        UUID id = UUID.randomUUID();
        try {
            db.update("INSERT INTO identity.users(id,email,phone,status) VALUES (?,?,?, 'ACTIVE')", id, normalized, phone);
            db.update("INSERT INTO identity.credentials(user_id,password_hash) VALUES (?,?)", id, passwords.encode(password));
            db.update("INSERT INTO identity.user_roles(user_id,role) VALUES (?, 'CUSTOMER')", id);
        } catch (DataIntegrityViolationException ex) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Không thể đăng ký tài khoản");
        }
    }

    @Transactional
    public Login login(String rawEmail, String password) {
        String normalized = email(rawEmail);
        validatePassword(password);
        List<Map<String,Object>> rows = db.queryForList(
            "SELECT u.id,u.status,c.password_hash FROM identity.users u JOIN identity.credentials c ON c.user_id=u.id WHERE u.email=?", normalized);
        String hash = rows.isEmpty() ? dummyHash : (String) rows.get(0).get("password_hash");
        boolean valid = passwords.matches(password, hash);
        if (rows.isEmpty() || !valid || !"ACTIVE".equals(rows.get(0).get("status")))
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Thông tin đăng nhập không đúng");
        UUID userId = (UUID) rows.get(0).get("id");
        String token = SessionTokens.generate();
        Instant expires = Instant.now().plus(12, ChronoUnit.HOURS);
        db.update("INSERT INTO identity.sessions(token_hash,user_id,expires_at) VALUES (?,?,?)",
            SessionTokens.hash(token), userId, java.sql.Timestamp.from(expires));
        return new Login(token, expires);
    }

    public Profile profile(String authorization) {
        String token = SessionTokens.bearer(authorization);
        if (token == null) throw unauthorized();
        List<Map<String,Object>> users = db.queryForList("""
            SELECT u.id,u.email FROM identity.sessions s JOIN identity.users u ON u.id=s.user_id
            WHERE s.token_hash=? AND s.revoked_at IS NULL AND s.expires_at > now() AND u.status='ACTIVE'
            """, SessionTokens.hash(token));
        if (users.isEmpty()) throw unauthorized();
        UUID id = (UUID)users.get(0).get("id");
        List<String> roles = db.queryForList("SELECT role FROM identity.user_roles WHERE user_id=? ORDER BY role", String.class, id);
        return new Profile(id, (String)users.get(0).get("email"), roles);
    }

    public void logout(String authorization) {
        String token = SessionTokens.bearer(authorization);
        if (token == null) throw unauthorized();
        db.update("UPDATE identity.sessions SET revoked_at=now() WHERE token_hash=? AND revoked_at IS NULL", SessionTokens.hash(token));
    }
    private static ResponseStatusException unauthorized() { return new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Phiên đăng nhập không hợp lệ"); }
    public record Login(String accessToken, Instant expiresAt) {}
    public record Profile(UUID id, String email, List<String> roles) {}
}