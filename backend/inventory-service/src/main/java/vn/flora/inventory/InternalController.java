package vn.flora.inventory;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/internal/reservations")
class InternalController {
    private final BatchService batches;private final String key;
    InternalController(BatchService batches,@Value("${INTERNAL_API_KEY:}") String key){this.batches=batches;this.key=key;}
    private void authorize(String supplied){
        if(key.length()<32 || supplied==null || !MessageDigest.isEqual(key.getBytes(StandardCharsets.UTF_8),supplied.getBytes(StandardCharsets.UTF_8)))
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
    }
    @PutMapping("/{order}") ResponseEntity<Void> reserve(@RequestHeader(value="X-Internal-Key",required=false) String supplied,@PathVariable UUID order,@RequestBody List<BatchService.Line> lines){
        authorize(supplied);
        try{batches.reserve(order,lines);}
        catch(IllegalArgumentException ex){throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Yêu cầu giữ tồn không hợp lệ");}
        catch(IllegalStateException ex){throw new ResponseStatusException(HttpStatus.CONFLICT,"Không đủ tồn hoặc yêu cầu xung đột");}
        return ResponseEntity.noContent().build();
    }
    @DeleteMapping("/{order}") ResponseEntity<Void> release(@RequestHeader(value="X-Internal-Key",required=false)String supplied,@PathVariable UUID order){
        authorize(supplied);batches.release(order);return ResponseEntity.noContent().build();
    }
}