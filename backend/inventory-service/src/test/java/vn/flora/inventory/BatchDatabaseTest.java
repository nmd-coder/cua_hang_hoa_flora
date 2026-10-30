package vn.flora.inventory;
import static org.junit.jupiter.api.Assertions.*;
import java.util.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
@SpringBootTest
@EnabledIfEnvironmentVariable(named="RUN_DB_TESTS",matches="true")
class BatchDatabaseTest {
 @Autowired BatchService batches;@Autowired JdbcTemplate db;
 @Test void failureRollsBackEntireBatchAndReleasePreventsLateReserve(){
  UUID order=UUID.randomUUID(),a=UUID.randomUUID(),b=UUID.randomUUID();
  db.update("INSERT INTO inventory.stock(item_id,on_hand,reserved) VALUES (?,2,0),(?,0,0)",a,b);
  try{
   assertThrows(IllegalStateException.class,()->batches.reserve(order,List.of(new BatchService.Line(a,1),new BatchService.Line(b,1))));
   assertEquals(0,db.queryForObject("SELECT sum(reserved)::integer FROM inventory.stock WHERE item_id IN (?,?)",Integer.class,a,b));
   db.update("UPDATE inventory.stock SET on_hand=2 WHERE item_id=?",b);
   var lines=List.of(new BatchService.Line(a,1),new BatchService.Line(b,1));
   batches.reserve(order,lines);batches.reserve(order,lines);
   assertEquals(2,db.queryForObject("SELECT sum(reserved)::integer FROM inventory.stock WHERE item_id IN (?,?)",Integer.class,a,b));
   batches.release(order);batches.release(order);
   assertThrows(IllegalStateException.class,()->batches.reserve(order,lines));
   assertEquals(0,db.queryForObject("SELECT sum(reserved)::integer FROM inventory.stock WHERE item_id IN (?,?)",Integer.class,a,b));
  }finally{
   db.update("DELETE FROM inventory.stock_movements WHERE item_id IN (?,?)",a,b);
   db.update("DELETE FROM inventory.stock_reservations WHERE order_id=?",order);
   db.update("DELETE FROM inventory.reservation_batches WHERE order_id=?",order);
   db.update("DELETE FROM inventory.stock WHERE item_id IN (?,?)",a,b);
  }
 }
}