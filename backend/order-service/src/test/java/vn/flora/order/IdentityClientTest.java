package vn.flora.order;
import static org.junit.jupiter.api.Assertions.*;
import com.sun.net.httpserver.HttpServer;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;
class IdentityClientTest {
    @Test void validatesSessionThroughIdentity() throws Exception {
        var expected=UUID.randomUUID();
        var server=HttpServer.create(new InetSocketAddress("127.0.0.1",0),0);
        server.createContext("/api/auth/me",exchange-> {
            var auth=exchange.getRequestHeaders().getFirst("Authorization");
            boolean valid=("Bearer "+"a".repeat(43)).equals(auth);
            byte[] body=(valid ? "{\"id\":\""+expected+"\",\"roles\":[\"STAFF\"]}" : "{}").getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(valid?200:401,body.length);
            exchange.getResponseBody().write(body); exchange.close();
        });
        server.start();
        try {
            var client=new IdentityClient("http://127.0.0.1:"+server.getAddress().getPort());
            assertEquals(expected,client.requireUser("Bearer "+"a".repeat(43)));
            assertEquals(java.util.Set.of("STAFF"),client.requireActor("Bearer "+"a".repeat(43)).roles());
            assertEquals(401,assertThrows(ResponseStatusException.class,()->client.requireUser("Bearer "+"b".repeat(43))).getStatusCode().value());
            assertEquals(401,assertThrows(ResponseStatusException.class,()->client.requireUser(null)).getStatusCode().value());
        } finally {server.stop(0);}
    }
    @Test void rejectsAccessWhenIdentityIsUnavailable() {
        var client=new IdentityClient("http://127.0.0.1:1");
        assertEquals(503,assertThrows(ResponseStatusException.class,()->client.requireUser("Bearer "+"a".repeat(43))).getStatusCode().value());
    }
}