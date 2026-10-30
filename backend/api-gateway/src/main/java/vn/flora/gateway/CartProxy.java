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
class CartProxy {
    private final RestClient client;

    CartProxy(@Value("${ORDER_URL:http://localhost:8083}") String url) {
        var factory = new JdkClientHttpRequestFactory(HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(3)).build());
        factory.setReadTimeout(Duration.ofSeconds(10));
        client = RestClient.builder().baseUrl(url).requestFactory(factory).build();
    }

    @RequestMapping(value = {
        "/api/cart", "/api/cart/**",
        "/api/checkout/**",
        "/api/orders", "/api/orders/**",
        "/api/vouchers/**",
        "/api/admin/orders", "/api/admin/orders/**",
        "/api/admin/dashboard", "/api/admin/dashboard/**",
        "/api/admin/vouchers", "/api/admin/vouchers/**"
    }, method = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.PATCH, RequestMethod.DELETE})
    ResponseEntity<String> forward(
            jakarta.servlet.http.HttpServletRequest request,
            @RequestHeader(value = "Authorization", required = false) String auth,
            @RequestHeader(value = "Idempotency-Key", required = false) String key,
            @RequestBody(required = false) String body) {

        String path = request.getRequestURI();
        if (request.getQueryString() != null) {
            path += "?" + request.getQueryString();
        }

        try {
            var spec = client.method(org.springframework.http.HttpMethod.valueOf(request.getMethod()))
                .uri(path)
                .headers(h -> {
                    if (auth != null) h.set("Authorization", auth);
                    if (key != null) h.set("Idempotency-Key", key);
                    if (request.getContentType() != null) h.set("Content-Type", request.getContentType());
                });

            if (body != null && !body.isEmpty()) {
                spec.body(body);
            }

            return spec.exchange((req, res) ->
                ResponseEntity.status(res.getStatusCode())
                    .header("Cache-Control", "no-store")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(new String(res.getBody().readAllBytes(), StandardCharsets.UTF_8))
            );
        } catch (RestClientException | java.util.concurrent.CancellationException ex) {
            return ResponseEntity.status(503).body("{\"code\":\"ORDER_UNAVAILABLE\",\"message\":\"Dịch vụ đơn hàng tạm thời không khả dụng\"}");
        }
    }
}
