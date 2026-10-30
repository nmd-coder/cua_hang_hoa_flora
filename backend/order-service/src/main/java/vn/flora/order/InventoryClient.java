package vn.flora.order;
import java.net.http.HttpClient;
import java.time.Duration;
import java.util.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.*;
import org.springframework.web.server.ResponseStatusException;
@Component
public class InventoryClient {
    private final RestClient client; private final String key;
    InventoryClient(@Value("${INVENTORY_URL:http://localhost:8084}")String url,@Value("${INTERNAL_API_KEY:}")String key){
        this.key=key;
        var factory=new JdkClientHttpRequestFactory(HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(3)).build());
        factory.setReadTimeout(Duration.ofSeconds(5));
        client=RestClient.builder().baseUrl(url).requestFactory(factory).build();
    }
    public void reserve(UUID order,QuoteService.Quote quote){
        if(key.length()<32)throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,"Chưa cấu hình kết nối kho");
        var lines=quote.items().stream().map(l->Map.of("itemId",l.productId(),"quantity",l.quantity())).toList();
        try{
            client.put().uri("/internal/reservations/{id}",order).header("X-Internal-Key",key).contentType(MediaType.APPLICATION_JSON).body(lines)
                .exchange((req,res)->{if(res.getStatusCode().value()==409)throw new ResponseStatusException(HttpStatus.CONFLICT,"Không đủ tồn kho");if(!res.getStatusCode().is2xxSuccessful())throw unavailable();return null;});
        }catch(ResponseStatusException ex){throw ex;}catch(RestClientException|java.util.concurrent.CancellationException ex){throw unavailable();}
    }
    public void release(UUID order){
        if(key.length()<32)throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,"Chưa cấu hình kết nối kho");
        try {
            client.delete().uri("/internal/reservations/{id}",order).header("X-Internal-Key",key)
                .exchange((req,res)->{if(!res.getStatusCode().is2xxSuccessful())throw unavailable();return null;});
        } catch(ResponseStatusException ex){throw ex;}
        catch(RestClientException|java.util.concurrent.CancellationException ex){throw unavailable();}
    }
    private static ResponseStatusException unavailable(){return new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,"Kho tạm thời không khả dụng. Thử lại cùng khóa đặt hàng.");}
}