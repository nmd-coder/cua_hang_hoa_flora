package vn.flora.order;

import static org.junit.jupiter.api.Assertions.*;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

class OrderTransitionsTest {
    @Test void staffCanAcceptOnlyPending() {
        assertDoesNotThrow(() -> OrderTransitions.verify("PENDING", "PROCESSING", false, Set.of("STAFF")));
        assertDoesNotThrow(() -> OrderTransitions.verify("PENDING", "PROCESSING", false, Set.of("ADMIN")));
        assertEquals(403, assertThrows(ResponseStatusException.class, () -> OrderTransitions.verify("PENDING", "PROCESSING", true, Set.of("CUSTOMER"))).getStatusCode().value());
        assertEquals(409, assertThrows(ResponseStatusException.class, () -> OrderTransitions.verify("CANCELLED", "PROCESSING", false, Set.of("STAFF"))).getStatusCode().value());
    }

    @Test void staffCanAdvanceToShippedAndCompleted() {
        assertDoesNotThrow(() -> OrderTransitions.verify("PROCESSING", "SHIPPED", false, Set.of("STAFF")));
        assertDoesNotThrow(() -> OrderTransitions.verify("SHIPPED", "COMPLETED", false, Set.of("ADMIN")));
        assertEquals(409, assertThrows(ResponseStatusException.class, () -> OrderTransitions.verify("PENDING", "SHIPPED", false, Set.of("STAFF"))).getStatusCode().value());
    }

    @Test void onlyOwnerCanConfirmCompletedOrder() {
        assertDoesNotThrow(() -> OrderTransitions.verify("COMPLETED", "RECEIVED", true, Set.of("CUSTOMER")));
        assertEquals(403, assertThrows(ResponseStatusException.class, () -> OrderTransitions.verify("COMPLETED", "RECEIVED", false, Set.of("CUSTOMER"))).getStatusCode().value());
        assertEquals(409, assertThrows(ResponseStatusException.class, () -> OrderTransitions.verify("PENDING", "RECEIVED", true, Set.of("CUSTOMER"))).getStatusCode().value());
    }
}
