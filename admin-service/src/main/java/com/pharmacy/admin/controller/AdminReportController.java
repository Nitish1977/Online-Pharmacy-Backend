package com.pharmacy.admin.controller;

import com.pharmacy.admin.service.AdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/reports")
@Tag(name = "Admin Reports", description = "Sales, inventory and prescription reports")
@SecurityRequirement(name = "Bearer Authentication")
@PreAuthorize("hasRole('ADMIN')")
public class AdminReportController {

    private final AdminService adminService;

    public AdminReportController(AdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping("/sales")
    @Operation(summary = "Get sales report", description = "Returns total revenue, delivered orders and average order value")
    public ResponseEntity<Object> getSalesReport() {
        return ResponseEntity.ok(adminService.getSalesReport());
    }

    @GetMapping("/inventory")
    @Operation(summary = "Get inventory report", description = "Returns total medicines, low-stock list and expiring medicines list")
    public ResponseEntity<Object> getInventoryReport() {
        return ResponseEntity.ok(adminService.getInventoryReport());
    }
}
