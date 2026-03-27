package com.pharmacy.admin.controller;

import com.pharmacy.admin.entity.orders.OrderEntity;
import com.pharmacy.admin.service.AdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/orders")
@Tag(name = "Admin Order Management", description = "View and update order statuses (reads pharmacy_orders DB)")
@SecurityRequirement(name = "Bearer Authentication")
@PreAuthorize("hasRole('ADMIN')")
public class AdminOrderController {

    @Autowired
    private AdminService adminService;

    @GetMapping
    @Operation(summary = "Get all orders sorted by latest first [ADMIN]")
    public ResponseEntity<List<OrderEntity>> getAllOrders() {
        return ResponseEntity.ok(adminService.getAllOrders());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get order by ID [ADMIN]")
    public ResponseEntity<OrderEntity> getOrderById(@PathVariable Long id) {
        return ResponseEntity.ok(adminService.getOrderById(id));
    }

    @PutMapping("/{id}/status")
    @Operation(summary = "Update order status [ADMIN]",
               description = "Valid values: PACKED, OUT_FOR_DELIVERY, DELIVERED, ADMIN_CANCELLED, REFUND_INITIATED, REFUND_COMPLETED")
    public ResponseEntity<OrderEntity> updateOrderStatus(@PathVariable Long id,
                                                         @RequestParam String status) {
        return ResponseEntity.ok(adminService.updateOrderStatus(id, status));
    }

    @GetMapping("/by-status")
    @Operation(summary = "Filter orders by status [ADMIN]")
    public ResponseEntity<List<OrderEntity>> getOrdersByStatus(@RequestParam String status) {
        return ResponseEntity.ok(adminService.getOrdersByStatus(status));
    }
}
