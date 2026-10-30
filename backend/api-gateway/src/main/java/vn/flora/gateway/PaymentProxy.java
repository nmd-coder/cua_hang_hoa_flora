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
class PaymentProxy {
    private final RestClient client;

    PaymentProxy(@Value("${PAYMENT_URL:http://localhost:8085}") String url) {
        var factory = new JdkClientHttpRequestFactory(HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(3)).build());
        factory.setReadTimeout(Duration.ofSeconds(8));
        client = RestClient.builder().baseUrl(url).requestFactory(factory).build();
    }

    @RequestMapping(value = {
        "/api/payments", "/api/payments/**"
    }, method = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.PATCH, RequestMethod.DELETE})
    ResponseEntity<String> forward(
            jakarta.servlet.http.HttpServletRequest request,
            @RequestHeader(value = "Authorization", required = false) String auth,
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
            return ResponseEntity.status(503).body("{\"code\":\"PAYMENT_UNAVAILABLE\",\"message\":\"Dịch vụ thanh toán tạm thời không khả dụng\"}");
        }
    }
}

