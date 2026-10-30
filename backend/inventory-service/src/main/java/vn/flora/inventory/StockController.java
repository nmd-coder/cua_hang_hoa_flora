package vn.flora.inventory;

import java.util.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/inventory")
public class StockController {
    private final JdbcTemplate db;

    public StockController(JdbcTemplate db) {
        this.db = db;
    }

    @GetMapping("/stock")
    public List<Map<String, Object>> listStock() {
        return db.queryForList("""
            SELECT item_id, on_hand, reserved, (on_hand - reserved) AS available, updated_at
            FROM inventory.stock
            ORDER BY available ASC, on_hand ASC
            """);
    }

    @GetMapping("/stock/{itemId}")
    public ResponseEntity<Map<String, Object>> getStock(@PathVariable UUID itemId) {
        List<Map<String, Object>> rows = db.queryForList("""
            SELECT item_id, on_hand, reserved, (on_hand - reserved) AS available, updated_at
            FROM inventory.stock
            WHERE item_id = ?
            """, itemId);
        if (rows.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(rows.get(0));
    }

    public record StockAdjustment(UUID itemId, int changeQty, String reason, UUID actorId) {}

    @PostMapping("/adjust")
    @Transactional
    public ResponseEntity<Map<String, Object>> adjustStock(@RequestBody StockAdjustment req) {
        if (req.itemId() == null || req.changeQty() == 0 || req.reason() == null || req.reason().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Thông tin điều chỉnh tồn kho không hợp lệ");
        }

        // Lock row
        List<Map<String, Object>> rows = db.queryForList("""
            SELECT item_id, on_hand, reserved
            FROM inventory.stock
            WHERE item_id = ?
            FOR UPDATE
            """, req.itemId());

        if (rows.isEmpty()) {
            // If item does not exist in stock table, create it if changeQty > 0
            if (req.changeQty() < 0) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Sản phẩm chưa có trong kho");
            }
            db.update("INSERT INTO inventory.stock(item_id, on_hand, reserved) VALUES (?, ?, 0)", req.itemId(), req.changeQty());
        } else {
            int currentOnHand = (int) rows.get(0).get("on_hand");
            int reserved = (int) rows.get(0).get("reserved");
            int newOnHand = currentOnHand + req.changeQty();

            if (newOnHand < reserved) {
                throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Không thể giảm tồn dưới mức đang giữ hàng: Hiện có " + currentOnHand + ", đang giữ " + reserved);
            }
            if (newOnHand < 0) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Tồn kho không được âm");
            }

            db.update("UPDATE inventory.stock SET on_hand = ?, updated_at = now() WHERE item_id = ?", newOnHand, req.itemId());
        }

        UUID movementId = UUID.randomUUID();
        db.update("""
            INSERT INTO inventory.stock_movements(id, item_id, change_qty, reason, reference_id, actor_id, created_at)
            VALUES (?, ?, ?, ?, ?, ?, now())
            """, movementId, req.itemId(), req.changeQty(), req.reason().trim(), null, req.actorId());

        return ResponseEntity.ok(Map.of(
            "itemId", req.itemId(),
            "changeQty", req.changeQty(),
            "movementId", movementId,
            "status", "SUCCESS"
        ));
    }

    @GetMapping("/movements")
    public List<Map<String, Object>> listMovements(
            @RequestParam(required = false) UUID itemId,
            @RequestParam(defaultValue = "50") int limit) {
        if (itemId != null) {
            return db.queryForList("""
                SELECT id, item_id, change_qty, reason, reference_id, actor_id, created_at
                FROM inventory.stock_movements
                WHERE item_id = ?
                ORDER BY created_at DESC
                LIMIT ?
                """, itemId, Math.min(limit, 200));
        }
        return db.queryForList("""
            SELECT id, item_id, change_qty, reason, reference_id, actor_id, created_at
            FROM inventory.stock_movements
            ORDER BY created_at DESC
            LIMIT ?
            """, Math.min(limit, 200));
    }
}

