package vn.flora.order;
import java.util.UUID;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
@Component
@ConditionalOnProperty(name="flora.recovery.enabled",havingValue="true",matchIfMissing=true)
class CodRecovery {
 private final JdbcTemplate db;private final CodCheckoutService orders;private final CancelService cancellations;
 CodRecovery(JdbcTemplate db,CodCheckoutService orders,CancelService cancellations){this.db=db;this.orders=orders;this.cancellations=cancellations;}
 @Scheduled(fixedDelay=30000,initialDelay=30000)
 public void retry(){
  var cancelled=db.queryForList("SELECT id,user_id FROM \"order\".orders WHERE status='CANCEL_PENDING' ORDER BY created_at LIMIT 10");
  for(var row:cancelled)try{cancellations.cancel((UUID)row.get("user_id"),(UUID)row.get("id"));}
  catch(org.springframework.web.server.ResponseStatusException ignored){}
  var pending=db.queryForList("SELECT user_id,quote_id,idempotency_key FROM \"order\".checkout_intents WHERE status='PREPARING' ORDER BY created_at LIMIT 10");
  for(var row:pending)try{
   orders.place((UUID)row.get("user_id"),(UUID)row.get("quote_id"),(String)row.get("idempotency_key"));
  }catch(org.springframework.web.server.ResponseStatusException ignored){
   // The durable intent remains available for the next retry; no secrets or recipient data are logged.
  }
 }
}