package vn.flora.customer;

import java.util.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class CustomerController {
    private final CustomerService customerService;
    private final IdentityClient identity;

    public CustomerController(CustomerService customerService, IdentityClient identity) {
        this.customerService = customerService;
        this.identity = identity;
    }

    // --- Profile ---
    @GetMapping("/api/customer/profile")
    public ResponseEntity<Map<String, Object>> getProfile(@RequestHeader(value = "Authorization", required = false) String auth) {
        UUID userId = identity.requireUser(auth);
        return ResponseEntity.ok(customerService.getProfile(userId));
    }

    public record UpdateProfileRequest(String fullName, String avatarUrl) {}

    @PutMapping("/api/customer/profile")
    public ResponseEntity<Void> updateProfile(
            @RequestHeader(value = "Authorization", required = false) String auth,
            @RequestBody UpdateProfileRequest req) {
        UUID userId = identity.requireUser(auth);
        customerService.updateProfile(userId, req.fullName(), req.avatarUrl());
        return ResponseEntity.noContent().build();
    }

    // --- Addresses ---
    @GetMapping("/api/customer/addresses")
    public ResponseEntity<List<Map<String, Object>>> listAddresses(@RequestHeader(value = "Authorization", required = false) String auth) {
        UUID userId = identity.requireUser(auth);
        return ResponseEntity.ok(customerService.listAddresses(userId));
    }

    @PostMapping("/api/customer/addresses")
    public ResponseEntity<Map<String, Object>> addAddress(
            @RequestHeader(value = "Authorization", required = false) String auth,
            @RequestBody CustomerService.AddressDto req) {
        UUID userId = identity.requireUser(auth);
        UUID id = customerService.addAddress(userId, req);
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("id", id, "status", "SUCCESS"));
    }

    @PutMapping("/api/customer/addresses/{id}")
    public ResponseEntity<Void> updateAddress(
            @RequestHeader(value = "Authorization", required = false) String auth,
            @PathVariable UUID id,
            @RequestBody CustomerService.AddressDto req) {
        UUID userId = identity.requireUser(auth);
        customerService.updateAddress(userId, id, req);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/api/customer/addresses/{id}")
    public ResponseEntity<Void> deleteAddress(
            @RequestHeader(value = "Authorization", required = false) String auth,
            @PathVariable UUID id) {
        UUID userId = identity.requireUser(auth);
        customerService.deleteAddress(userId, id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/api/customer/addresses/{id}/default")
    public ResponseEntity<Void> setDefaultAddress(
            @RequestHeader(value = "Authorization", required = false) String auth,
            @PathVariable UUID id) {
        UUID userId = identity.requireUser(auth);
        customerService.setDefaultAddress(userId, id);
        return ResponseEntity.noContent().build();
    }

    // --- Wishlists ---
    @GetMapping("/api/customer/wishlist")
    public ResponseEntity<List<UUID>> listWishlist(@RequestHeader(value = "Authorization", required = false) String auth) {
        UUID userId = identity.requireUser(auth);
        return ResponseEntity.ok(customerService.listWishlist(userId));
    }

    @PostMapping("/api/customer/wishlist/{productId}")
    public ResponseEntity<Void> addWishlist(
            @RequestHeader(value = "Authorization", required = false) String auth,
            @PathVariable UUID productId) {
        UUID userId = identity.requireUser(auth);
        customerService.addWishlist(userId, productId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/api/customer/wishlist/{productId}")
    public ResponseEntity<Void> removeWishlist(
            @RequestHeader(value = "Authorization", required = false) String auth,
            @PathVariable UUID productId) {
        UUID userId = identity.requireUser(auth);
        customerService.removeWishlist(userId, productId);
        return ResponseEntity.noContent().build();
    }

    // --- Reviews ---
    @GetMapping("/api/reviews/product/{productId}")
    public ResponseEntity<List<Map<String, Object>>> getReviews(@PathVariable UUID productId) {
        return ResponseEntity.ok(customerService.listReviews(productId));
    }

    public record CreateReviewRequest(UUID orderItemId, UUID productId, int rating, String comment) {}

    @PostMapping("/api/reviews")
    public ResponseEntity<Map<String, Object>> createReview(
            @RequestHeader(value = "Authorization", required = false) String auth,
            @RequestBody CreateReviewRequest req) {
        UUID userId = identity.requireUser(auth);
        UUID reviewId = customerService.createReview(userId, req.orderItemId(), req.productId(), req.rating(), req.comment());
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("id", reviewId, "status", "SUCCESS"));
    }
}

