package vn.flora.order;
import static org.junit.jupiter.api.Assertions.*;
import com.sun.net.httpserver.HttpServer;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.time.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
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
class CodDatabaseTest {
    static HttpServer server;static AtomicInteger reserves=new AtomicInteger();static volatile int inventoryStatus=204;
    @DynamicPropertySource static void config(DynamicPropertyRegistry p)throws Exception{
        server=HttpServer.create(new InetSocketAddress("127.0.0.1",0),0);
        server.createContext("/api/products",e->{byte[] b="{\"sku\":\"TEST-SKU\",\"name\":\"Hoa mẫu\",\"price\":100000}".getBytes(StandardCharsets.UTF_8);e.sendResponseHeaders(200,b.length);e.getResponseBody().write(b);e.close();});
        server.createContext("/internal/reservations",e->{reserves.incrementAndGet();e.getRequestBody().readAllBytes();e.sendResponseHeaders(inventoryStatus,-1);e.close();});server.start();
        p.add("CATALOG_URL",()->"http://127.0.0.1:"+server.getAddress().getPort());
        p.add("INVENTORY_URL",()->"http://127.0.0.1:"+server.getAddress().getPort());
        p.add("INTERNAL_API_KEY",()->"test-internal-key-not-for-real-use-123456");
    }
    @AfterAll static void stop(){if(server!=null)server.stop(0);}
    @Autowired QuoteService quotes;@Autowired CodCheckoutService orders;@Autowired CancelService cancellations;@Autowired JdbcTemplate db;
    UUID user;
    @BeforeEach void setup(){user=UUID.randomUUID();reserves.set(0);inventoryStatus=204;}
    QuoteService.Quote quote(){return quotes.create(user,new QuoteService.Request(List.of(new QuoteService.Item(UUID.randomUUID(),2)),"Người nhận mẫu","0900000000","Địa chỉ mẫu",LocalDate.now(ZoneId.of("Asia/Ho_Chi_Minh")).plusDays(1),"09-12","Thiệp"));}
    @AfterEach void cleanup(){
        db.update("DELETE FROM \"order\".checkout_requests WHERE user_id=?",user);
        db.update("DELETE FROM \"order\".order_status_history WHERE order_id IN (SELECT id FROM \"order\".orders WHERE user_id=?)",user);
        db.update("DELETE FROM \"order\".order_items WHERE order_id IN (SELECT id FROM \"order\".orders WHERE user_id=?)",user);
        db.update("DELETE FROM \"order\".orders WHERE user_id=?",user);
        db.update("DELETE FROM \"order\".checkout_intents WHERE user_id=?",user);
        db.update("DELETE FROM \"order\".checkout_quotes WHERE user_id=?",user);
    }
    @Test void concurrentRetryCreatesOneOrderAndPreservesSnapshot()throws Exception{
        var quote=quote();var pool=Executors.newFixedThreadPool(2);
        try{
            var a=pool.submit(()->orders.place(user,quote.id(),"concurrent-test-key"));
            var b=pool.submit(()->orders.place(user,quote.id(),"concurrent-test-key"));
            var first=a.get(15,TimeUnit.SECONDS);var second=b.get(15,TimeUnit.SECONDS);
            assertEquals(first.id(),second.id());assertEquals("PENDING",first.status());assertEquals(1,reserves.get());
            assertEquals(1,orders.list(user).size());
            assertEquals(404,assertThrows(ResponseStatusException.class,()->orders.detail(UUID.randomUUID(),first.id())).getStatusCode().value());
            db.update("UPDATE \"order\".checkout_quotes SET expires_at=now()-interval '1 hour',snapshot='{}'::jsonb WHERE id=?",quote.id());
            assertEquals(first.id(),orders.place(user,quote.id(),"concurrent-test-key").id());
            assertEquals("Người nhận mẫu",orders.detail(user,first.id()).get("recipient_name"));
            assertThrows(ResponseStatusException.class,()->orders.place(user,UUID.randomUUID(),"concurrent-test-key"));
        }finally{pool.shutdownNow();}
    }
    @Test void definitiveStockFailureDoesNotCreateOrder(){
        inventoryStatus=409;var quote=quote();
        assertEquals(409,assertThrows(ResponseStatusException.class,()->orders.place(user,quote.id(),"stock-failure-key")).getStatusCode().value());
        assertTrue(orders.list(user).isEmpty());
    }
    @Test void cancellationRespectsOwnerRetriesAndRecordsHistoryOnce(){
        var q=quote();var order=orders.place(user,q.id(),"cancellation-test-key");
        assertEquals(404,assertThrows(ResponseStatusException.class,()->cancellations.cancel(UUID.randomUUID(),order.id())).getStatusCode().value());
        inventoryStatus=503;
        assertEquals(503,assertThrows(ResponseStatusException.class,()->cancellations.cancel(user,order.id())).getStatusCode().value());
        assertEquals("CANCEL_PENDING",orders.detail(user,order.id()).get("status"));
        inventoryStatus=204;
        assertEquals("CANCELLED",cancellations.cancel(user,order.id()));
        assertEquals("CANCELLED",orders.place(user,q.id(),"cancellation-test-key").status());
        assertEquals("CANCELLED",cancellations.cancel(user,order.id()));
        assertEquals("CANCELLED",orders.place(user,q.id(),"cancellation-test-key").status());
        assertEquals(1,db.queryForObject("SELECT count(*) FROM \"order\".order_status_history WHERE order_id=? AND to_status='CANCELLED'",Integer.class,order.id()));
    }
    @Test void transientFailureRemainsRetryable(){
        inventoryStatus=503;var quote=quote();
        assertEquals(503,assertThrows(ResponseStatusException.class,()->orders.place(user,quote.id(),"transient-test-key")).getStatusCode().value());
        assertTrue(orders.list(user).isEmpty());inventoryStatus=204;
        assertEquals("PENDING",orders.place(user,quote.id(),"transient-test-key").status());
    }
}