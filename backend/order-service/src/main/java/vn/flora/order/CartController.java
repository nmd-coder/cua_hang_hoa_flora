package vn.flora.order;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
@RestController
@RequestMapping("/api/cart")
public class CartController {
    private final CartService cart;
    private final IdentityClient identity;
    CartController(CartService cart,IdentityClient identity) { this.cart=cart; this.identity=identity; }
    record Quantity(Integer quantity) {}
    @GetMapping List<Map<String,Object>> list(@RequestHeader(value="Authorization",required=false) String auth) {
        return cart.list(identity.requireUser(auth));
    }
    @PutMapping("/items/{itemId}") ResponseEntity<Void> set(@RequestHeader(value="Authorization",required=false) String auth,@PathVariable UUID itemId,@RequestBody Quantity input) {
        UUID user=identity.requireUser(auth);
        if(input.quantity()==null) throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Thiếu số lượng");
        try { cart.set(user,itemId,input.quantity()); }
        catch(IllegalArgumentException e) { throw new ResponseStatusException(HttpStatus.BAD_REQUEST,e.getMessage()); }
        return ResponseEntity.noContent().build();
    }
    @DeleteMapping("/items/{itemId}") ResponseEntity<Void> remove(@RequestHeader(value="Authorization",required=false) String auth,@PathVariable UUID itemId) {
        cart.remove(identity.requireUser(auth),itemId);
        return ResponseEntity.noContent().build();
    }
}