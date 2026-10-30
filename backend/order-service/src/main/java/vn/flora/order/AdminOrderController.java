package vn.flora.order;

import java.math.BigDecimal;
import java.util.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/admin")
public class AdminOrderController {
    private final JdbcTemplate db;
    private final IdentityClient identity;
    private final OrderTransitionService transitions;
    private final CancelService cancelService;

    public AdminOrderController(JdbcTemplate db, IdentityClient identity, OrderTransitionService transitions, CancelService cancelService) {
        this.db = db;
        this.identity = identity;
        this.transitions = transitions;
        this.cancelService = cancelService;
    }

    private IdentityClient.Actor requireStaffOrAdmin(String auth) {
        var actor = identity.requireActor(auth);
        if (!actor.roles().contains("STAFF") && !actor.roles().contains("ADMIN")) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Yêu cầu quyền nhân viên hoặc quản trị viên");
        }
        return actor;
    }

    @GetMapping("/orders")
    public List<Map<String, Object>> listOrders(
            @RequestHeader(value = "Authorization", required = false) String auth,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "") String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        requireStaffOrAdmin(auth);

        StringBuilder sql = new StringBuilder("""
            SELECT o.id, o.order_code, o.user_id, o.status, o.recipient_name, o.recipient_phone,
                   o.delivery_date, o.delivery_time_slot, o.subtotal, o.discount_amount, o.shipping_fee,
                   o.total, o.payment_method, o.payment_status, o.voucher_code, o.created_at
            FROM "order".orders o
            WHERE 1=1
            """);

        List<Object> params = new ArrayList<>();
        if (status != null && !status.isBlank() && !"ALL".equalsIgnoreCase(status)) {
            sql.append(" AND o.status = ?");
            params.add(status.trim().toUpperCase(Locale.ROOT));
        }

        if (q != null && !q.isBlank()) {
            String keyword = "%" + q.trim().toLowerCase(Locale.ROOT) + "%";
            sql.append(" AND (lower(o.order_code) LIKE ? OR lower(o.recipient_name) LIKE ? OR lower(o.recipient_phone) LIKE ?)");
            params.add(keyword);
            params.add(keyword);
            params.add(keyword);
        }

        sql.append(" ORDER BY o.created_at DESC LIMIT ? OFFSET ?");
        params.add(Math.min(size, 100));
        params.add(page * size);

        List<Map<String, Object>> orders = db.queryForList(sql.toString(), params.toArray());
        for (var o : orders) {
            UUID orderId = (UUID) o.get("id");
            List<Map<String, Object>> items = db.queryForList("""
                SELECT id, product_id, sku_snapshot, name_snapshot, unit_price, quantity, line_total
                FROM "order".order_items
                WHERE order_id = ?
                """, orderId);
            o.put("items", items);
        }
        return orders;
    }

    @GetMapping("/orders/{id}")
    public Map<String, Object> getOrderDetail(
            @RequestHeader(value = "Authorization", required = false) String auth,
            @PathVariable UUID id) {
        requireStaffOrAdmin(auth);

        List<Map<String, Object>> rows = db.queryForList("SELECT * FROM \"order\".orders WHERE id = ?", id);
        if (rows.isEmpty()) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy đơn hàng");

        Map<String, Object> order = new LinkedHashMap<>(rows.get(0));
        order.put("items", db.queryForList("""
            SELECT id, product_id, sku_snapshot, name_snapshot, image_snapshot, unit_price, quantity, line_total
            FROM "order".order_items
            WHERE order_id = ?
            """, id));
        order.put("history", db.queryForList("""
            SELECT from_status, to_status, actor_id, reason, created_at
            FROM "order".order_status_history
            WHERE order_id = ?
            ORDER BY created_at ASC
            """, id));

        return order;
    }

    public record UpdateStatusRequest(String status, String reason) {}

    @PostMapping("/orders/{id}/status")
    public ResponseEntity<Void> updateOrderStatus(
            @RequestHeader(value = "Authorization", required = false) String auth,
            @PathVariable UUID id,
            @RequestBody UpdateStatusRequest req) {
        var actor = requireStaffOrAdmin(auth);
        String target = req.status() != null ? req.status().trim().toUpperCase(Locale.ROOT) : "";

        if ("CANCELLED".equals(target)) {
            cancelService.adminCancel(actor.id(), id, req.reason());
            return ResponseEntity.noContent().build();
        }

        transitions.transition(actor, id, target);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/dashboard")
    public Map<String, Object> getDashboardStats(
            @RequestHeader(value = "Authorization", required = false) String auth) {
        requireStaffOrAdmin(auth);

        int pendingCount = db.queryForObject("SELECT count(*) FROM \"order\".orders WHERE status = 'PENDING'", Integer.class);
        int processingCount = db.queryForObject("SELECT count(*) FROM \"order\".orders WHERE status = 'PROCESSING'", Integer.class);
        int shippingCount = db.queryForObject("SELECT count(*) FROM \"order\".orders WHERE status = 'SHIPPED'", Integer.class);
        int completedCount = db.queryForObject("SELECT count(*) FROM \"order\".orders WHERE status IN ('COMPLETED', 'RECEIVED')", Integer.class);
        int cancelledCount = db.queryForObject("SELECT count(*) FROM \"order\".orders WHERE status = 'CANCELLED'", Integer.class);

        BigDecimal revenue = db.queryForObject("""
            SELECT COALESCE(sum(total), 0) FROM "order".orders WHERE status IN ('COMPLETED', 'RECEIVED')
            """, BigDecimal.class);

        List<Map<String, Object>> recentOrders = db.queryForList("""
            SELECT id, order_code, recipient_name, total, status, created_at
            FROM "order".orders
            ORDER BY created_at DESC
            LIMIT 5
            """);

        Map<String, Object> stats = new HashMap<>();
        stats.put("newOrders", pendingCount);
        stats.put("processingOrders", processingCount);
        stats.put("shippingOrders", shippingCount);
        stats.put("completedOrders", completedCount);
        stats.put("cancelledOrders", cancelledCount);
        stats.put("totalRevenue", revenue != null ? revenue : BigDecimal.ZERO);
        stats.put("recentOrders", recentOrders);
        return stats;
    }
}

