package vn.flora.identity;

import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService auth;
    AuthController(AuthService auth) { this.auth = auth; }
    public record Credentials(String email, String password, String phone) {}
    @PostMapping("/register") ResponseEntity<Void> register(@RequestBody Credentials body) {
        auth.register(body.email(), body.password(), body.phone());
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
    @PostMapping("/login") ResponseEntity<AuthService.Login> login(@RequestBody Credentials body) {
        return ResponseEntity.ok().header("Cache-Control", "no-store").body(auth.login(body.email(), body.password()));
    }
    @GetMapping("/me") ResponseEntity<AuthService.Profile> me(@RequestHeader(value="Authorization",required=false) String authorization) {
        return ResponseEntity.ok().header("Cache-Control", "no-store").body(auth.profile(authorization));
    }
    @PostMapping("/logout") ResponseEntity<Void> logout(@RequestHeader(value="Authorization",required=false) String authorization) {
        auth.logout(authorization);
        return ResponseEntity.noContent().header("Cache-Control", "no-store").build();
    }
}