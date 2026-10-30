package vn.flora.order;
import java.net.http.HttpClient;
import java.time.Duration;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.server.ResponseStatusException;

@Component
public class IdentityClient {
    private final RestClient client;
    IdentityClient(@Value("${IDENTITY_URL:http://localhost:8082}") String url) {
        var factory=new JdkClientHttpRequestFactory(HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(3)).build());
        factory.setReadTimeout(Duration.ofSeconds(5));
        client=RestClient.builder().baseUrl(url).requestFactory(factory).build();
    }
    public record Actor(UUID id, java.util.Set<String> roles) {}
    public UUID requireUser(String authorization) { return requireActor(authorization).id(); }
    public Actor requireActor(String authorization) {
        if (authorization == null || !authorization.matches("Bearer [A-Za-z0-9_-]{43}"))
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,"Cần đăng nhập");
        try {
            return client.get().uri("/api/auth/me").header("Authorization",authorization)
                .exchange((req,res)-> {
                    if (res.getStatusCode().value()==401) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,"Phiên đăng nhập không hợp lệ");
                    if (!res.getStatusCode().is2xxSuccessful()) throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,"Không thể xác thực");
                    var profile = new com.fasterxml.jackson.databind.ObjectMapper().readTree(res.getBody());
                    UUID id=UUID.fromString(profile.path("id").asText());
                    var roles=new java.util.HashSet<String>();
                    if(profile.path("roles").isArray())profile.path("roles").forEach(role->{if(role.isTextual())roles.add(role.asText());});
                    return new Actor(id,java.util.Set.copyOf(roles));
                });
        } catch (ResponseStatusException ex) { throw ex; }
        catch (RestClientException | IllegalArgumentException | java.util.concurrent.CancellationException ex) { throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,"Không thể xác thực"); }
    }
}