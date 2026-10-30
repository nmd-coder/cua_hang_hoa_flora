package vn.flora.order;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
@Service
public class OrderTransitionService {
 private final JdbcTemplate db;
 OrderTransitionService(JdbcTemplate db){this.db=db;}
 @Transactional public void transition(IdentityClient.Actor actor,UUID id,String target){
  var rows=db.queryForList("SELECT user_id,status FROM \"order\".orders WHERE id=? FOR UPDATE",id);
  if(rows.isEmpty())throw new ResponseStatusException(HttpStatus.NOT_FOUND,"Không tìm thấy đơn");
  var row=rows.get(0);String current=(String)row.get("status");boolean owner=actor.id().equals(row.get("user_id"));
  OrderTransitions.verify(current,target,owner,actor.roles());
  db.update("UPDATE \"order\".orders SET status=? WHERE id=?",target,id);
  db.update("INSERT INTO \"order\".order_status_history(id,order_id,from_status,to_status,actor_id,reason) VALUES (?,?,?,?,?,?)",
   UUID.randomUUID(),id,current,target,actor.id(),"PROCESSING".equals(target)?"Nhân viên tiếp nhận":"Khách xác nhận đã nhận");
 }
}