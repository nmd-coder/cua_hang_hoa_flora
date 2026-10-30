package vn.flora.order;
import static org.junit.jupiter.api.Assertions.*;
import com.sun.net.httpserver.HttpServer;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.web.server.ResponseStatusException;
@SpringBootTest(properties="flora.recovery.enabled=false")
@EnabledIfEnvironmentVariable(named="RUN_DB_TESTS",matches="true")
class QuoteDatabaseTest {
    static HttpServer server;
    static volatile String response="{\"sku\":\"TEST-SKU\",\"name\":\"Hoa thử nghiệm\",\"price\":125000,\"image\":null}";
    @DynamicPropertySource static void configure(DynamicPropertyRegistry properties) throws Exception {
        server=HttpServer.create(new InetSocketAddress("127.0.0.1",0),0);
        server.createContext("/api/products",exchange->{
            byte[] bytes=response.getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(200,bytes.length);exchange.getResponseBody().write(bytes);exchange.close();
        });
        server.start();
        properties.add("CATALOG_URL",()->"http://127.0.0.1:"+server.getAddress().getPort());
    }
    @AfterAll static void stop(){if(server!=null)server.stop(0);}
    @Autowired QuoteService quotes;
    @Autowired JdbcTemplate db;
    @Test void totalsSnapshotsOwnershipAndExpiryAreEnforced() {
        UUID user=UUID.randomUUID(), product=UUID.randomUUID();
        var request=new QuoteService.Request(List.of(new QuoteService.Item(product,2)),"Khách mẫu","0900000000","Địa chỉ thử nghiệm",
            LocalDate.now(ZoneId.of("Asia/Ho_Chi_Minh")).plusDays(1),"09:00–12:00","Lời nhắn");
        try {
            var quote=quotes.create(user,request);
            assertEquals(new BigDecimal("250000.00"),quote.subtotal());
            assertEquals(new BigDecimal("280000.00"),quote.total());
            assertEquals("MOCK",quote.shippingMode());
            response="{\"sku\":\"TEST-SKU\",\"name\":\"Tên mới\",\"price\":999999}";
            assertEquals("Hoa thử nghiệm",quotes.get(user,quote.id()).items().get(0).name());
            assertEquals(new BigDecimal("280000.00"),quotes.get(user,quote.id()).total());
            assertEquals(404,assertThrows(ResponseStatusException.class,()->quotes.get(UUID.randomUUID(),quote.id())).getStatusCode().value());
            db.update("UPDATE \"order\".checkout_quotes SET expires_at=now()-interval '1 second' WHERE id=?",quote.id());
            assertEquals(404,assertThrows(ResponseStatusException.class,()->quotes.get(user,quote.id())).getStatusCode().value());
            assertThrows(ResponseStatusException.class,()->quotes.create(user,new QuoteService.Request(List.of(),"x","0900000000","address",request.deliveryDate(),"morning","")));
        } finally {db.update("DELETE FROM \"order\".checkout_quotes WHERE user_id=?",user);}
    }
}