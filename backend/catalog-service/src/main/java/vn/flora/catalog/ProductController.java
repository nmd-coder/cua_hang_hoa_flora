package vn.flora.catalog;

import java.math.BigDecimal;
import java.util.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

@RestController
public class ProductController {
    private final JdbcTemplate db;

    public ProductController(JdbcTemplate db) {
        this.db = db;
    }

    @GetMapping("/api/categories")
    public List<Map<String, Object>> categories() {
        return db.queryForList("SELECT id, name, slug, description, status FROM catalog.categories WHERE status='ACTIVE' ORDER BY name");
    }

    @GetMapping("/api/occasions")
    public List<Map<String, Object>> occasions() {
        return db.queryForList("SELECT id, name, slug FROM catalog.occasions ORDER BY name");
    }

    @GetMapping("/api/flower-types")
    public List<Map<String, Object>> flowerTypes() {
        return db.queryForList("SELECT id, name, slug FROM catalog.flower_types ORDER BY name");
    }

    @GetMapping("/api/colors")
    public List<Map<String, Object>> colors() {
        return db.queryForList("SELECT id, name, hex_code FROM catalog.colors ORDER BY name");
    }

    public List<Map<String, Object>> list(String q, int page, int size) {
        return list(q, null, null, null, null, null, null, "newest", "ACTIVE", page, size);
    }

    @GetMapping("/api/products")
    public List<Map<String, Object>> list(
            @RequestParam(defaultValue = "") String q,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String occasion,
            @RequestParam(required = false, name = "flower_type") String flowerType,
            @RequestParam(required = false) String color,
            @RequestParam(required = false, name = "min_price") BigDecimal minPrice,
            @RequestParam(required = false, name = "max_price") BigDecimal maxPrice,
            @RequestParam(defaultValue = "newest") String sort,
            @RequestParam(defaultValue = "ACTIVE") String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size) {

        if (q.length() > 100 || page < 0 || page > 10000 || size < 1 || size > 100) {
            throw new IllegalArgumentException("Tham số tìm kiếm không hợp lệ");
        }

        StringBuilder sql = new StringBuilder("""
            SELECT p.id, p.sku, p.name, p.slug, p.description, p.price, p.original_price, p.status,
                   c.name AS category_name, c.slug AS category_slug,
                   (SELECT i.url FROM catalog.product_images i WHERE i.product_id = p.id ORDER BY i.is_primary DESC, i.sort_order LIMIT 1) AS image
            FROM catalog.products p
            LEFT JOIN catalog.categories c ON c.id = p.category_id
            WHERE 1=1
            """);

        List<Object> params = new ArrayList<>();

        if (!"ALL".equalsIgnoreCase(status)) {
            sql.append(" AND p.status = ?");
            params.add(status.toUpperCase(Locale.ROOT));
        }

        if (q != null && !q.isBlank()) {
            String keyword = "%" + q.trim().toLowerCase(Locale.ROOT).replace("!", "!!").replace("%", "!%").replace("_", "!_") + "%";
            sql.append(" AND (lower(p.name) LIKE ? ESCAPE '!' OR lower(p.description) LIKE ? ESCAPE '!' OR lower(p.sku) LIKE ? ESCAPE '!')");
            params.add(keyword);
            params.add(keyword);
            params.add(keyword);
        }

        if (category != null && !category.isBlank()) {
            if (category.matches("[0-9a-fA-F-]{36}")) {
                sql.append(" AND p.category_id = ?::uuid");
                params.add(category);
            } else {
                sql.append(" AND c.slug = ?");
                params.add(category.trim().toLowerCase(Locale.ROOT));
            }
        }

        if (occasion != null && !occasion.isBlank()) {
            sql.append(" AND EXISTS (SELECT 1 FROM catalog.product_occasions po JOIN catalog.occasions o ON o.id = po.occasion_id WHERE po.product_id = p.id AND (o.slug = ? OR o.id::text = ?))");
            params.add(occasion.trim().toLowerCase(Locale.ROOT));
            params.add(occasion.trim());
        }

        if (flowerType != null && !flowerType.isBlank()) {
            sql.append(" AND EXISTS (SELECT 1 FROM catalog.product_flower_types pft JOIN catalog.flower_types ft ON ft.id = pft.flower_type_id WHERE pft.product_id = p.id AND (ft.slug = ? OR ft.id::text = ?))");
            params.add(flowerType.trim().toLowerCase(Locale.ROOT));
            params.add(flowerType.trim());
        }

        if (color != null && !color.isBlank()) {
            sql.append(" AND EXISTS (SELECT 1 FROM catalog.product_colors pc JOIN catalog.colors col ON col.id = pc.color_id WHERE pc.product_id = p.id AND (lower(col.name) = ? OR col.id::text = ?))");
            params.add(color.trim().toLowerCase(Locale.ROOT));
            params.add(color.trim());
        }

        if (minPrice != null && minPrice.compareTo(BigDecimal.ZERO) >= 0) {
            sql.append(" AND p.price >= ?");
            params.add(minPrice);
        }

        if (maxPrice != null && maxPrice.compareTo(BigDecimal.ZERO) >= 0) {
            sql.append(" AND p.price <= ?");
            params.add(maxPrice);
        }

        // Sorting
        switch (sort.toLowerCase(Locale.ROOT)) {
            case "price_asc" -> sql.append(" ORDER BY p.price ASC, p.id ASC");
            case "price_desc" -> sql.append(" ORDER BY p.price DESC, p.id ASC");
            case "name" -> sql.append(" ORDER BY p.name ASC, p.id ASC");
            case "newest" -> sql.append(" ORDER BY p.created_at DESC, p.id ASC");
            default -> sql.append(" ORDER BY p.created_at DESC, p.id ASC");
        }

        sql.append(" LIMIT ? OFFSET ?");
        params.add(size);
        params.add(page * size);

        return db.queryForList(sql.toString(), params.toArray());
    }

    @GetMapping("/api/products/{identifier}")
    public ResponseEntity<Map<String, Object>> detail(@PathVariable String identifier) {
        boolean isUuid = identifier.matches("[0-9a-fA-F-]{36}");
        String query = """
            SELECT p.id, p.category_id, p.sku, p.name, p.slug, p.description, p.price, p.original_price, p.status,
                   p.weight_g, p.length_cm, p.width_cm, p.height_cm, p.created_at, p.updated_at,
                   c.name AS category_name, c.slug AS category_slug
            FROM catalog.products p
            LEFT JOIN catalog.categories c ON c.id = p.category_id
            WHERE 
            """ + (isUuid ? "p.id = ?::uuid" : "p.slug = ?");

        List<Map<String, Object>> rows = db.queryForList(query, identifier);
        if (rows.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Map<String, Object> product = new HashMap<>(rows.get(0));
        UUID productId = (UUID) product.get("id");

        // Images
        List<Map<String, Object>> images = db.queryForList("""
            SELECT id, url, alt_text, sort_order, is_primary
            FROM catalog.product_images
            WHERE product_id = ?
            ORDER BY is_primary DESC, sort_order ASC
            """, productId);
        product.put("images", images);
        product.put("image", images.isEmpty() ? null : images.get(0).get("url"));

        // Occasions
        List<Map<String, Object>> productOccasions = db.queryForList("""
            SELECT o.id, o.name, o.slug
            FROM catalog.product_occasions po
            JOIN catalog.occasions o ON o.id = po.occasion_id
            WHERE po.product_id = ?
            ORDER BY o.name
            """, productId);
        product.put("occasions", productOccasions);

        // Flower Types
        List<Map<String, Object>> productFlowerTypes = db.queryForList("""
            SELECT ft.id, ft.name, ft.slug
            FROM catalog.product_flower_types pft
            JOIN catalog.flower_types ft ON ft.id = pft.flower_type_id
            WHERE pft.product_id = ?
            ORDER BY ft.name
            """, productId);
        product.put("flower_types", productFlowerTypes);

        // Colors
        List<Map<String, Object>> productColors = db.queryForList("""
            SELECT col.id, col.name, col.hex_code
            FROM catalog.product_colors pc
            JOIN catalog.colors col ON col.id = pc.color_id
            WHERE pc.product_id = ?
            ORDER BY col.name
            """, productId);
        product.put("colors", productColors);

        return ResponseEntity.ok(product);
    }

    // Admin Product Management
    public record CreateProductRequest(
            String name,
            String sku,
            String slug,
            String description,
            BigDecimal price,
            BigDecimal originalPrice,
            UUID categoryId,
            String imageUrl,
            Integer weightG,
            Integer lengthCm,
            Integer widthCm,
            Integer heightCm,
            List<UUID> occasionIds,
            List<UUID> flowerTypeIds,
            List<UUID> colorIds
    ) {}

    @PostMapping("/api/admin/products")
    @Transactional
    public ResponseEntity<Map<String, Object>> createProduct(@RequestBody CreateProductRequest req) {
        if (req.name() == null || req.name().isBlank() || req.price() == null || req.price().compareTo(BigDecimal.ZERO) < 0) {
            return ResponseEntity.badRequest().body(Map.of("code", "INVALID_REQUEST", "message", "Tên và giá sản phẩm không hợp lệ"));
        }

        UUID id = UUID.randomUUID();
        String sku = req.sku() != null && !req.sku().isBlank() ? req.sku().trim() : "FL-" + id.toString().substring(0, 6).toUpperCase();
        String slug = req.slug() != null && !req.slug().isBlank() ? req.slug().trim() : req.name().toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]+", "-") + "-" + id.toString().substring(0, 4);

        db.update("""
            INSERT INTO catalog.products(id, category_id, sku, name, slug, description, price, original_price, status, weight_g, length_cm, width_cm, height_cm)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, 'ACTIVE', ?, ?, ?, ?)
            """, id, req.categoryId(), sku, req.name().trim(), slug, req.description(), req.price(), req.originalPrice(),
            req.weightG() != null ? req.weightG() : 500,
            req.lengthCm() != null ? req.lengthCm() : 30,
            req.widthCm() != null ? req.widthCm() : 25,
            req.heightCm() != null ? req.heightCm() : 45
        );

        if (req.imageUrl() != null && !req.imageUrl().isBlank()) {
            UUID imgId = UUID.randomUUID();
            db.update("""
                INSERT INTO catalog.product_images(id, product_id, url, alt_text, sort_order, is_primary)
                VALUES (?, ?, ?, ?, 1, true)
                """, imgId, id, req.imageUrl().trim(), req.name().trim());
        }

        if (req.occasionIds() != null) {
            for (UUID occId : req.occasionIds()) {
                db.update("INSERT INTO catalog.product_occasions(product_id, occasion_id) VALUES (?, ?) ON CONFLICT DO NOTHING", id, occId);
            }
        }

        if (req.flowerTypeIds() != null) {
            for (UUID ftId : req.flowerTypeIds()) {
                db.update("INSERT INTO catalog.product_flower_types(product_id, flower_type_id) VALUES (?, ?) ON CONFLICT DO NOTHING", id, ftId);
            }
        }

        if (req.colorIds() != null) {
            for (UUID colId : req.colorIds()) {
                db.update("INSERT INTO catalog.product_colors(product_id, color_id) VALUES (?, ?) ON CONFLICT DO NOTHING", id, colId);
            }
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("id", id, "sku", sku, "slug", slug));
    }

    @PutMapping("/api/admin/products/{id}")
    @Transactional
    public ResponseEntity<Void> updateProduct(@PathVariable UUID id, @RequestBody CreateProductRequest req) {
        db.update("""
            UPDATE catalog.products
            SET name = COALESCE(?, name),
                description = COALESCE(?, description),
                price = COALESCE(?, price),
                original_price = COALESCE(?, original_price),
                category_id = COALESCE(?, category_id),
                weight_g = COALESCE(?, weight_g),
                length_cm = COALESCE(?, length_cm),
                width_cm = COALESCE(?, width_cm),
                height_cm = COALESCE(?, height_cm),
                updated_at = now()
            WHERE id = ?
            """, req.name(), req.description(), req.price(), req.originalPrice(), req.categoryId(),
            req.weightG(), req.lengthCm(), req.widthCm(), req.heightCm(), id);

        if (req.imageUrl() != null && !req.imageUrl().isBlank()) {
            int count = db.queryForObject("SELECT count(*) FROM catalog.product_images WHERE product_id = ?", Integer.class, id);
            if (count == 0) {
                db.update("INSERT INTO catalog.product_images(id, product_id, url, alt_text, sort_order, is_primary) VALUES (?, ?, ?, ?, 1, true)",
                    UUID.randomUUID(), id, req.imageUrl().trim(), req.name() != null ? req.name() : "Hoa");
            } else {
                db.update("UPDATE catalog.product_images SET url = ? WHERE product_id = ? AND is_primary = true", req.imageUrl().trim(), id);
            }
        }

        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/api/admin/products/{id}/status")
    public ResponseEntity<Void> updateStatus(@PathVariable UUID id, @RequestBody Map<String, String> body) {
        String newStatus = body.get("status");
        if (newStatus == null || (!newStatus.equals("ACTIVE") && !newStatus.equals("HIDDEN") && !newStatus.equals("ARCHIVED"))) {
            return ResponseEntity.badRequest().build();
        }
        db.update("UPDATE catalog.products SET status = ?, updated_at = now() WHERE id = ?", newStatus, id);
        return ResponseEntity.noContent().build();
    }

    @ExceptionHandler(IllegalArgumentException.class)
    ResponseEntity<Map<String, String>> invalid(IllegalArgumentException ex) {
        return ResponseEntity.badRequest().body(Map.of("code", "INVALID_ARGUMENT", "message", ex.getMessage()));
    }
}
