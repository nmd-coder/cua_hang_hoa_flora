package vn.flora.gateway;
import static org.junit.jupiter.api.Assertions.*;
import com.sun.net.httpserver.HttpServer;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;
class ProductProxyTest {
    @Test void rejectsInvalidId() {
        var proxy = new ProductProxy("http://127.0.0.1:1");
        assertEquals(400, proxy.products("bad", "", 0, 12).getStatusCode().value());
    }
    @Test void forwardsCatalogResponse() throws Exception {
        var server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/api/products", exchange -> {
            byte[] body = "[]".getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type","application/json");
            exchange.sendResponseHeaders(200,body.length);
            exchange.getResponseBody().write(body);
            exchange.close();
        });
        server.start();
        try {
            var response = new ProductProxy("http://127.0.0.1:" + server.getAddress().getPort()).products(null, "hoa", 0, 12);
            assertEquals(200, response.getStatusCode().value());
            assertEquals("[]", response.getBody());
        } finally { server.stop(0); }
    }
    @Test void reportsUnavailableCatalog() {
        var response = new ProductProxy("http://127.0.0.1:1").products(null,"",0,12);
        assertEquals(503,response.getStatusCode().value());
    }
}