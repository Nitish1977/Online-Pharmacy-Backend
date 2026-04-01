package com.pharmacy.order.controller;

import com.pharmacy.order.dto.CartRequest;
import com.pharmacy.order.entity.CartItem;
import com.pharmacy.order.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders/cart")
@Tag(name = "Cart", description = "Add, update and view cart items")
@SecurityRequirement(name = "Bearer Authentication")
public class CartController {

    private final OrderService orderService;

    public CartController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    @PreAuthorize("hasRole('CUSTOMER') or hasRole('ADMIN')")
    @Operation(summary = "Add item to cart [CUSTOMER/ADMIN]")
    public ResponseEntity<CartItem> addToCart(@Valid @RequestBody CartRequest request) {
        return ResponseEntity.ok(orderService.addToCart(request));
    }

    @GetMapping("/{customerId}")
    @PreAuthorize("hasRole('CUSTOMER') or hasRole('ADMIN')")
    @Operation(summary = "View cart [CUSTOMER/ADMIN]")
    public ResponseEntity<List<CartItem>> getCart(@PathVariable Long customerId) {
        return ResponseEntity.ok(orderService.getCart(customerId));
    }

    @PutMapping("/item/{cartItemId}")
    @PreAuthorize("hasRole('CUSTOMER') or hasRole('ADMIN')")
    @Operation(summary = "Update cart item quantity [CUSTOMER/ADMIN]")
    public ResponseEntity<Object> updateCartItem(@PathVariable Long cartItemId, @RequestParam int quantity) {
        CartItem updated = orderService.updateCartItem(cartItemId, quantity);
        return ResponseEntity.ok(updated != null ? updated : "Item removed from cart");
    }

    @DeleteMapping("/item/{cartItemId}")
    @PreAuthorize("hasRole('CUSTOMER') or hasRole('ADMIN')")
    @Operation(summary = "Remove item from cart [CUSTOMER/ADMIN]")
    public ResponseEntity<String> removeItem(@PathVariable Long cartItemId) {
        orderService.removeFromCart(cartItemId);
        return ResponseEntity.ok("Item removed from cart");
    }

    @DeleteMapping("/clear/{customerId}")
    @PreAuthorize("hasRole('CUSTOMER') or hasRole('ADMIN')")
    @Operation(summary = "Clear entire cart [CUSTOMER/ADMIN]")
    public ResponseEntity<String> clearCart(@PathVariable Long customerId) {
        orderService.clearCart(customerId);
        return ResponseEntity.ok("Cart cleared");
    }
}
