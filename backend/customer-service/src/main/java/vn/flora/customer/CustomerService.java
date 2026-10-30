package vn.flora.customer;

import java.util.*;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class CustomerService {
    private final JdbcTemplate db;

    public CustomerService(JdbcTemplate db) {
        this.db = db;
    }

    // --- Profile ---
    public Map<String, Object> getProfile(UUID userId) {
        List<Map<String, Object>> rows = db.queryForList("""
            SELECT user_id, full_name, avatar_url
            FROM customer.customer_profiles
            WHERE user_id = ?
            """, userId);

        if (rows.isEmpty()) {
            return Map.of("user_id", userId, "full_name", "", "avatar_url", "");
        }
        return rows.get(0);
    }

    @Transactional
    public void updateProfile(UUID userId, String fullName, String avatarUrl) {
        db.update("""
            INSERT INTO customer.customer_profiles(user_id, full_name, avatar_url)
            VALUES (?, ?, ?)
            ON CONFLICT (user_id) DO UPDATE
            SET full_name = EXCLUDED.full_name,
                avatar_url = EXCLUDED.avatar_url
            """, userId, fullName != null ? fullName.trim() : "", avatarUrl != null ? avatarUrl.trim() : null);
    }

    // --- Addresses ---
    public record AddressDto(UUID id, String recipientName, String recipientPhone, String province,
                             String district, String ward, String addressDetail, boolean isDefault) {}

    public List<Map<String, Object>> listAddresses(UUID userId) {
        return db.queryForList("""
            SELECT id, recipient_name, recipient_phone, province, district, ward, address_detail, is_default
            FROM customer.addresses
            WHERE user_id = ?
            ORDER BY is_default DESC, id DESC
            """, userId);
    }

    @Transactional
    public UUID addAddress(UUID userId, AddressDto req) {
        if (req.recipientName() == null || req.recipientName().isBlank() ||
            req.recipientPhone() == null || req.recipientPhone().isBlank() ||
            req.addressDetail() == null || req.addressDetail().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Thông tin địa chỉ không đầy đủ");
        }

        UUID id = UUID.randomUUID();
        if (req.isDefault()) {
            db.update("UPDATE customer.addresses SET is_default = false WHERE user_id = ?", userId);
        } else {
            // If this is user's first address, make it default automatically
            int count = db.queryForObject("SELECT count(*) FROM customer.addresses WHERE user_id = ?", Integer.class, userId);
            if (count == 0) {
                db.update("""
                    INSERT INTO customer.addresses(id, user_id, recipient_name, recipient_phone, province, district, ward, address_detail, is_default)
                    VALUES (?, ?, ?, ?, ?, ?, ?, ?, true)
                    """, id, userId, req.recipientName().trim(), req.recipientPhone().trim(),
                    req.province() != null ? req.province().trim() : "",
                    req.district() != null ? req.district().trim() : "",
                    req.ward() != null ? req.ward().trim() : "",
                    req.addressDetail().trim());
                return id;
            }
        }

        db.update("""
            INSERT INTO customer.addresses(id, user_id, recipient_name, recipient_phone, province, district, ward, address_detail, is_default)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
            """, id, userId, req.recipientName().trim(), req.recipientPhone().trim(),
            req.province() != null ? req.province().trim() : "",
            req.district() != null ? req.district().trim() : "",
            req.ward() != null ? req.ward().trim() : "",
            req.addressDetail().trim(), req.isDefault());
        return id;
    }

    @Transactional
    public void updateAddress(UUID userId, UUID id, AddressDto req) {
        if (req.isDefault()) {
            db.update("UPDATE customer.addresses SET is_default = false WHERE user_id = ?", userId);
        }
        db.update("""
            UPDATE customer.addresses
            SET recipient_name = ?, recipient_phone = ?, province = ?, district = ?, ward = ?, address_detail = ?, is_default = ?
            WHERE id = ? AND user_id = ?
            """, req.recipientName(), req.recipientPhone(), req.province(), req.district(), req.ward(), req.addressDetail(), req.isDefault(), id, userId);
    }

    @Transactional
    public void deleteAddress(UUID userId, UUID id) {
        db.update("DELETE FROM customer.addresses WHERE id = ? AND user_id = ?", id, userId);
    }

    @Transactional
    public void setDefaultAddress(UUID userId, UUID id) {
        db.update("UPDATE customer.addresses SET is_default = false WHERE user_id = ?", userId);
        db.update("UPDATE customer.addresses SET is_default = true WHERE id = ? AND user_id = ?", id, userId);
    }

    // --- Wishlists ---
    public List<UUID> listWishlist(UUID userId) {
        return db.queryForList("SELECT product_id FROM customer.wishlists WHERE user_id = ? ORDER BY created_at DESC", UUID.class, userId);
    }

    public void addWishlist(UUID userId, UUID productId) {
        db.update("INSERT INTO customer.wishlists(user_id, product_id, created_at) VALUES (?, ?, now()) ON CONFLICT DO NOTHING", userId, productId);
    }

    public void removeWishlist(UUID userId, UUID productId) {
        db.update("DELETE FROM customer.wishlists WHERE user_id = ? AND product_id = ?", userId, productId);
    }

    // --- Reviews ---
    public List<Map<String, Object>> listReviews(UUID productId) {
        return db.queryForList("""
            SELECT r.id, r.order_item_id, r.user_id, r.product_id, r.rating, r.comment, r.created_at,
                   COALESCE(cp.full_name, 'Khách hàng FLORA') AS reviewer_name,
                   cp.avatar_url AS reviewer_avatar
            FROM customer.reviews r
            LEFT JOIN customer.customer_profiles cp ON cp.user_id = r.user_id
            WHERE r.product_id = ?
            ORDER BY r.created_at DESC
            """, productId);
    }

    @Transactional
    public UUID createReview(UUID userId, UUID orderItemId, UUID productId, int rating, String comment) {
        if (rating < 1 || rating > 5) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Số sao đánh giá phải từ 1 đến 5");
        }
        if (orderItemId == null || productId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Thông tin đơn hàng không hợp lệ");
        }

        // Check if this order item was already reviewed
        int existing = db.queryForObject("SELECT count(*) FROM customer.reviews WHERE order_item_id = ?", Integer.class, orderItemId);
        if (existing > 0) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Sản phẩm này trong đơn hàng đã được đánh giá");
        }

        UUID reviewId = UUID.randomUUID();
        db.update("""
            INSERT INTO customer.reviews(id, order_item_id, user_id, product_id, rating, comment, created_at)
            VALUES (?, ?, ?, ?, ?, ?, now())
            """, reviewId, orderItemId, userId, productId, rating, comment != null ? comment.trim() : "");

        return reviewId;
    }
}

