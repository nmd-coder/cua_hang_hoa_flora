package vn.flora.gateway;

import java.net.http.HttpClient;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@RestController
class AuthProxy {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(AuthProxy.class);
    private final RestClient client;
    AuthProxy(@Value("${IDENTITY_URL:http://localhost:8082}") String base) {
        var factory=new JdkClientHttpRequestFactory(HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(8)).build());
        factory.setReadTimeout(Duration.ofSeconds(15));
        client=RestClient.builder().baseUrl(base).requestFactory(factory).build();
    }
    @PostMapping(value={"/api/auth/register","/api/auth/login"}, consumes="application/json")
    ResponseEntity<String> credentials(jakarta.servlet.http.HttpServletRequest request, @RequestBody String body) {
        if (body.length()>4096) return ResponseEntity.badRequest().body("{\"code\":\"INVALID_REQUEST\"}");
        try {
            return client.post().uri(request.getRequestURI()).contentType(MediaType.APPLICATION_JSON).body(body)
                .exchange((req,res)->response(res));
        } catch (Exception ex) {
            log.error("AuthProxy credentials error for {}: {}", request.getRequestURI(), ex.getMessage());
            return unavailable();
        }
    }
    @GetMapping("/api/auth/me")
    ResponseEntity<String> me(@RequestHeader(value="Authorization",required=false) String auth) {
        try {
            return client.get().uri("/api/auth/me").headers(h->{ if(auth!=null) h.set("Authorization",auth); })
                .exchange((req,res)->response(res));
        } catch (Exception ex) {
            log.error("AuthProxy me error: {}", ex.getMessage());
            return unavailable();
        }
    }
    @PostMapping("/api/auth/logout")
    ResponseEntity<String> logout(@RequestHeader(value="Authorization",required=false) String auth) {
        try {
            return client.post().uri("/api/auth/logout").headers(h->{ if(auth!=null) h.set("Authorization",auth); })
                .exchange((req,res)->response(res));
        } catch (Exception ex) {
            log.error("AuthProxy logout error: {}", ex.getMessage());
            return unavailable();
        }
    }
    private static ResponseEntity<String> response(org.springframework.http.client.ClientHttpResponse res) throws java.io.IOException {
        return ResponseEntity.status(res.getStatusCode()).header("Cache-Control","no-store")
            .contentType(MediaType.APPLICATION_JSON)
            .body(new String(res.getBody().readAllBytes(),StandardCharsets.UTF_8));
    }
    private static ResponseEntity<String> unavailable() {
        return ResponseEntity.status(503).header("Cache-Control","no-store")
            .body("{\"code\":\"IDENTITY_UNAVAILABLE\"}");
    }
}