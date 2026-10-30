package vn.flora.gateway;
import static org.junit.jupiter.api.Assertions.*;
import com.sun.net.httpserver.HttpServer;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;
class AuthProxyTest {
    @Test void forwardsAuthorizationAndDoesNotCacheResponse() throws Exception {
        var server=HttpServer.create(new InetSocketAddress("127.0.0.1",0),0);
        server.createContext("/api/auth/me", exchange -> {
            String token=exchange.getRequestHeaders().getFirst("Authorization");
            byte[] body=(token != null && token.equals("Bearer test-token") ? "{\"ok\":true}" : "{}").getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(200,body.length);
            exchange.getResponseBody().write(body);
            exchange.close();
        });
        server.start();
        try {
            var proxy=new AuthProxy("http://127.0.0.1:"+server.getAddress().getPort());
            var result=proxy.me("Bearer test-token");
            assertEquals(200,result.getStatusCode().value());
            assertEquals("{\"ok\":true}",result.getBody());
            assertEquals("no-store",result.getHeaders().getCacheControl());
        } finally { server.stop(0); }
    }
    @Test void reportsIdentityUnavailable() {
        assertEquals(503,new AuthProxy("http://127.0.0.1:1").me(null).getStatusCode().value());
    }
}