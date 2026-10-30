package vn.flora.inventory;
import java.util.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BatchService {
    public record Line(UUID itemId,int quantity) {}
    private final JdbcTemplate db;
    private final InventoryService stock;
    BatchService(JdbcTemplate db,InventoryService stock){this.db=db;this.stock=stock;}
    @Transactional public void reserve(UUID order,List<Line> input) {
        if(order==null || input==null || input.isEmpty() || input.size()>30)throw new IllegalArgumentException("Invalid items");
        var lines=new ArrayList<>(input);
        var seen=new HashSet<UUID>();
        for(var line:lines)if(line==null || line.itemId()==null || line.quantity()<1 || line.quantity()>99 || !seen.add(line.itemId()))throw new IllegalArgumentException("Invalid quantity or duplicate item");
        lines.sort(Comparator.comparing(l->l.itemId().toString()));
        int inserted=db.update("INSERT INTO inventory.reservation_batches(order_id,status) VALUES (?,'HELD') ON CONFLICT DO NOTHING",order);
        String status=db.queryForObject("SELECT status FROM inventory.reservation_batches WHERE order_id=? FOR UPDATE",String.class,order);
        if(!"HELD".equals(status))throw new IllegalStateException("Reservation already released");
        if(inserted==0) {
            var prior=db.query("SELECT item_id,quantity FROM inventory.stock_reservations WHERE order_id=? ORDER BY item_id::text",(rs,n)->new Line(rs.getObject(1,UUID.class),rs.getInt(2)),order);
            if(!prior.equals(lines))throw new IllegalStateException("Reservation payload conflict");
            return;
        }
        for(var line:lines)stock.reserve(order,line.itemId(),line.quantity());
    }
    @Transactional public void release(UUID order) {
        db.update("INSERT INTO inventory.reservation_batches(order_id,status) VALUES (?,'RELEASED') ON CONFLICT DO NOTHING",order);
        String status=db.queryForObject("SELECT status FROM inventory.reservation_batches WHERE order_id=? FOR UPDATE",String.class,order);
        if("RELEASED".equals(status))return;
        var items=db.queryForList("SELECT item_id FROM inventory.stock_reservations WHERE order_id=? ORDER BY item_id::text",UUID.class,order);
        for(var item:items)stock.release(order,item);
        db.update("UPDATE inventory.reservation_batches SET status='RELEASED' WHERE order_id=?",order);
    }
}