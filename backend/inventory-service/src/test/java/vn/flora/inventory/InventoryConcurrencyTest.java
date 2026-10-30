package vn.flora.inventory;

import static org.junit.jupiter.api.Assertions.*;
import java.util.UUID;
import java.util.concurrent.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

@SpringBootTest
@EnabledIfEnvironmentVariable(named="RUN_DB_TESTS", matches="true")
class InventoryConcurrencyTest {
    @Autowired InventoryService inventory;
    @Autowired JdbcTemplate db;

    @Test void concurrentReservationsDoNotOversellAndReleaseIsIdempotent() throws Exception {
        UUID item=UUID.randomUUID(), first=UUID.randomUUID(), second=UUID.randomUUID();
        db.update("INSERT INTO inventory.stock(item_id,on_hand,reserved) VALUES (?,1,0)",item);
        var start=new CountDownLatch(1);
        var pool=Executors.newFixedThreadPool(2);
        try {
            Callable<Boolean> a=() -> { start.await(); try { inventory.reserve(first,item,1); return true; } catch (IllegalStateException ex) { return false; } };
            Callable<Boolean> b=() -> { start.await(); try { inventory.reserve(second,item,1); return true; } catch (IllegalStateException ex) { return false; } };
            var x=pool.submit(a); var y=pool.submit(b); start.countDown();
            boolean one=x.get(10,TimeUnit.SECONDS), two=y.get(10,TimeUnit.SECONDS);
            assertNotEquals(one,two);
            UUID winner=one ? first : second;
            inventory.reserve(winner,item,1);
            assertEquals(1,db.queryForObject("SELECT reserved FROM inventory.stock WHERE item_id=?",Integer.class,item));
            inventory.release(winner,item);
            inventory.release(winner,item);
            assertEquals(0,db.queryForObject("SELECT reserved FROM inventory.stock WHERE item_id=?",Integer.class,item));
            assertEquals(1,db.queryForObject("SELECT on_hand FROM inventory.stock WHERE item_id=?",Integer.class,item));
            UUID next=UUID.randomUUID();
            inventory.reserve(next,item,1);
            inventory.commit(next,item);
            inventory.commit(next,item);
            assertEquals(0,db.queryForObject("SELECT on_hand FROM inventory.stock WHERE item_id=?",Integer.class,item));
            assertEquals(0,db.queryForObject("SELECT reserved FROM inventory.stock WHERE item_id=?",Integer.class,item));
            assertThrows(IllegalStateException.class, () -> inventory.release(next,item));
        } finally {
            pool.shutdownNow();
            db.update("DELETE FROM inventory.stock_movements WHERE item_id=?",item);
            db.update("DELETE FROM inventory.stock_reservations WHERE item_id=?",item);
            db.update("DELETE FROM inventory.stock WHERE item_id=?",item);
        }
    }
}