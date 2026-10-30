package vn.flora.order;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.util.*;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.web.server.ResponseStatusException;

@Service
public class CodCheckoutService {
    public record Result(UUID id, String status) {}
    private final JdbcTemplate db;
    private final QuoteService quotes;
    private final InventoryClient inventory;
    private final VoucherService vouchers;
    private final ObjectMapper json;
    private final TransactionTemplate tx;

    CodCheckoutService(JdbcTemplate db, QuoteService quotes, InventoryClient inventory, VoucherService vouchers, ObjectMapper json, PlatformTransactionManager manager) {
        this.db = db;
        this.quotes = quotes;
        this.inventory = inventory;
        this.vouchers = vouchers;
        this.json = json;
        this.tx = new TransactionTemplate(manager);
    }

    public Result place(UUID user, UUID quoteId, String key) {
        if (quoteId == null || key == null || !key.matches("[A-Za-z0-9_-]{16,100}"))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Khóa đặt hàng không hợp lệ");

        UUID id = tx.execute(status -> {
            db.queryForList("SELECT pg_advisory_xact_lock(hashtextextended(?,0))", user + ":" + key);
            var existing = db.queryForList("SELECT id, quote_id FROM \"order\".checkout_intents WHERE user_id=? AND idempotency_key=?", user, key);
            if (!existing.isEmpty()) {
                if (!quoteId.equals(existing.get(0).get("quote_id")))
                    throw new ResponseStatusException(HttpStatus.CONFLICT, "Cùng khóa nhưng khác yêu cầu");
                return (UUID) existing.get(0).get("id");
            }
            var quote = quotes.get(user, quoteId);
            quotes.verifyCurrent(quote);
            UUID next = UUID.randomUUID();
            int inserted = db.update("INSERT INTO \"order\".checkout_intents(id,user_id,quote_id,idempotency_key,status,snapshot) VALUES (?,?,?,?,'PREPARING',CAST(? AS jsonb)) ON CONFLICT (quote_id) DO NOTHING",
                next, user, quoteId, key, encode(quote));
            if (inserted != 1)
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Báo giá đã được dùng cho yêu cầu khác");
            return next;
        });

        // Intent is durable before remote effects.
        Result result = tx.execute(status -> {
            var row = db.queryForMap("SELECT status, snapshot::text FROM \"order\".checkout_intents WHERE id=? AND user_id=? FOR UPDATE", id, user);
            String state = (String) row.get("status");
            if ("PENDING".equals(state)) {
                String current = db.queryForObject("SELECT status FROM \"order\".orders WHERE id=? AND user_id=?", String.class, id, user);
                return new Result(id, current);
            }
            if (!"PREPARING".equals(state)) return new Result(id, state);

            var quote = decode((String) row.get("snapshot"));
            try {
                inventory.reserve(id, quote);
            } catch (ResponseStatusException ex) {
                if (ex.getStatusCode().value() == 409) {
                    db.update("UPDATE \"order\".checkout_intents SET status='FAILED' WHERE id=?", id);
                    return new Result(id, "FAILED");
                }
                throw ex;
            }

            BigDecimal discount = quote.discountAmount() != null ? quote.discountAmount() : BigDecimal.ZERO;

            db.update("""
                INSERT INTO "order".orders(id, order_code, user_id, status, recipient_name, recipient_phone,
                    address_snapshot, delivery_date, delivery_time_slot, card_message, subtotal, discount_amount,
                    shipping_fee, total, payment_method, payment_status, shipping_mode, voucher_code)
                VALUES (?, ?, ?, 'PENDING', ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 'COD', 'UNPAID', 'MOCK', ?)
                """, id, "FL-" + id.toString().substring(0, 8).toUpperCase(), user, quote.recipientName(),
                quote.recipientPhone(), quote.address(), quote.deliveryDate(), quote.timeSlot(), quote.cardMessage(),
                quote.subtotal(), discount, quote.shippingFee(), quote.total(), quote.voucherCode());

            for (var line : quote.items()) {
                db.update("""
                    INSERT INTO "order".order_items(id, order_id, product_id, sku_snapshot, name_snapshot, image_snapshot, unit_price, quantity, line_total)
                    VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                    """, UUID.randomUUID(), id, line.productId(), line.sku(), line.name(), line.image(), line.unitPrice(), line.quantity(), line.lineTotal());
            }

            db.update("INSERT INTO \"order\".order_status_history(id,order_id,to_status,actor_id,reason) VALUES (?,?,'PENDING',?,'COD checkout')",
                UUID.randomUUID(), id, user);
            db.update("INSERT INTO \"order\".checkout_requests(user_id,idempotency_key,request_hash,order_id) VALUES (?,?,?,?)",
                user, key, quoteId.toString(), id);
            db.update("UPDATE \"order\".checkout_intents SET status='PENDING' WHERE id=?", id);

            // Redeem voucher if applicable
            if (quote.voucherCode() != null && !quote.voucherCode().isBlank()) {
                var validation = vouchers.validate(quote.voucherCode(), quote.subtotal());
                if (validation.valid()) {
                    vouchers.redeem(validation.id(), id, user, discount);
                }
            }

            return new Result(id, "PENDING");
        });

        if ("FAILED".equals(result.status()))
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Không đủ tồn, hãy tạo báo giá và yêu cầu mới");
        return result;
    }

    public List<Map<String, Object>> list(UUID user) {
        return db.queryForList("""
            SELECT id, order_code, status, total, currency, payment_method, payment_status, created_at,
                   recipient_name, delivery_date, delivery_time_slot
            FROM "order".orders
            WHERE user_id = ?
            ORDER BY created_at DESC
            LIMIT 100
            """, user);
    }

    public Map<String, Object> detail(UUID user, UUID id) {
        var rows = db.queryForList("SELECT * FROM \"order\".orders WHERE user_id=? AND id=?", user, id);
        if (rows.isEmpty()) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy đơn");
        var result = new LinkedHashMap<>(rows.get(0));
        result.put("items", db.queryForList("SELECT id, product_id, sku_snapshot, name_snapshot, image_snapshot, unit_price, quantity, line_total FROM \"order\".order_items WHERE order_id=?", id));
        result.put("history", db.queryForList("SELECT from_status, to_status, actor_id, reason, created_at FROM \"order\".order_status_history WHERE order_id=? ORDER BY created_at", id));
        return result;
    }

    private String encode(QuoteService.Quote q) {
        try { return json.writeValueAsString(q); } catch (Exception ex) { throw new IllegalStateException(ex); }
    }
    private QuoteService.Quote decode(String q) {
        try { return json.readValue(q, QuoteService.Quote.class); } catch (Exception ex) { throw new IllegalStateException(ex); }
    }
}
