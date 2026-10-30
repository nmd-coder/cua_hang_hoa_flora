package vn.flora.order;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CartService {
    private final JdbcTemplate db;
    CartService(JdbcTemplate db) { this.db=db; }
    public List<Map<String,Object>> list(UUID user) {
        return db.queryForList("""
          SELECT i.item_id,i.quantity FROM "order".cart_items i
          JOIN "order".carts c ON c.id=i.cart_id WHERE c.user_id=? ORDER BY i.item_id
          """,user);
    }
    @Transactional
    public void set(UUID user,UUID item,int quantity) {
        if (user==null || item==null || quantity<1 || quantity>99) throw new IllegalArgumentException("Số lượng phải từ 1 đến 99");
        db.update("INSERT INTO \"order\".carts(id,user_id) VALUES (?,?) ON CONFLICT (user_id) DO NOTHING",UUID.randomUUID(),user);
        UUID cart=db.queryForObject("SELECT id FROM \"order\".carts WHERE user_id=?",UUID.class,user);
        db.update("""
          INSERT INTO "order".cart_items(id,cart_id,item_id,quantity) VALUES (?,?,?,?)
          ON CONFLICT (cart_id,item_id) DO UPDATE SET quantity=EXCLUDED.quantity
          """,UUID.randomUUID(),cart,item,quantity);
    }
    public void remove(UUID user,UUID item) {
        db.update("""
          DELETE FROM "order".cart_items i USING "order".carts c
          WHERE i.cart_id=c.id AND c.user_id=? AND i.item_id=?
          """,user,item);
    }
}