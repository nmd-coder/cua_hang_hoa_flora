package vn.flora.order;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
@RestController
@RequestMapping("/api/checkout/quotes")
public class CheckoutController {
    private final QuoteService quotes;
    private final IdentityClient identity;
    CheckoutController(QuoteService quotes,IdentityClient identity){this.quotes=quotes;this.identity=identity;}
    @PostMapping ResponseEntity<QuoteService.Quote> create(@RequestHeader(value="Authorization",required=false) String auth,@RequestBody QuoteService.Request request){
        return ResponseEntity.status(201).header("Cache-Control","no-store").body(quotes.create(identity.requireUser(auth),request));
    }
    @GetMapping("/{id}") ResponseEntity<QuoteService.Quote> get(@RequestHeader(value="Authorization",required=false) String auth,@PathVariable UUID id){
        return ResponseEntity.ok().header("Cache-Control","no-store").body(quotes.get(identity.requireUser(auth),id));
    }
}