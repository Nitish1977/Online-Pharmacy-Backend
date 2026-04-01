package com.pharmacy.admin.controller;

import com.pharmacy.admin.dto.MedicineRequest;
import com.pharmacy.admin.entity.catalog.MedicineEntity;
import com.pharmacy.admin.service.AdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/medicines")
@Tag(name = "Admin Medicine Management", description = "Full CRUD for medicines and inventory (reads/writes pharmacy_catalog DB)")
@SecurityRequirement(name = "Bearer Authentication")
@PreAuthorize("hasRole('ADMIN')")
public class AdminMedicineController {

    private final AdminService adminService;

    public AdminMedicineController(AdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping
    @Operation(summary = "Get all medicines [ADMIN]")
    public ResponseEntity<List<MedicineEntity>> getAllMedicines() {
        return ResponseEntity.ok(adminService.getAllMedicines());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get medicine by ID [ADMIN]")
    public ResponseEntity<MedicineEntity> getMedicineById(@PathVariable Long id) {
        return ResponseEntity.ok(adminService.getMedicineById(id));
    }

    @PostMapping
    @Operation(summary = "Add new medicine [ADMIN]")
    public ResponseEntity<MedicineEntity> addMedicine(@Valid @RequestBody MedicineRequest request) {
        return ResponseEntity.ok(adminService.addMedicine(request));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update medicine [ADMIN]")
    public ResponseEntity<MedicineEntity> updateMedicine(@PathVariable Long id,
                                                         @Valid @RequestBody MedicineRequest request) {
        return ResponseEntity.ok(adminService.updateMedicine(id, request));
    }

    @PatchMapping("/{id}/stock")
    @Operation(summary = "Update stock quantity [ADMIN] — positive adds, negative deducts")
    public ResponseEntity<MedicineEntity> updateStock(@PathVariable Long id,
                                                      @RequestParam int quantity) {
        return ResponseEntity.ok(adminService.updateStock(id, quantity));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Deactivate medicine [ADMIN]")
    public ResponseEntity<String> deleteMedicine(@PathVariable Long id) {
        adminService.deleteMedicine(id);
        return ResponseEntity.ok("Medicine deactivated successfully");
    }

    @GetMapping("/low-stock")
    @Operation(summary = "Get medicines with stock below 10 [ADMIN]")
    public ResponseEntity<List<MedicineEntity>> getLowStock() {
        return ResponseEntity.ok(adminService.getLowStockMedicines());
    }

    @GetMapping("/expiring")
    @Operation(summary = "Get medicines expiring within N days [ADMIN] — default 30")
    public ResponseEntity<List<MedicineEntity>> getExpiring(
            @RequestParam(defaultValue = "30") int days) {
        return ResponseEntity.ok(adminService.getExpiringMedicines(days));
    }
}
