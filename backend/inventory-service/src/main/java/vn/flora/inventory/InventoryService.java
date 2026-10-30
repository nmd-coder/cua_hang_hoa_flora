package vn.flora.inventory;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Local stock transaction. Callers must authenticate and implement cross-service recovery. */
@Service
public class InventoryService {
    private final JdbcTemplate db;
    InventoryService(JdbcTemplate db) { this.db = db; }

    @Transactional
    public void reserve(UUID orderId, UUID itemId, int quantity) {
        if (orderId == null || itemId == null || quantity <= 0) throw new IllegalArgumentException("Invalid reservation");
        lockStock(itemId);
        List<Map<String,Object>> prior = db.queryForList(
            "SELECT quantity,status FROM inventory.stock_reservations WHERE order_id=? AND item_id=?", orderId, itemId);
        if (!prior.isEmpty()) {
            var row = prior.get(0);
            if (((Number)row.get("quantity")).intValue() == quantity && "HELD".equals(row.get("status"))) return;
            throw new IllegalStateException("Reservation conflict");
        }
        int changed = db.update("""
            UPDATE inventory.stock SET reserved=reserved+?,updated_at=now()
            WHERE item_id=? AND on_hand-reserved>=?
            """, quantity,itemId,quantity);
        if (changed != 1) throw new IllegalStateException("Insufficient stock");
        // The provisional 15-minute expiry is a placeholder; no automatic release worker exists yet.
        db.update("""
            INSERT INTO inventory.stock_reservations(id,order_id,item_id,quantity,status,expires_at)
            VALUES (?,?,?,?,'HELD',now() + interval '15 minutes')
            """, UUID.randomUUID(),orderId,itemId,quantity);
    }

    @Transactional
    public void release(UUID orderId, UUID itemId) { finish(orderId,itemId,false); }

    @Transactional
    public void commit(UUID orderId, UUID itemId) { finish(orderId,itemId,true); }

    private void finish(UUID orderId, UUID itemId, boolean consume) {
        lockStock(itemId);
        var rows = db.queryForList("SELECT quantity,status FROM inventory.stock_reservations WHERE order_id=? AND item_id=?",orderId,itemId);
        if (rows.isEmpty()) throw new IllegalStateException("Reservation missing");
        var row=rows.get(0);
        String status=(String)row.get("status");
        String target=consume ? "COMMITTED" : "RELEASED";
        if (target.equals(status)) return;
        if (!"HELD".equals(status)) throw new IllegalStateException("Reservation already finalized");
        int quantity=((Number)row.get("quantity")).intValue();
        if (consume) db.update("UPDATE inventory.stock SET on_hand=on_hand-?,reserved=reserved-?,updated_at=now() WHERE item_id=?",quantity,quantity,itemId);
        else db.update("UPDATE inventory.stock SET reserved=reserved-?,updated_at=now() WHERE item_id=?",quantity,itemId);
        db.update("UPDATE inventory.stock_reservations SET status=? WHERE order_id=? AND item_id=?",target,orderId,itemId);
        db.update("INSERT INTO inventory.stock_movements(id,item_id,change_qty,reason,reference_id) VALUES (?,?,?,?,?)",
            UUID.randomUUID(),itemId,consume ? -quantity : 0,target,orderId);
    }

    private void lockStock(UUID itemId) {
        if (itemId == null || db.queryForList("SELECT item_id FROM inventory.stock WHERE item_id=? FOR UPDATE",itemId).isEmpty())
            throw new IllegalStateException("Stock item missing");
    }
}