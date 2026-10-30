package vn.flora.order;

import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.server.ResponseStatusException;

@Service
public class CancelService {
    private final JdbcTemplate db;
    private final InventoryClient inventory;
    private final VoucherService vouchers;
    private final TransactionTemplate tx;

    CancelService(JdbcTemplate db, InventoryClient inventory, VoucherService vouchers, PlatformTransactionManager manager) {
        this.db = db;
        this.inventory = inventory;
        this.vouchers = vouchers;
        this.tx = new TransactionTemplate(manager);
    }

    public String cancel(UUID user, UUID id) {
        tx.executeWithoutResult(t -> {
            var rows = db.queryForList("SELECT status, payment_status FROM \"order\".orders WHERE id=? AND user_id=? FOR UPDATE", id, user);
            if (rows.isEmpty()) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy đơn");
            String state = (String) rows.get(0).get("status");
            if ("CANCELLED".equals(state) || "CANCEL_PENDING".equals(state)) return;
            if (!"PENDING".equals(state) || !"UNPAID".equals(rows.get(0).get("payment_status")))
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Đơn không còn đủ điều kiện hủy");
            db.update("UPDATE \"order\".orders SET status='CANCEL_PENDING' WHERE id=?", id);
            db.update("INSERT INTO \"order\".order_status_history(id,order_id,from_status,to_status,actor_id,reason) VALUES (?,?,'PENDING','CANCEL_PENDING',?,'Khách yêu cầu hủy')", UUID.randomUUID(), id, user);
        });

        return tx.execute(t -> {
            var row = db.queryForMap("SELECT status FROM \"order\".orders WHERE id=? AND user_id=? FOR UPDATE", id, user);
            String state = (String) row.get("status");
            if ("CANCELLED".equals(state)) return state;
            if (!"CANCEL_PENDING".equals(state)) throw new ResponseStatusException(HttpStatus.CONFLICT, "Đơn không còn đủ điều kiện hủy");
            inventory.release(id);
            vouchers.release(id);
            db.update("UPDATE \"order\".orders SET status='CANCELLED' WHERE id=?", id);
            db.update("INSERT INTO \"order\".order_status_history(id,order_id,from_status,to_status,actor_id,reason) VALUES (?,?,'CANCEL_PENDING','CANCELLED',?,'Đã giải phóng tồn và voucher')", UUID.randomUUID(), id, user);
            return "CANCELLED";
        });
    }

    public String adminCancel(UUID actorId, UUID id, String reason) {
        tx.executeWithoutResult(t -> {
            var rows = db.queryForList("SELECT status FROM \"order\".orders WHERE id=? FOR UPDATE", id);
            if (rows.isEmpty()) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy đơn");
            String state = (String) rows.get(0).get("status");
            if ("CANCELLED".equals(state)) return;
            db.update("UPDATE \"order\".orders SET status='CANCEL_PENDING' WHERE id=?", id);
            db.update("INSERT INTO \"order\".order_status_history(id,order_id,from_status,to_status,actor_id,reason) VALUES (?,?,?,?,?,'Quản trị viên hủy: ' || ?)",
                UUID.randomUUID(), id, state, "CANCEL_PENDING", actorId, reason != null ? reason : "Không có lý do");
        });

        return tx.execute(t -> {
            inventory.release(id);
            vouchers.release(id);
            db.update("UPDATE \"order\".orders SET status='CANCELLED' WHERE id=?", id);
            db.update("INSERT INTO \"order\".order_status_history(id,order_id,from_status,to_status,actor_id,reason) VALUES (?,?,'CANCEL_PENDING','CANCELLED',?,'Đã giải phóng tồn và voucher')",
                UUID.randomUUID(), id, actorId);
            return "CANCELLED";
        });
    }
}
