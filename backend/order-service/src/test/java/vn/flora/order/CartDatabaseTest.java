package vn.flora.order;
import static org.junit.jupiter.api.Assertions.*;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
@SpringBootTest(properties="flora.recovery.enabled=false")
@EnabledIfEnvironmentVariable(named="RUN_DB_TESTS",matches="true")
class CartDatabaseTest {
    @Autowired CartService carts;
    @Autowired JdbcTemplate db;
    @Test void cartsAreIsolatedAndSetIsIdempotent() {
        UUID a=UUID.randomUUID(),b=UUID.randomUUID(),item=UUID.randomUUID();
        try {
            carts.set(a,item,2); carts.set(a,item,2); carts.set(b,item,3);
            assertEquals(1,carts.list(a).size());
            assertEquals(2,((Number)carts.list(a).get(0).get("quantity")).intValue());
            assertThrows(IllegalArgumentException.class,()->carts.set(a,item,0));
            carts.remove(a,item); carts.remove(a,item);
            assertTrue(carts.list(a).isEmpty()); assertEquals(1,carts.list(b).size());
        } finally {
            db.update("DELETE FROM \"order\".cart_items WHERE cart_id IN (SELECT id FROM \"order\".carts WHERE user_id IN (?,?))",a,b);
            db.update("DELETE FROM \"order\".carts WHERE user_id IN (?,?)",a,b);
        }
    }
}