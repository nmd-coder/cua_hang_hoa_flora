package vn.flora.order;
import java.util.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
@RestController
@RequestMapping("/api/orders")
class OrderController {
    record Request(UUID quoteId){}
    private final CodCheckoutService orders;private final IdentityClient identity;private final CancelService cancel;private final OrderTransitionService transitions;
    OrderController(CodCheckoutService orders,IdentityClient identity,CancelService cancel,OrderTransitionService transitions){this.orders=orders;this.identity=identity;this.cancel=cancel;this.transitions=transitions;}
    @PostMapping ResponseEntity<CodCheckoutService.Result> place(@RequestHeader(value="Authorization",required=false)String auth,@RequestHeader(value="Idempotency-Key",required=false)String key,@RequestBody Request request){
        return ResponseEntity.ok().header("Cache-Control","no-store").body(orders.place(identity.requireUser(auth),request.quoteId(),key));
    }
    @PostMapping("/{id}/cancel") Map<String,String> cancel(@RequestHeader(value="Authorization",required=false)String auth,@PathVariable UUID id){return Map.of("status",cancel.cancel(identity.requireUser(auth),id));}
    record StatusRequest(String status){}
    @PostMapping("/{id}/status") ResponseEntity<Void> transition(@RequestHeader(value="Authorization",required=false)String auth,@PathVariable UUID id,@RequestBody StatusRequest request){
        transitions.transition(identity.requireActor(auth),id,request.status());return ResponseEntity.noContent().build();
    }
    @GetMapping List<Map<String,Object>> list(@RequestHeader(value="Authorization",required=false)String auth){return orders.list(identity.requireUser(auth));}
    @GetMapping("/{id}") Map<String,Object> detail(@RequestHeader(value="Authorization",required=false)String auth,@PathVariable UUID id){return orders.detail(identity.requireUser(auth),id);}
}