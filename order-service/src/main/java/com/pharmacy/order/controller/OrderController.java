package com.pharmacy.order.controller;

import com.pharmacy.order.dto.CheckoutRequest;
import com.pharmacy.order.entity.Order;
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
@RequestMapping("/api/orders")
@Tag(name = "Orders", description = "Checkout, payment and order management")
@SecurityRequirement(name = "Bearer Authentication")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping("/checkout/start")
    @PreAuthorize("hasRole('CUSTOMER') or hasRole('ADMIN')")
    @Operation(summary = "Start checkout [CUSTOMER/ADMIN]",
               description = "Converts cart to order. If an Rx medicine has no prescription, status = PRESCRIPTION_PENDING")
    public ResponseEntity<Order> checkout(@Valid @RequestBody CheckoutRequest request) {
        return ResponseEntity.ok(orderService.checkout(request));
    }

    @PostMapping("/payments/initiate")
    @PreAuthorize("hasRole('CUSTOMER') or hasRole('ADMIN')")
    @Operation(summary = "Initiate payment [CUSTOMER/ADMIN]", description = "Stub: marks order as PAID")
    public ResponseEntity<Order> initiatePayment(@RequestParam Long orderId) {
        return ResponseEntity.ok(orderService.initiatePayment(orderId));
    }

    @GetMapping("/my/{customerId}")
    @PreAuthorize("hasRole('CUSTOMER') or hasRole('ADMIN')")
    @Operation(summary = "Get my order history [CUSTOMER/ADMIN]")
    public ResponseEntity<List<Order>> getMyOrders(@PathVariable Long customerId) {
        return ResponseEntity.ok(orderService.getMyOrders(customerId));
    }

    @GetMapping("/{orderId}")
    @PreAuthorize("hasRole('CUSTOMER') or hasRole('ADMIN')")
    @Operation(summary = "Get order by ID [CUSTOMER/ADMIN]")
    public ResponseEntity<Order> getOrderById(@PathVariable Long orderId) {
        return ResponseEntity.ok(orderService.getOrderById(orderId));
    }

    @PutMapping("/{orderId}/cancel")
    @PreAuthorize("hasRole('CUSTOMER') or hasRole('ADMIN')")
    @Operation(summary = "Cancel order [CUSTOMER/ADMIN]")
    public ResponseEntity<Order> cancelOrder(@PathVariable Long orderId) {
        return ResponseEntity.ok(orderService.cancelOrder(orderId));
    }
}
