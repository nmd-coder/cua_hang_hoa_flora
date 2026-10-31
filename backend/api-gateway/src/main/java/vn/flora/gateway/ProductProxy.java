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
import org.springframework.web.util.UriComponentsBuilder;

@RestController
class ProductProxy {
    private final RestClient client;

    ProductProxy(@Value("${CATALOG_URL:http://localhost:8081}") String url) {
        var factory = new JdkClientHttpRequestFactory(HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(3)).build());
        factory.setReadTimeout(Duration.ofSeconds(8));
        client = RestClient.builder().baseUrl(url).requestFactory(factory).build();
    }

    @GetMapping(value = {"/api/products", "/api/products/{id}"}, produces = "application/json")
    ResponseEntity<String> products(
            @PathVariable(required = false) String id,
            @RequestParam(defaultValue = "") String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size,
            jakarta.servlet.http.HttpServletRequest request) {
        if (id != null && !id.matches("^[a-zA-Z0-9_-]{1,120}$")) {
            return ResponseEntity.badRequest().body("{\"code\":\"INVALID_ID\"}");
        }
        String path;
        if (id != null) {
            path = "/api/products/" + id;
        } else if (request != null && request.getQueryString() != null) {
            path = "/api/products?" + request.getQueryString();
        } else {
            path = UriComponentsBuilder.fromPath("/api/products")
                    .queryParam("q", q)
                    .queryParam("page", page)
                    .queryParam("size", size)
                    .build().encode().toUriString();
        }
        try {
            return client.get().uri(path).exchange((req, res) ->
                ResponseEntity.status(res.getStatusCode())
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(new String(res.getBody().readAllBytes(), StandardCharsets.UTF_8)));
        } catch (RestClientException | java.util.concurrent.CancellationException ex) {
            return ResponseEntity.status(503).body("{\"code\":\"CATALOG_UNAVAILABLE\",\"message\":\"Danh mục tạm thời không khả dụng\"}");
        }
    }

    public ResponseEntity<String> products(String id, String q, int page, int size) {
        return products(id, q, page, size, null);
    }

    @RequestMapping(value = {
        "/api/categories", "/api/categories/**",
        "/api/occasions", "/api/occasions/**",
        "/api/flower-types", "/api/flower-types/**",
        "/api/colors", "/api/colors/**",
        "/api/admin/products", "/api/admin/products/**"
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
            return ResponseEntity.status(503).body("{\"code\":\"CATALOG_UNAVAILABLE\",\"message\":\"Dịch vụ danh mục tạm thời không khả dụng\"}");
        }
    }
}
